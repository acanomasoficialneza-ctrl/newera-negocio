package com.newera.negocio.repositories;

import com.newera.negocio.models.AsignacionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionClienteRepository extends JpaRepository<AsignacionCliente, Integer> {
    List<AsignacionCliente> findByIdAdmin(Integer idAdmin);
    List<AsignacionCliente> findByIdCliente(Integer idCliente);
    
    @org.springframework.transaction.annotation.Transactional
    void deleteByIdClienteAndIdAdmin(Integer idCliente, Integer idAdmin);
}
