package com.newera.negocio.controllers;

import com.newera.negocio.models.ApuestaCliente;
import com.newera.negocio.services.ApuestaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/apuestas")
@RequiredArgsConstructor
public class ApuestaController {

    private final ApuestaService apuestaService;

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ApuestaCliente>> getApuestas(
            @PathVariable Integer idUsuario,
            @RequestParam(required = false) String estatus) {
        return ResponseEntity.ok(apuestaService.getApuestasByUsuario(idUsuario, estatus));
    }

    @PostMapping("/abrir")
    public ResponseEntity<ApuestaCliente> abrirPosicion(@RequestBody ApuestaCliente apuesta) {
        return ResponseEntity.ok(apuestaService.abrirPosicion(apuesta));
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<ApuestaCliente> cerrarPosicion(@PathVariable Integer id, @RequestParam BigDecimal gananciaPerdida, @RequestParam(required = false) Integer idAdmin) {
        return ResponseEntity.ok(apuestaService.cerrarPosicion(id, gananciaPerdida, idAdmin));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApuestaCliente> actualizarApuesta(@PathVariable Integer id, @RequestBody java.util.Map<String, Object> updates) {
        return ResponseEntity.ok(apuestaService.actualizarApuesta(id, updates));
    }
}
