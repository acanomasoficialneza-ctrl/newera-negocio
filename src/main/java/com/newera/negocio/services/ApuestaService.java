package com.newera.negocio.services;

import com.newera.negocio.models.ApuestaCliente;
import com.newera.negocio.models.PerfilCliente;
import com.newera.negocio.models.PerfilAdmin;
import com.newera.negocio.repositories.ApuestaClienteRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
import com.newera.negocio.repositories.PerfilAdminRepository;
import com.newera.negocio.repositories.AsignacionClienteRepository;
import com.newera.negocio.models.AsignacionCliente;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.newera.negocio.services.AuditoriaService;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ApuestaService {

    private final ApuestaClienteRepository apuestaRepository;
    private final PerfilClienteRepository perfilClienteRepository;
    private final PerfilAdminRepository perfilAdminRepository;
    private final AuditoriaService auditoriaService;
    private final AsignacionClienteRepository asignacionClienteRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ApuestaCliente> getApuestasByUsuario(Integer idUsuario, String estatusCompra) {
        if (estatusCompra != null && !estatusCompra.isEmpty()) {
            return apuestaRepository.findByUsuarioIdUsuarioAndEstatusCompra(idUsuario, estatusCompra);
        }
        return apuestaRepository.findByUsuarioIdUsuario(idUsuario);
    }

    @Transactional
    public ApuestaCliente abrirPosicion(ApuestaCliente apuesta) {
        apuesta.setEstatusCompra("ABIERTO");
        apuesta.setFechaCreacion(LocalDateTime.now());
        
        String nombreCliente = "Desconocido";
        String idClienteStr = "N/A";
        
        if (apuesta.getUsuario() != null) {
            idClienteStr = String.valueOf(apuesta.getUsuario().getIdUsuario());
            PerfilCliente pc = perfilClienteRepository.findByIdLocked(apuesta.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado para ID: " + apuesta.getUsuario().getIdUsuario()));
            
            nombreCliente = pc.getNombreCompleto();
            
            // Validar y descontar margen libre, sumar a margen utilizado
            java.math.BigDecimal margenRequerido = apuesta.getMargen();
            if (margenRequerido == null) {
                margenRequerido = java.math.BigDecimal.ZERO;
            }
            
            if (pc.getMargenLibre() == null || pc.getMargenLibre().compareTo(margenRequerido) < 0) {
                throw new RuntimeException("Margen libre insuficiente para abrir esta posición");
            }
            
            // Calcular el Margen Utilizado sumando TODAS las posiciones reales (previniendo "margen volador")
            java.util.List<ApuestaCliente> posicionesAbiertas = apuestaRepository.findByUsuarioIdUsuarioAndEstatusCompra(apuesta.getUsuario().getIdUsuario(), "ABIERTO");
            java.math.BigDecimal realMargenUtilizado = margenRequerido; // Sumar la orden que estamos abriendo
            for (ApuestaCliente pos : posicionesAbiertas) {
                if (pos.getMargen() != null) {
                    realMargenUtilizado = realMargenUtilizado.add(pos.getMargen());
                }
            }
            
            pc.setMargenUtilizado(realMargenUtilizado);
            pc.setMargenLibre(pc.getBalance().subtract(realMargenUtilizado));
            
            perfilClienteRepository.save(pc);
        }
        
        ApuestaCliente saved = apuestaRepository.save(apuesta);

        auditoriaService.registrarLog("INFO", "Trading: Orden abierta (Activo: " + apuesta.getCompra() + ", Tipo: " + apuesta.getTipoCompra() + ") para cliente " + nombreCliente + " (ID: " + idClienteStr + ").", "System");
        
        // Notify assigned admins
        if (apuesta.getUsuario() != null) {
            try {
                List<AsignacionCliente> asignaciones = asignacionClienteRepository.findByIdCliente(apuesta.getUsuario().getIdUsuario());
                for (AsignacionCliente asig : asignaciones) {
                    java.util.Map<String, Object> req = new java.util.HashMap<>();
                    req.put("idUsuarioDestino", asig.getIdAdmin());
                    req.put("correoDestino", "");
                    req.put("titulo", "Posición Abierta");
                    req.put("mensaje", "El cliente " + nombreCliente + " ha abierto una posición de " + apuesta.getCompra() + " (" + apuesta.getTipoCompra() + ").");
                    req.put("tipo", "INFO");
                    restTemplate.postForObject("http://localhost:8084/api/v1/notificaciones/enviar", req, Object.class);
                }
            } catch(Exception e) {
                System.err.println("Error enviando notificacion a admins: " + e.getMessage());
            }
        }
        
        return saved;
    }

    @Transactional
    public ApuestaCliente cerrarPosicion(Integer idApuesta, java.math.BigDecimal gananciaPerdidaFinal, Integer idAdmin) {
        ApuestaCliente apuesta = apuestaRepository.findById(idApuesta)
                .orElseThrow(() -> new RuntimeException("Apuesta no encontrada"));
        
        if ("CERRADO".equals(apuesta.getEstatusCompra())) {
            throw new RuntimeException("La apuesta ya se encuentra cerrada");
        }
        
        apuesta.setEstatusCompra("CERRADO");
        apuesta.setFechaCierre(LocalDateTime.now());
        apuesta.setGananciaPerdida(gananciaPerdidaFinal);
        
        String nombreCliente = "Desconocido";
        String idClienteStr = "N/A";
        
        if (apuesta.getUsuario() != null) {
            idClienteStr = String.valueOf(apuesta.getUsuario().getIdUsuario());
            PerfilCliente pc = perfilClienteRepository.findByIdLocked(apuesta.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
                
            nombreCliente = pc.getNombreCompleto();
            
            // Lógica para devolver margen y sumar ganancia/pérdida
            java.math.BigDecimal margenRequerido = apuesta.getMargen();
            if (margenRequerido == null) {
                margenRequerido = java.math.BigDecimal.ZERO;
            }
            
            // 1. Aplicamos la ganancia o pérdida final al balance general
            if (gananciaPerdidaFinal != null) {
                pc.setBalance(pc.getBalance() != null ? pc.getBalance().add(gananciaPerdidaFinal) : gananciaPerdidaFinal);
            }
            
            // 2. Recalculamos el Margen Utilizado exacto leyendo las posiciones abiertas
            java.util.List<ApuestaCliente> posicionesAbiertas = apuestaRepository.findByUsuarioIdUsuarioAndEstatusCompra(apuesta.getUsuario().getIdUsuario(), "ABIERTO");
            java.math.BigDecimal realMargenUtilizado = java.math.BigDecimal.ZERO;
            for (ApuestaCliente pos : posicionesAbiertas) {
                // Ignoramos la posición que estamos cerrando actualmente
                if (!pos.getIdApuestaCliente().equals(idApuesta) && pos.getMargen() != null) {
                    realMargenUtilizado = realMargenUtilizado.add(pos.getMargen());
                }
            }
            
            // 3. Establecemos los márgenes precisos y blindados
            pc.setMargenUtilizado(realMargenUtilizado);
            pc.setMargenLibre(pc.getBalance().subtract(realMargenUtilizado));
            
            perfilClienteRepository.save(pc);
        }
        
        ApuestaCliente saved = apuestaRepository.save(apuesta);

        String adminStr = "";
        if (idAdmin != null) {
            PerfilAdmin pa = perfilAdminRepository.findById(idAdmin).orElse(null);
            if (pa != null) adminStr = " - Cerrada por admin: " + pa.getNombreCompleto() + " (ID: " + idAdmin + ")";
        } else {
            adminStr = " - Cerrada por el propio cliente";
        }

        auditoriaService.registrarLog("INFO", "Trading: Orden CERRADA (ID: " + idApuesta + ") con P&L: " + gananciaPerdidaFinal + " para cliente " + nombreCliente + " (ID: " + idClienteStr + ")" + adminStr, "System");

        // Notify assigned admins if closed by client
        if (apuesta.getUsuario() != null && idAdmin == null) {
            try {
                List<AsignacionCliente> asignaciones = asignacionClienteRepository.findByIdCliente(apuesta.getUsuario().getIdUsuario());
                for (AsignacionCliente asig : asignaciones) {
                    java.util.Map<String, Object> req = new java.util.HashMap<>();
                    req.put("idUsuarioDestino", asig.getIdAdmin());
                    req.put("correoDestino", "");
                    req.put("titulo", "Posición Cerrada");
                    req.put("mensaje", "El cliente " + nombreCliente + " cerró su posición de " + apuesta.getCompra() + ". P&L: $" + gananciaPerdidaFinal);
                    req.put("tipo", "INFO");
                    restTemplate.postForObject("http://localhost:8084/api/v1/notificaciones/enviar", req, Object.class);
                }
            } catch(Exception e) {
                System.err.println("Error enviando notificacion a admins: " + e.getMessage());
            }
        }
        
        return saved;
    }

    @Transactional
    public ApuestaCliente actualizarApuesta(Integer idApuesta, java.util.Map<String, Object> updates) {
        ApuestaCliente apuesta = apuestaRepository.findById(idApuesta)
                .orElseThrow(() -> new RuntimeException("Apuesta no encontrada"));
        
        if (updates.containsKey("margen")) {
            apuesta.setMargen(new java.math.BigDecimal(updates.get("margen").toString()));
        }
        if (updates.containsKey("swap")) {
            apuesta.setSwap(new java.math.BigDecimal(updates.get("swap").toString()));
        }
        
        ApuestaCliente saved = apuestaRepository.save(apuesta);

        String nombreCliente = "Desconocido";
        if (apuesta.getUsuario() != null) {
            PerfilCliente pc = perfilClienteRepository.findById(apuesta.getUsuario().getIdUsuario()).orElse(null);
            if (pc != null) nombreCliente = pc.getNombreCompleto();
        }
        
        String idClienteStr = apuesta.getUsuario() != null ? String.valueOf(apuesta.getUsuario().getIdUsuario()) : "N/A";
        auditoriaService.registrarLog("INFO", "Trading: Orden ACTUALIZADA (ID: " + idApuesta + ") para cliente " + nombreCliente + " (ID: " + idClienteStr + ")", "System");
        return saved;
    }
}
