package com.flowops.reporting.api;

import com.flowops.reporting.api.dto.DashboardMetricsDto;
import com.flowops.reporting.application.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Số liệu vận hành và báo cáo tổng quan (Dành cho Admin)")
public class DashboardController {
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }


    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy tổng hợp số liệu vận hành và SLA")
    public ResponseEntity<DashboardMetricsDto> getDashboardMetrics() {
        return ResponseEntity.ok(dashboardService.getMetrics());
    }

}
