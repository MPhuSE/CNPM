package com.flowops.approvals.api;

import com.flowops.approvals.api.dto.*;
import com.flowops.approvals.application.TaskService;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.infrastructure.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Quản lý hộp thư duyệt (Inbox), nhận việc (Claim) và quyết định phê duyệt")
public class TaskController {
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    private final TaskService taskService;

    @GetMapping
    @Operation(summary = "Lấy danh sách nhiệm vụ phê duyệt (scope=mine hoặc scope=group)")
    public ResponseEntity<PageResponse<TaskSummaryDto>> getTasks(
            @RequestParam(defaultValue = "mine") String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long callerId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(taskService.getTasks(scope, callerId, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một nhiệm vụ duyệt")
    public ResponseEntity<TaskDetailDto> getTaskDetail(@PathVariable Long id) {
        Long callerId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(taskService.getTaskDetail(id, callerId));
    }

    @PostMapping("/{id}/claim")
    @Operation(summary = "Nhận việc từ nhiệm vụ nhóm (GROUP_OPEN -> ASSIGNED)")
    public ResponseEntity<TaskDetailDto> claimTask(
            @PathVariable Long id,
            @RequestBody ClaimTaskRequest req
    ) {
        Long callerId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(taskService.claimTask(id, req, callerId));
    }

    @PostMapping("/{id}/decisions")
    @Operation(summary = "Thực hiện quyết định phê duyệt (APPROVE, REJECT, REQUEST_CHANGES)")
    public ResponseEntity<Void> decideTask(
            @PathVariable Long id,
            @Valid @RequestBody DecisionTaskRequest req
    ) {
        Long callerId = SecurityUtils.getRequiredCurrentUserId();
        taskService.decideTask(id, req, callerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reassign")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Phân công lại người duyệt (Dành cho Admin)")
    public ResponseEntity<Void> reassignTask(
            @PathVariable Long id,
            @Valid @RequestBody ReassignTaskRequest req
    ) {
        Long adminId = SecurityUtils.getRequiredCurrentUserId();
        taskService.reassignTask(id, req, adminId);
        return ResponseEntity.ok().build();
    }

}
