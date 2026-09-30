package com.newera.negocio.controllers;

import com.newera.negocio.dto.DashboardGlobalDTO;
import com.newera.negocio.dto.DashboardChartDTO;
import com.newera.negocio.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/global")
    public ResponseEntity<DashboardGlobalDTO> getGlobalStats() {
        return ResponseEntity.ok(dashboardService.getGlobalStats());
    }

    @GetMapping("/chart-data")
    public ResponseEntity<List<DashboardChartDTO>> getChartData(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getChartData(days));
    }
}
