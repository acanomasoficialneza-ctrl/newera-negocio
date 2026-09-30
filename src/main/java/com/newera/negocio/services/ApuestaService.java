package com.newera.negocio.services;

import com.newera.negocio.models.ApuestaCliente;
import com.newera.negocio.models.PerfilCliente;
import com.newera.negocio.repositories.ApuestaClienteRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
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
    private final AuditoriaService auditoriaService;

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
            PerfilCliente pc = perfilClienteRepository.findById(apuesta.getUsuario().getIdUsuario())
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
            
            pc.setMargenLibre(pc.getMargenLibre().subtract(margenRequerido));
            
            java.math.BigDecimal margenActualizado = pc.getMargenUtilizado() != null ? pc.getMargenUtilizado() : java.math.BigDecimal.ZERO;
            pc.setMargenUtilizado(margenActualizado.add(margenRequerido));
            
            perfilClienteRepository.save(pc);
        }
        
        ApuestaCliente saved = apuestaRepository.save(apuesta);

        auditoriaService.registrarLog("INFO", "Trading: Orden abierta (Activo: " + apuesta.getCompra() + ", Tipo: " + apuesta.getTipoCompra() + ") para cliente " + nombreCliente + " (ID: " + idClienteStr + ").", "System");
        return saved;
    }

    @Transactional
    public ApuestaCliente cerrarPosicion(Integer idApuesta, java.math.BigDecimal gananciaPerdidaFinal) {
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
            PerfilCliente pc = perfilClienteRepository.findById(apuesta.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
                
            nombreCliente = pc.getNombreCompleto();
            
            // Lógica para devolver margen y sumar ganancia/pérdida
            java.math.BigDecimal margenRequerido = apuesta.getMargen();
            if (margenRequerido == null) {
                margenRequerido = java.math.BigDecimal.ZERO;
            }
            
            // 1. Devolvemos el margen a libre y lo restamos de utilizado
            pc.setMargenLibre(pc.getMargenLibre() != null ? pc.getMargenLibre().add(margenRequerido) : margenRequerido);
            
            java.math.BigDecimal margenActualizado = pc.getMargenUtilizado() != null ? pc.getMargenUtilizado() : java.math.BigDecimal.ZERO;
            pc.setMargenUtilizado(margenActualizado.subtract(margenRequerido).max(java.math.BigDecimal.ZERO));
            
            // 2. Aplicamos la ganancia o pérdida final al balance y al margen libre
            if (gananciaPerdidaFinal != null) {
                pc.setBalance(pc.getBalance() != null ? pc.getBalance().add(gananciaPerdidaFinal) : gananciaPerdidaFinal);
                pc.setMargenLibre(pc.getMargenLibre().add(gananciaPerdidaFinal));
            }
            
            perfilClienteRepository.save(pc);
        }
        
        ApuestaCliente saved = apuestaRepository.save(apuesta);

        auditoriaService.registrarLog("INFO", "Trading: Orden CERRADA (ID: " + idApuesta + ") con P&L: " + gananciaPerdidaFinal + " para cliente " + nombreCliente + " (ID: " + idClienteStr + ")", "System");
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
