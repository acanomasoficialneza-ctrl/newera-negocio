package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transacciones_caja")
public class TransaccionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Integer idTransaccion;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuariosAuth usuario;

    @Column(name = "tipo_transaccion", length = 20, nullable = false)
    private String tipoTransaccion; // DEPOSITO, RETIRO

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "detalles_cuenta", length = 255)
    private String detallesCuenta;

    @Column(name = "monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "referencia", length = 255)
    private String referencia;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "estatus", length = 20)
    private String estatus; // PENDIENTE, APROBADO, RECHAZADO

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin_aprobador")
    private UsuariosAuth adminAprobador;

    @Column(name = "url_comprobante")
    private String urlComprobante;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Transient
    private Integer idAdmin;
}
