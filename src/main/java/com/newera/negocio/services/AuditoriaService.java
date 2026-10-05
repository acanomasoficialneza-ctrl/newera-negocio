package com.newera.negocio.services;

import com.newera.negocio.models.Auditoria;
import com.newera.negocio.repositories.AuditoriaRepository;
import com.newera.negocio.repositories.PerfilAdminRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final PerfilAdminRepository adminRepository;
    private final PerfilClienteRepository clienteRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public void registrarLog(String nivel, String mensaje, String ip) {
        registrarLog(nivel, mensaje, ip, null);
    }

    public void registrarLog(String nivel, String mensaje, String ip, Integer idUsuarioResponsable) {
        Auditoria log = new Auditoria();
        log.setNivel(nivel);
        String finalMessage = mensaje;
        if (idUsuarioResponsable != null) {
            String nombreResponsable = "Desconocido";
            var admin = adminRepository.findById(idUsuarioResponsable).orElse(null);
            if (admin != null) {
                nombreResponsable = admin.getNombreCompleto();
            } else {
                var cliente = clienteRepository.findById(idUsuarioResponsable).orElse(null);
                if (cliente != null) {
                    nombreResponsable = cliente.getNombreCompleto();
                }
            }
            finalMessage += " - Usuario Responsable: " + nombreResponsable;
        }

        log.setMensaje(finalMessage);
        log.setDireccionIp(ip != null ? ip : "System");
        log.setFechaEvento(LocalDateTime.now());
        log.setIdUsuarioResponsable(idUsuarioResponsable);
        
        auditoriaRepository.save(log);

        if ("CRITICAL".equalsIgnoreCase(nivel)) {
            try {
                String titulo = finalMessage.startsWith("INFRA:") ? "ALERTA DE INFRAESTRUCTURA" : "Alerta Crítica de Seguridad";
                String mensajeLimpio = finalMessage.startsWith("INFRA:") ? finalMessage.replace("INFRA: ", "") : finalMessage;
                
                java.util.Map<String, Object> req = new java.util.HashMap<>();
                req.put("idUsuarioDestino", 1); // ID del Supremo
                req.put("correoDestino", "supremo@newera.com");
                req.put("titulo", titulo);
                req.put("mensaje", mensajeLimpio);
                req.put("tipo", "ALERTA");
                
                restTemplate.postForEntity("http://localhost:8084/api/v1/notificaciones/enviar", req, String.class);
            } catch (Exception e) {
                System.err.println("No se pudo enviar la notificación de auditoría: " + e.getMessage());
            }
        }
    }

    public java.util.List<Auditoria> getAllLogs() {
        return auditoriaRepository.findAll();
    }

    public java.util.List<Auditoria> getLogsByDate(String date) {
        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(date);
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(java.time.LocalTime.MAX);
            return auditoriaRepository.findByFechaEventoBetween(startOfDay, endOfDay);
        } catch (Exception e) {
            return auditoriaRepository.findAll();
        }
    }
}
