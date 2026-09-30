package com.newera.negocio.repositories;

import com.newera.negocio.models.CrmNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmNotaRepository extends JpaRepository<CrmNota, Integer> {
    List<CrmNota> findByClienteIdUsuario(Integer idCliente);
}
