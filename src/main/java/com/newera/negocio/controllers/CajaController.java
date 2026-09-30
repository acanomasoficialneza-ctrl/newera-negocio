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
    public ResponseEntity<List<TransaccionCaja>> getTransacciones(@PathVariable Integer idUsuario) {
        return ResponseEntity.ok(cajaService.getTransaccionesByUsuario(idUsuario));
    }

    @GetMapping
    public ResponseEntity<List<TransaccionCaja>> getAllTransacciones() {
        return ResponseEntity.ok(cajaService.getAllTransacciones());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<TransaccionCaja>> getTransaccionesPendientes() {
        return ResponseEntity.ok(cajaService.getTransaccionesPendientes());
    }

    @GetMapping("/historial")
    public ResponseEntity<List<TransaccionCaja>> getTransaccionesHistorial() {
        return ResponseEntity.ok(cajaService.getTransaccionesHistorial());
    }

    @PostMapping("/solicitar")
    public ResponseEntity<TransaccionCaja> solicitarTransaccion(@RequestBody TransaccionCaja transaccion) {
        return ResponseEntity.ok(cajaService.solicitarTransaccion(transaccion));
    }

    @PutMapping("/aprobar/{idTransaccion}")
    public ResponseEntity<TransaccionCaja> aprobarTransaccion(@PathVariable Integer idTransaccion, @RequestParam Integer idAdmin) {
        return ResponseEntity.ok(cajaService.aprobarTransaccion(idTransaccion, idAdmin));
    }

    @PutMapping("/rechazar/{idTransaccion}")
    public ResponseEntity<TransaccionCaja> rechazarTransaccion(@PathVariable Integer idTransaccion, @RequestParam Integer idAdmin) {
        return ResponseEntity.ok(cajaService.rechazarTransaccion(idTransaccion, idAdmin));
    }
}
