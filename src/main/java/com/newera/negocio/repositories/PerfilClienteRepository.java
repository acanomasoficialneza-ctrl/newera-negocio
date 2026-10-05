package com.newera.negocio.repositories;

import com.newera.negocio.models.PerfilCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PerfilClienteRepository extends JpaRepository<PerfilCliente, Integer> {
    boolean existsByTelefono(String telefono);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PerfilCliente p WHERE p.idUsuario = :id")
    Optional<PerfilCliente> findByIdLocked(@Param("id") Integer id);
}
