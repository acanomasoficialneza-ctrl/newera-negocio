package com.newera.negocio.repositories;

import com.newera.negocio.models.DocumentoLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DocumentoLegalRepository extends JpaRepository<DocumentoLegal, Integer> {
    List<DocumentoLegal> findByActivoTrueOrderByIdDocumentoAsc();
    Optional<DocumentoLegal> findByCodigoAndActivoTrue(String codigo);
}
