package com.newera.negocio.repositories;

import com.newera.negocio.models.ApuestaCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApuestaClienteRepository extends JpaRepository<ApuestaCliente, Integer> {
    List<ApuestaCliente> findByUsuarioIdUsuario(Integer idUsuario);
    List<ApuestaCliente> findByUsuarioIdUsuarioAndEstatusCompra(Integer idUsuario, String estatusCompra);
}
