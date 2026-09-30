package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "usuarios_auth")
public class UsuariosAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "correo", unique = true, nullable = false)
    private String correo;

    @Column(name = "pass", nullable = false)
    private String pass;

    @Column(name = "rol", nullable = false)
    private String rol;

    @Column(name = "estado")
    private String estado;
}
