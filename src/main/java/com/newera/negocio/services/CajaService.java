package com.newera.negocio.services;

import com.newera.negocio.models.TransaccionCaja;
import com.newera.negocio.models.UsuariosAuth;
import com.newera.negocio.models.PerfilCliente;
import com.newera.negocio.models.PerfilAdmin;
import com.newera.negocio.repositories.TransaccionCajaRepository;
import com.newera.negocio.repositories.UsuariosAuthRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
import com.newera.negocio.repositories.PerfilAdminRepository;
import com.newera.negocio.services.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajaService {

    private final TransaccionCajaRepository cajaRepository;
    private final UsuariosAuthRepository usuarioRepository;
    private final PerfilClienteRepository perfilClienteRepository;
    private final PerfilAdminRepository perfilAdminRepository;
    private final AuditoriaService auditoriaService;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<TransaccionCaja> getTransaccionesByUsuario(Integer idUsuario) {
        return cajaRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public List<TransaccionCaja> getAllTransacciones() {
        return cajaRepository.findAll();
    }

    public List<TransaccionCaja> getTransaccionesPendientes() {
        return cajaRepository.findByEstatus("PENDIENTE");
    }

    public List<TransaccionCaja> getTransaccionesHistorial() {
        List<TransaccionCaja> list = cajaRepository.findByEstatusNot("PENDIENTE");
        list.sort((a, b) -> b.getFechaSolicitud().compareTo(a.getFechaSolicitud()));
        return list;
    }

    @Transactional
    public TransaccionCaja solicitarTransaccion(TransaccionCaja transaccion) {
        transaccion.setEstatus("PENDIENTE");
        transaccion.setFechaSolicitud(LocalDateTime.now());
        TransaccionCaja saved = cajaRepository.save(transaccion);
        
        String nombreCliente = "Desconocido";
        PerfilCliente pc = perfilClienteRepository.findById(transaccion.getUsuario().getIdUsuario()).orElse(null);
        if (pc != null) nombreCliente = pc.getNombreCompleto();
        
        Integer idAdminResponsable = transaccion.getIdAdmin();
        String adminStr = "";
        if (idAdminResponsable != null) {
            PerfilAdmin pa = perfilAdminRepository.findById(idAdminResponsable).orElse(null);
            if (pa != null) adminStr = " - Solicitado por admin: " + pa.getNombreCompleto() + " (ID: " + idAdminResponsable + ")";
        } else {
            adminStr = " - Solicitado por el propio cliente";
        }

        auditoriaService.registrarLog("INFO", "Caja: Solicitud de " + transaccion.getTipoTransaccion() + " por " + transaccion.getMonto() + " para cliente " + nombreCliente + " (ID: " + transaccion.getUsuario().getIdUsuario() + ")" + adminStr, "System");
        
        // Disparar Notificación al Supremo (Asumiendo Supremo id=1)
        try {
            java.util.Map<String, Object> req = new java.util.HashMap<>();
            req.put("idUsuarioDestino", 1); // ID del Supremo
            req.put("correoDestino", "supremo@newera.com");
            req.put("titulo", "Nueva Solicitud de Caja");
            req.put("mensaje", "Se ha solicitado un " + transaccion.getTipoTransaccion() + " por la cantidad de " + transaccion.getMonto() + ".");
            
            restTemplate.postForEntity("http://localhost:8084/api/v1/notificaciones/enviar", req, String.class);
        } catch (Exception e) {
            System.err.println("No se pudo enviar la notificación: " + e.getMessage());
        }

        return saved;
    }

    @Transactional
    public TransaccionCaja aprobarTransaccion(Integer idTransaccion, Integer idAdmin, String nota) {
        TransaccionCaja transaccion = cajaRepository.findById(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
                
        UsuariosAuth admin = usuarioRepository.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        transaccion.setEstatus("APROBADO");
        transaccion.setFechaResolucion(LocalDateTime.now());
        transaccion.setAdminAprobador(admin);

        if (nota != null && !nota.trim().isEmpty()) {
            String current = transaccion.getDetallesCuenta();
            transaccion.setDetallesCuenta((current == null ? "" : current + " | ") + "Nota: " + nota.trim());
        }

        UsuariosAuth clienteAuth = transaccion.getUsuario();
        PerfilCliente perfilCliente = perfilClienteRepository.findById(clienteAuth.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil del cliente no encontrado"));
        
        if ("DEPOSITO".equalsIgnoreCase(transaccion.getTipoTransaccion())) {
            perfilCliente.setBalance(perfilCliente.getBalance().add(transaccion.getMonto()));
            perfilCliente.setMargenLibre(perfilCliente.getMargenLibre().add(transaccion.getMonto()));
        } else if ("RETIRO".equalsIgnoreCase(transaccion.getTipoTransaccion())) {
            perfilCliente.setBalance(perfilCliente.getBalance().subtract(transaccion.getMonto()));
            perfilCliente.setMargenLibre(perfilCliente.getMargenLibre().subtract(transaccion.getMonto()));
        }
        
        perfilClienteRepository.save(perfilCliente);
        
        String nombreAdmin = "Desconocido";
        PerfilAdmin pa = perfilAdminRepository.findById(idAdmin).orElse(null);
        if (pa != null) nombreAdmin = pa.getNombreCompleto();
        
        auditoriaService.registrarLog("INFO", "Caja: " + transaccion.getTipoTransaccion() + " aprobado por " + transaccion.getMonto() + " para cliente " + perfilCliente.getNombreCompleto() + " (ID: " + perfilCliente.getIdUsuario() + ") - Procesado por: " + nombreAdmin + " (ID: " + idAdmin + ")", "System");
        
        return cajaRepository.save(transaccion);
    }

    @Transactional
    public TransaccionCaja rechazarTransaccion(Integer idTransaccion, Integer idAdmin, String nota) {
        TransaccionCaja transaccion = cajaRepository.findById(idTransaccion)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada"));
                
        UsuariosAuth admin = usuarioRepository.findById(idAdmin)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        transaccion.setEstatus("RECHAZADO");
        transaccion.setFechaResolucion(LocalDateTime.now());
        transaccion.setAdminAprobador(admin);

        if (nota != null && !nota.trim().isEmpty()) {
            String current = transaccion.getDetallesCuenta();
            transaccion.setDetallesCuenta((current == null ? "" : current + " | ") + "Razón: " + nota.trim());
        }

        String nombreAdmin = "Desconocido";
        PerfilAdmin pa = perfilAdminRepository.findById(idAdmin).orElse(null);
        if (pa != null) nombreAdmin = pa.getNombreCompleto();

        String nombreCliente = "Desconocido";
        PerfilCliente pc = perfilClienteRepository.findById(transaccion.getUsuario().getIdUsuario()).orElse(null);
        if (pc != null) nombreCliente = pc.getNombreCompleto();

        auditoriaService.registrarLog("WARN", "Caja: " + transaccion.getTipoTransaccion() + " rechazado por " + transaccion.getMonto() + " para cliente " + nombreCliente + " (ID: " + transaccion.getUsuario().getIdUsuario() + ") - Procesado por: " + nombreAdmin + " (ID: " + idAdmin + ")", "System");
        
        return cajaRepository.save(transaccion);
    }
}
