package com.flowops.organization.api;

import com.flowops.organization.api.dto.ApprovalGroupDto;
import com.flowops.organization.application.ApprovalGroupService;
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
@RequestMapping("/api/v1/approval-groups")
@Tag(name = "Approval Groups", description = "Quản lý nhóm phê duyệt độc lập (Finance, Accounting, IT)")
public class ApprovalGroupController {
    public ApprovalGroupController(ApprovalGroupService approvalGroupService) {
        this.approvalGroupService = approvalGroupService;
    }


    private final ApprovalGroupService approvalGroupService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Lấy danh sách nhóm phê duyệt")
    public ResponseEntity<PageResponse<ApprovalGroupDto>> getApprovalGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(approvalGroupService.getApprovalGroups(page, size));
    }

}
