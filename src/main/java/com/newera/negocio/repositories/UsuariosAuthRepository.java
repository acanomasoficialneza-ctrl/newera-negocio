package com.newera.negocio.repositories;

import com.newera.negocio.models.UsuariosAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuariosAuthRepository extends JpaRepository<UsuariosAuth, Integer> {
    List<UsuariosAuth> findByRol(String rol);
}
