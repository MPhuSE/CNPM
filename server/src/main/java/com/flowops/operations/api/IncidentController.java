package com.flowops.operations.api;

import com.flowops.operations.api.dto.IncidentDto;
import com.flowops.operations.application.IncidentService;
import com.flowops.shared.application.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Quản lý sự cố vận hành hệ thống (Dành cho Admin)")
public class IncidentController {
    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }


    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách các sự cố vận hành")
    public ResponseEntity<PageResponse<IncidentDto>> getIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(incidentService.getIncidents(status, page, size));
    }

}
