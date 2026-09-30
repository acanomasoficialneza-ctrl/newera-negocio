package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "perfil_cliente")
public class PerfilCliente {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    @MapsId
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UsuariosAuth usuarioAuth;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column(name = "nombre_pila")
    private String nombrePila;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private java.sql.Date fechaNacimiento;

    @Column(name = "estado_residencia")
    private String estadoResidencia;

    @Column(name = "profesion")
    private String profesion;

    @Column(name = "experiencia_trading")
    private String experienciaTrading;

    @Column(name = "hobbie")
    private String hobbie;

    @Column(name = "url_foto_perfil")
    private String urlFotoPerfil;

    @Column(name = "url_ine_frente")
    private String urlIneFrente;

    @Column(name = "url_ine_reverso")
    private String urlIneReverso;

    @Column(name = "estado_kyc")
    private String estadoKyc;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "margen_utilizado")
    private BigDecimal margenUtilizado;

    @Column(name = "margen_libre")
    private BigDecimal margenLibre;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
}
