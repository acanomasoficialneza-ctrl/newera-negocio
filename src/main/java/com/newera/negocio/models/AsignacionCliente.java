package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "asignacion_clientes")
public class AsignacionCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Integer idAsignacion;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(name = "id_admin")
    private Integer idAdmin;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;
}
