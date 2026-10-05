package com.newera.negocio.services;

import com.newera.negocio.dto.DashboardGlobalDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import com.newera.negocio.dto.DashboardChartDTO;

@Service
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    public DashboardGlobalDTO getGlobalStats() {
        DashboardGlobalDTO dto = new DashboardGlobalDTO();

        // 1. AUM (Activos bajo manejo)
        BigDecimal aum = (BigDecimal) entityManager.createNativeQuery(
                "SELECT SUM(balance) FROM newera.perfil_cliente"
        ).getSingleResult();
        dto.setAum(Optional.ofNullable(aum).orElse(BigDecimal.ZERO));

        // 2. Riesgo Vivo
        BigDecimal riesgo = (BigDecimal) entityManager.createNativeQuery(
                "SELECT SUM(margen) FROM newera.apuesta_cliente WHERE estatus_compra = 'ABIERTA'"
        ).getSingleResult();
        dto.setRiesgoVivo(Optional.ofNullable(riesgo).orElse(BigDecimal.ZERO));

        // 3. Volumen 24h
        BigDecimal volumen = (BigDecimal) entityManager.createNativeQuery(
                "SELECT SUM(monto_apuesta) FROM newera.apuesta_cliente WHERE fecha_creacion >= CURRENT_DATE - INTERVAL '1 day'"
        ).getSingleResult();
        dto.setVolumen24h(Optional.ofNullable(volumen).orElse(BigDecimal.ZERO));

        // 4. Flujo Neto 30D (Depositos - Retiros)
        BigDecimal depositos = (BigDecimal) entityManager.createNativeQuery(
                "SELECT SUM(monto) FROM newera.transacciones_caja WHERE tipo_transaccion = 'DEPOSITO' AND estatus = 'APROBADO' AND fecha_resolucion >= CURRENT_DATE - INTERVAL '30 days'"
        ).getSingleResult();
        BigDecimal retiros = (BigDecimal) entityManager.createNativeQuery(
                "SELECT SUM(monto) FROM newera.transacciones_caja WHERE tipo_transaccion = 'RETIRO' AND estatus = 'APROBADO' AND fecha_resolucion >= CURRENT_DATE - INTERVAL '30 days'"
        ).getSingleResult();
        BigDecimal flujoNeto = Optional.ofNullable(depositos).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(retiros).orElse(BigDecimal.ZERO));
        dto.setFlujoNeto30d(flujoNeto);

        // 5. Staff Activos
        Number staff = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM newera.perfil_admin"
        ).getSingleResult();
        dto.setStaffActivos(staff != null ? staff.longValue() : 0L);

        // 6. Conversión FTD %
        Number clientesFondeados = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT id_usuario) FROM newera.transacciones_caja WHERE tipo_transaccion = 'DEPOSITO' AND estatus = 'APROBADO'"
        ).getSingleResult();
        Number totalClientes = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM newera.perfil_cliente"
        ).getSingleResult();
        
        long totalC = totalClientes != null ? totalClientes.longValue() : 0L;
        long fondeados = clientesFondeados != null ? clientesFondeados.longValue() : 0L;
        if (totalC > 0) {
            dto.setConversionFtd((double) fondeados * 100.0 / totalC);
        } else {
            dto.setConversionFtd(0.0);
        }

        // 7. Usuarios Totales
        dto.setUsuariosTotales(totalC);

        // 8. Retiros en cola (Count y Monto)
        Object[] retirosCola = (Object[]) entityManager.createNativeQuery(
                "SELECT COUNT(*), SUM(monto) FROM newera.transacciones_caja WHERE tipo_transaccion = 'RETIRO' AND estatus = 'PENDIENTE'"
        ).getSingleResult();
        if (retirosCola != null) {
            dto.setRetirosEnCola(retirosCola[0] != null ? ((Number) retirosCola[0]).longValue() : 0L);
            dto.setRetirosEnColaMonto(retirosCola[1] != null ? (BigDecimal) retirosCola[1] : BigDecimal.ZERO);
        } else {
            dto.setRetirosEnCola(0L);
            dto.setRetirosEnColaMonto(BigDecimal.ZERO);
        }

        // 9. KYC Pendientes
        Number kyc = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM newera.perfil_cliente WHERE estado_kyc = 'PENDIENTE'"
        ).getSingleResult();
        dto.setKycPendientes(kyc != null ? kyc.longValue() : 0L);

        // 10. Alertas Seguridad (Hoy)
        Number alertas = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM newera.auditoria WHERE nivel IN ('WARN', 'ERROR') AND fecha_evento >= CURRENT_DATE"
        ).getSingleResult();
        dto.setAlertasSeguridad(alertas != null ? alertas.longValue() : 0L);

        return dto;
    }

    public List<DashboardChartDTO> getChartData(int days) {
        // 1. Initial balance up to the start of the window
        String initialQuery = "SELECT SUM(CASE WHEN tipo_transaccion = 'DEPOSITO' THEN monto ELSE -monto END) " +
                              "FROM newera.transacciones_caja " +
                              "WHERE estatus = 'APROBADO' AND fecha_resolucion < CURRENT_DATE - CAST(:days || ' days' AS INTERVAL)";
        BigDecimal initialBalance = (BigDecimal) entityManager.createNativeQuery(initialQuery)
                .setParameter("days", days)
                .getSingleResult();
        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }

        // 2. Fetch all individual approved transactions in the window
        String queryStr = "SELECT EXTRACT(EPOCH FROM fecha_resolucion) as time_epoch, " +
                          "CASE WHEN tipo_transaccion = 'DEPOSITO' THEN monto ELSE -monto END as value " +
                          "FROM newera.transacciones_caja " +
                          "WHERE estatus = 'APROBADO' AND fecha_resolucion >= CURRENT_DATE - CAST(:days || ' days' AS INTERVAL) " +
                          "ORDER BY fecha_resolucion ASC";
                          
        List<Object[]> results = entityManager.createNativeQuery(queryStr)
                .setParameter("days", days)
                .getResultList();

        List<DashboardChartDTO> chartData = new ArrayList<>();
        BigDecimal cumulative = initialBalance;
        
        long lastTime = 0;

        for (Object[] row : results) {
            Number epochNum = (Number) row[0];
            long time = epochNum.longValue();
            // Lightweight charts needs unique strictly ascending time values
            if (time <= lastTime) {
                time = lastTime + 1;
            }
            lastTime = time;

            BigDecimal txValue = (BigDecimal) row[1];
            cumulative = cumulative.add(txValue != null ? txValue : BigDecimal.ZERO);
            
            // To ensure the chart displays correctly, Lightweight Charts uses Unix timestamp in seconds for intraday
            chartData.add(new DashboardChartDTO(time, cumulative));
        }
        
        // If there is no data in the window, at least add a point for the start of the window so the line isn't empty
        if (chartData.isEmpty()) {
            long windowStart = System.currentTimeMillis() / 1000L - (days * 86400L);
            chartData.add(new DashboardChartDTO(windowStart, cumulative));
        }
        
        return chartData;
    }
}
