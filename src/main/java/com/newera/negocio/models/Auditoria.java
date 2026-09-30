package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Integer idAuditoria;

    @Column(name = "nivel", length = 20, nullable = false)
    private String nivel; // INFO, WARN, ERROR, AUTH

    @Column(name = "mensaje", columnDefinition = "TEXT", nullable = false)
    private String mensaje;

    @Column(name = "direccion_ip", length = 45, nullable = false)
    private String direccionIp;

    @Column(name = "fecha_evento")
    private LocalDateTime fechaEvento;

    @Column(name = "id_usuario_responsable")
    private Integer idUsuarioResponsable;
}
