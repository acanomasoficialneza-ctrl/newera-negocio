package com.newera.negocio.repositories;

import com.newera.negocio.models.TransaccionCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionCajaRepository extends JpaRepository<TransaccionCaja, Integer> {
    List<TransaccionCaja> findByUsuarioIdUsuario(Integer idUsuario);
    List<TransaccionCaja> findByEstatus(String estatus);
    List<TransaccionCaja> findByEstatusNot(String estatus);
}
