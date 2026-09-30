package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "crm_notas")
public class CrmNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nota")
    private Integer idNota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private UsuariosAuth cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_colaborador", nullable = false)
    private UsuariosAuth colaborador;

    @Column(name = "nota", nullable = false, columnDefinition = "TEXT")
    private String nota;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
}
