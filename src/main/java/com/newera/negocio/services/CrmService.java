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
    private final com.newera.negocio.repositories.PerfilAdminRepository adminRepository;

    public List<CrmNota> getNotasByCliente(Integer idCliente) {
        return crmNotaRepository.findByClienteIdUsuarioOrderByFechaCreacionDesc(idCliente);
    }

    public List<java.util.Map<String, Object>> getNotasConNombres(Integer idCliente) {
        List<CrmNota> notas = crmNotaRepository.findByClienteIdUsuarioOrderByFechaCreacionDesc(idCliente);
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (CrmNota nota : notas) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("idNota", nota.getIdNota());
            map.put("nota", nota.getNota());
            map.put("fechaCreacion", nota.getFechaCreacion());
            
            java.util.Map<String, Object> colabMap = new java.util.HashMap<>();
            colabMap.put("idUsuario", nota.getColaborador().getIdUsuario());
            colabMap.put("correo", nota.getColaborador().getCorreo());
            
            // Fetch name
            String nombre = nota.getColaborador().getCorreo();
            var admin = adminRepository.findById(nota.getColaborador().getIdUsuario()).orElse(null);
            if (admin != null && admin.getNombreCompleto() != null) {
                nombre = admin.getNombreCompleto();
            }
            colabMap.put("nombreCompleto", nombre);
            map.put("colaborador", colabMap);
            
            result.add(map);
        }
        return result;
    }

    public CrmNota saveNota(CrmNota nota) {
        nota.setFechaCreacion(LocalDateTime.now());
        return crmNotaRepository.save(nota);
    }
}
