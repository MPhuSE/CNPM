package com.flowops.workflowconfig.api;

import com.flowops.shared.application.dto.PageResponse;
import com.flowops.workflowconfig.api.dto.WorkflowSummaryDto;
import com.flowops.workflowconfig.application.WorkflowConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/workflows")
@Tag(name = "Workflows", description = "Quản lý danh mục và cấu hình quy trình")
public class WorkflowController {
    public WorkflowController(WorkflowConfigService workflowConfigService) {
        this.workflowConfigService = workflowConfigService;
    }


    private final WorkflowConfigService workflowConfigService;

    @GetMapping("/available")
    @Operation(summary = "Lấy danh sách các quy trình đang có phiên bản PUBLISHED để nhân viên tạo hồ sơ")
    public ResponseEntity<List<WorkflowSummaryDto>> getAvailableWorkflows() {
        return ResponseEntity.ok(workflowConfigService.getAvailableWorkflows());
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả danh mục quy trình (Dành cho Admin)")
    public ResponseEntity<PageResponse<WorkflowSummaryDto>> getAllWorkflows(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(workflowConfigService.getAllWorkflows(page, size));
    }

}
