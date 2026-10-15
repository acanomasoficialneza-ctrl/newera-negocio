package com.newera.negocio.controllers;

import com.newera.negocio.models.TransaccionCaja;
import com.newera.negocio.services.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> getTransacciones(
            @PathVariable Integer idUsuario,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page != null) {
            return ResponseEntity.ok(cajaService.getTransaccionesByUsuarioPaginado(idUsuario, page, size));
        }
        return ResponseEntity.ok(cajaService.getTransaccionesByUsuario(idUsuario));
    }

    @GetMapping
    public ResponseEntity<List<TransaccionCaja>> getAllTransacciones() {
        return ResponseEntity.ok(cajaService.getAllTransacciones());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> getTransaccionesPendientes(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page != null) {
            return ResponseEntity.ok(cajaService.getTransaccionesPendientesPaginado(page, size));
        }
        return ResponseEntity.ok(cajaService.getTransaccionesPendientes());
    }

    @GetMapping("/historial")
    public ResponseEntity<?> getTransaccionesHistorial(
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        if (page != null) {
            return ResponseEntity.ok(cajaService.getTransaccionesHistorialPaginado(page, size));
        }
        return ResponseEntity.ok(cajaService.getTransaccionesHistorial());
    }

    @PostMapping("/solicitar")
    public ResponseEntity<TransaccionCaja> solicitarTransaccion(@RequestBody TransaccionCaja transaccion) {
        return ResponseEntity.ok(cajaService.solicitarTransaccion(transaccion));
    }

    @PutMapping("/aprobar/{idTransaccion}")
    public ResponseEntity<TransaccionCaja> aprobarTransaccion(@PathVariable Integer idTransaccion, @RequestParam Integer idAdmin, @RequestParam(required = false) String nota) {
        return ResponseEntity.ok(cajaService.aprobarTransaccion(idTransaccion, idAdmin, nota));
    }

    @PutMapping("/rechazar/{idTransaccion}")
    public ResponseEntity<TransaccionCaja> rechazarTransaccion(@PathVariable Integer idTransaccion, @RequestParam Integer idAdmin, @RequestParam(required = false) String nota) {
        return ResponseEntity.ok(cajaService.rechazarTransaccion(idTransaccion, idAdmin, nota));
    }
}
