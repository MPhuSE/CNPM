package com.flowops.requests.api;

import com.flowops.requests.api.dto.*;
import com.flowops.requests.application.RequestService;
import com.flowops.requests.domain.RequestStatus;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.infrastructure.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "Requests", description = "Quản lý vòng đời hồ sơ: Tạo nháp, Gửi, Bổ sung, Hủy")
public class RequestController {
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }


    private final RequestService requestService;

    @GetMapping
    @Operation(summary = "Lấy danh sách hồ sơ của người dùng hiện tại")
    public ResponseEntity<PageResponse<RequestSummaryDto>> getMyRequests(
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long requesterId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(requestService.getMyRequests(requesterId, status, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết một hồ sơ")
    public ResponseEntity<RequestDetailDto> getRequestDetail(@PathVariable Long id) {
        Long callerId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(requestService.getRequestDetail(id, callerId));
    }

    @PostMapping
    @Operation(summary = "Tạo bản nháp hồ sơ mới (DRAFT)")
    public ResponseEntity<RequestDetailDto> createDraft(@Valid @RequestBody CreateDraftRequest request) {
        Long requesterId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.createDraft(request, requesterId));
    }

    @PatchMapping("/{id}/draft")
    @Operation(summary = "Cập nhật bản nháp hồ sơ")
    public ResponseEntity<RequestDetailDto> updateDraft(
            @PathVariable Long id,
            @RequestBody UpdateDraftDto dto
    ) {
        Long requesterId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(requestService.updateDraft(id, dto, requesterId));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Gửi hồ sơ lần đầu để phê duyệt")
    public ResponseEntity<RequestDetailDto> submitRequest(
            @PathVariable Long id,
            @RequestBody SubmitRequestDto dto
    ) {
        Long requesterId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(requestService.submitRequest(id, dto, requesterId));
    }

    @PostMapping("/{id}/resubmit")
    @Operation(summary = "Gửi lại hồ sơ sau khi bổ sung (RETURNED -> PENDING)")
    public ResponseEntity<RequestDetailDto> resubmitRequest(
            @PathVariable Long id,
            @RequestBody ResubmitRequestDto dto
    ) {
        Long requesterId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(requestService.resubmitRequest(id, dto, requesterId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Hủy hồ sơ")
    public ResponseEntity<RequestDetailDto> cancelRequest(
            @PathVariable Long id,
            @Valid @RequestBody CancelRequestDto dto
    ) {
        Long requesterId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(requestService.cancelRequest(id, dto, requesterId));
    }

}
