package com.newera.negocio.controllers;

import com.newera.negocio.models.AsignacionCliente;
import com.newera.negocio.models.PerfilAdmin;
import com.newera.negocio.models.PerfilCliente;
import com.newera.negocio.repositories.AsignacionClienteRepository;
import com.newera.negocio.repositories.PerfilAdminRepository;
import com.newera.negocio.repositories.PerfilClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionClienteRepository asignacionRepository;
    private final PerfilClienteRepository perfilClienteRepository;
    private final PerfilAdminRepository perfilAdminRepository;
    private final com.newera.negocio.services.AuditoriaService auditoriaService;

    @PostMapping
    public ResponseEntity<?> asignarCliente(@RequestBody java.util.Map<String, Integer> body) {
        Integer idCliente = body.get("idCliente");
        Integer idAdmin = body.get("idAdmin");
        
        if (idCliente == null || idAdmin == null) {
            return ResponseEntity.badRequest().body("Faltan idCliente o idAdmin");
        }

        AsignacionCliente asignacion = new AsignacionCliente();
        asignacion.setIdCliente(idCliente);
        asignacion.setIdAdmin(idAdmin);
        asignacion.setFechaAsignacion(LocalDateTime.now());
        
        AsignacionCliente saved = asignacionRepository.save(asignacion);
        
        String nombreCliente = "Desconocido";
        PerfilCliente pc = perfilClienteRepository.findById(idCliente).orElse(null);
        if (pc != null) nombreCliente = pc.getNombreCompleto();

        String nombreAdmin = "Desconocido";
        PerfilAdmin pa = perfilAdminRepository.findById(idAdmin).orElse(null);
        if (pa != null) nombreAdmin = pa.getNombreCompleto();

        auditoriaService.registrarLog("INFO", "Asignación: Cliente " + nombreCliente + " (ID: " + idCliente + ") asignado al ejecutivo/admin " + nombreAdmin + " (ID: " + idAdmin + ")", "System");
        
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/admin/{idAdmin}")
    public ResponseEntity<?> getClientesPorAdmin(@PathVariable Integer idAdmin) {
        return ResponseEntity.ok(asignacionRepository.findByIdAdmin(idAdmin));
    }

    @GetMapping
    public ResponseEntity<?> getAllAsignaciones() {
        return ResponseEntity.ok(asignacionRepository.findAll());
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<?> getAsignacionesPorCliente(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(asignacionRepository.findByIdCliente(idCliente));
    }

    @DeleteMapping("/cliente/{idCliente}/admin/{idAdmin}")
    public ResponseEntity<?> eliminarAsignacion(@PathVariable Integer idCliente, @PathVariable Integer idAdmin) {
        asignacionRepository.deleteByIdClienteAndIdAdmin(idCliente, idAdmin);

        String nombreCliente = "Desconocido";
        PerfilCliente pc = perfilClienteRepository.findById(idCliente).orElse(null);
        if (pc != null) nombreCliente = pc.getNombreCompleto();

        String nombreAdmin = "Desconocido";
        PerfilAdmin pa = perfilAdminRepository.findById(idAdmin).orElse(null);
        if (pa != null) nombreAdmin = pa.getNombreCompleto();

        auditoriaService.registrarLog("INFO", "Asignación: Cliente " + nombreCliente + " (ID: " + idCliente + ") desasignado del ejecutivo/admin " + nombreAdmin + " (ID: " + idAdmin + ")", "System");
        return ResponseEntity.ok().build();
    }
}
