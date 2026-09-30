package com.newera.negocio.dto;

import java.math.BigDecimal;

public class DashboardGlobalDTO {
    private BigDecimal aum;
    private BigDecimal riesgoVivo;
    private BigDecimal volumen24h;
    private BigDecimal flujoNeto30d;
    private Long staffActivos;
    private Double conversionFtd;
    private Long usuariosTotales;
    private Long retirosEnCola;
    private BigDecimal retirosEnColaMonto;
    private Long kycPendientes;
    private Long alertasSeguridad;

    public BigDecimal getAum() { return aum; }
    public void setAum(BigDecimal aum) { this.aum = aum; }
    public BigDecimal getRiesgoVivo() { return riesgoVivo; }
    public void setRiesgoVivo(BigDecimal riesgoVivo) { this.riesgoVivo = riesgoVivo; }
    public BigDecimal getVolumen24h() { return volumen24h; }
    public void setVolumen24h(BigDecimal volumen24h) { this.volumen24h = volumen24h; }
    public BigDecimal getFlujoNeto30d() { return flujoNeto30d; }
    public void setFlujoNeto30d(BigDecimal flujoNeto30d) { this.flujoNeto30d = flujoNeto30d; }
    public Long getStaffActivos() { return staffActivos; }
    public void setStaffActivos(Long staffActivos) { this.staffActivos = staffActivos; }
    public Double getConversionFtd() { return conversionFtd; }
    public void setConversionFtd(Double conversionFtd) { this.conversionFtd = conversionFtd; }
    public Long getUsuariosTotales() { return usuariosTotales; }
    public void setUsuariosTotales(Long usuariosTotales) { this.usuariosTotales = usuariosTotales; }
    public Long getRetirosEnCola() { return retirosEnCola; }
    public void setRetirosEnCola(Long retirosEnCola) { this.retirosEnCola = retirosEnCola; }
    public BigDecimal getRetirosEnColaMonto() { return retirosEnColaMonto; }
    public void setRetirosEnColaMonto(BigDecimal retirosEnColaMonto) { this.retirosEnColaMonto = retirosEnColaMonto; }
    public Long getKycPendientes() { return kycPendientes; }
    public void setKycPendientes(Long kycPendientes) { this.kycPendientes = kycPendientes; }
    public Long getAlertasSeguridad() { return alertasSeguridad; }
    public void setAlertasSeguridad(Long alertasSeguridad) { this.alertasSeguridad = alertasSeguridad; }
}
