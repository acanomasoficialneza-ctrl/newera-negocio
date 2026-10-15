package com.newera.negocio.repositories;

import com.newera.negocio.models.ApuestaCliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApuestaClienteRepository extends JpaRepository<ApuestaCliente, Integer> {
    List<ApuestaCliente> findByUsuarioIdUsuario(Integer idUsuario);
    List<ApuestaCliente> findByUsuarioIdUsuarioAndEstatusCompra(Integer idUsuario, String estatusCompra);

    Page<ApuestaCliente> findByUsuarioIdUsuario(Integer idUsuario, Pageable pageable);
    Page<ApuestaCliente> findByUsuarioIdUsuarioAndEstatusCompra(Integer idUsuario, String estatusCompra, Pageable pageable);
}
