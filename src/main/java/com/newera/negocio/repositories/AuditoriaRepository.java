package com.newera.negocio.repositories;

import com.newera.negocio.models.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    List<Auditoria> findByFechaEventoBetween(LocalDateTime start, LocalDateTime end);
}
