package com.newera.negocio.services;

import com.newera.negocio.models.CrmNota;
import com.newera.negocio.repositories.CrmNotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmService {

    private final CrmNotaRepository crmNotaRepository;

    public List<CrmNota> getNotasByCliente(Integer idCliente) {
        return crmNotaRepository.findByClienteIdUsuario(idCliente);
    }

    public CrmNota saveNota(CrmNota nota) {
        nota.setFechaCreacion(LocalDateTime.now());
        return crmNotaRepository.save(nota);
    }
}
