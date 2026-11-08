package com.flowops.notifications.api;

import com.flowops.notifications.api.dto.NotificationDto;
import com.flowops.notifications.application.NotificationService;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.infrastructure.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Trung tâm thông báo in-app")
public class NotificationController {
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách thông báo của người dùng hiện tại")
    public ResponseEntity<PageResponse<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        return ResponseEntity.ok(notificationService.getNotifications(userId, page, size));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu thông báo đã đọc")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long userId = SecurityUtils.getRequiredCurrentUserId();
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

}
