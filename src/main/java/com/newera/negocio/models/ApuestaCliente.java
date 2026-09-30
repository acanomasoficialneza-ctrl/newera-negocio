package com.newera.negocio.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "apuesta_cliente")
public class ApuestaCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_apuesta_cliente")
    private Integer idApuestaCliente;

    @Column(name = "tipo_compra", length = 50)
    private String tipoCompra;

    @Column(name = "compra", length = 50)
    private String compra;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "valor_unidad")
    private BigDecimal valorUnidad;

    @Column(name = "monto_apuesta")
    private BigDecimal montoApuesta;

    @Column(name = "variacion")
    private BigDecimal variacion;

    @Column(name = "ganancia_perdida")
    private BigDecimal gananciaPerdida;

    @Column(name = "estatus_compra", length = 50)
    private String estatusCompra;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private UsuariosAuth usuario;

    @Column(name = "unidades")
    private BigDecimal unidades;

    @Column(name = "stop_loss")
    private BigDecimal stopLoss;

    @Column(name = "take_profit")
    private BigDecimal takeProfit;

    @Column(name = "margen")
    private BigDecimal margen;

    @Column(name = "swap")
    private BigDecimal swap;
}
