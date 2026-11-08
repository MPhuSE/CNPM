package com.flowops.workflowconfig.api;

import com.flowops.shared.infrastructure.SecurityUtils;
import com.flowops.workflowconfig.api.dto.WorkflowVersionDetailDto;
import com.flowops.workflowconfig.application.WorkflowConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflow-versions")
@Tag(name = "Workflow Versions", description = "Quản lý chi tiết phiên bản quy trình, xuất bản và lưu trữ")
public class WorkflowVersionController {
    public WorkflowVersionController(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
    }


    private final WorkflowConfigService workflowConfigService;

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một phiên bản quy trình")
    public ResponseEntity<WorkflowVersionDetailDto> getVersionDetail(@PathVariable Long id) {
        return ResponseEntity.ok(workflowConfigService.getVersionDetail(id));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Xuất bản phiên bản quy trình (Publish DRAFT -> PUBLISHED)")
    public ResponseEntity<Void> publishVersion(@PathVariable Long id) {
        Long adminUserId = SecurityUtils.getRequiredCurrentUserId();
        workflowConfigService.publishVersion(id, adminUserId);
        return ResponseEntity.ok().build();
    }

}
