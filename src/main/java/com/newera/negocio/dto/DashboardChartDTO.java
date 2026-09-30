package com.newera.negocio.dto;

import java.math.BigDecimal;

public class DashboardChartDTO {
    private String time;
    private BigDecimal value;

    public DashboardChartDTO(String time, BigDecimal value) {
        this.time = time;
        this.value = value;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
}
