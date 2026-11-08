package com.flowops.audit.api;

import com.flowops.audit.api.dto.AuditLogDto;
import com.flowops.audit.application.AuditService;
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
@RequestMapping("/api/v1/audit-logs")
@Tag(name = "Audit Logs", description = "Nhật ký kiểm toán hệ thống (Dành cho Admin)")
public class AuditController {
    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }


    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách nhật ký kiểm toán bất biến")
    public ResponseEntity<PageResponse<AuditLogDto>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auditService.getAuditLogs(page, size));
    }

}
