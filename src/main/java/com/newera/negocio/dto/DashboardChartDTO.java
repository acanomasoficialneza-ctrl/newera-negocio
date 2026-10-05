package com.newera.negocio.dto;

import java.math.BigDecimal;

public class DashboardChartDTO {
    private Long time;
    private BigDecimal value;

    public DashboardChartDTO(Long time, BigDecimal value) {
        this.time = time;
        this.value = value;
    }

    public Long getTime() { return time; }
    public void setTime(Long time) { this.time = time; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
