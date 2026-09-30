package com.newera.negocio.controllers;

import com.newera.negocio.models.CrmNota;
import com.newera.negocio.services.CrmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/crm")
@RequiredArgsConstructor
public class CrmController {

    private final CrmService crmService;

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<CrmNota>> getNotas(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(crmService.getNotasByCliente(idCliente));
    }

    @PostMapping
    public ResponseEntity<CrmNota> createNota(@RequestBody CrmNota nota) {
        return ResponseEntity.ok(crmService.saveNota(nota));
    }
}
