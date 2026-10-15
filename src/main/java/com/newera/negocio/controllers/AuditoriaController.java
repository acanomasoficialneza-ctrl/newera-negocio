package com.newera.negocio.controllers;

import com.newera.negocio.models.Auditoria;
import com.newera.negocio.services.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public ResponseEntity<?> getBitacora(
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page != null) {
            if (fecha != null && !fecha.isBlank()) {
                return ResponseEntity.ok(auditoriaService.getLogsByDatePaginado(fecha, page, size));
            }
            return ResponseEntity.ok(auditoriaService.getLogsPaginado(page, size));
        }
        if (fecha != null && !fecha.isBlank()) {
            return ResponseEntity.ok(auditoriaService.getLogsByDate(fecha));
        }
        return ResponseEntity.ok(auditoriaService.getAllLogs());
    }

    @PostMapping
    public ResponseEntity<?> registrarLog(@RequestBody java.util.Map<String, Object> payload) {
        String nivel = (String) payload.getOrDefault("nivel", "INFO");
        String mensaje = (String) payload.get("mensaje");
        String ip = (String) payload.getOrDefault("ip", "System");
        Integer idUsuarioResponsable = payload.containsKey("idUsuarioResponsable") ? (Integer) payload.get("idUsuarioResponsable") : null;
        
        auditoriaService.registrarLog(nivel, mensaje, ip, idUsuarioResponsable);
        return ResponseEntity.ok().build();
    }
}
