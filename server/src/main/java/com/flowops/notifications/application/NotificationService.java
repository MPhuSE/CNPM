package com.flowops.notifications.application;

import com.flowops.notifications.api.dto.NotificationDto;
import com.flowops.notifications.infrastructure.persistence.NotificationJpaEntity;
import com.flowops.notifications.infrastructure.persistence.SpringDataNotificationRepository;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.domain.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class NotificationService {
    public NotificationService(SpringDataNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }


    private final SpringDataNotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getNotifications(Long userId, int page, int size) {
        Page<NotificationJpaEntity> notifPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return PageResponse.of(
                notifPage.getContent().stream().map(this::toDto).toList(),
                notifPage.getNumber(),
                notifPage.getSize(),
                notifPage.getTotalElements()
        );
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        NotificationJpaEntity notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Thông báo", notificationId));

        if (notif.getReadAt() == null) {
            notif.setReadAt(Instant.now());
            notificationRepository.save(notif);
        }
    }

    private NotificationDto toDto(NotificationJpaEntity n) {
        return NotificationDto.builder()
                .id(String.valueOf(n.getId()))
                .eventKey(n.getEventKey().toString())
                .type(n.getNotificationType())
                .title(n.getTitle())
                .message(n.getMessage())
                .requestId(n.getRequest() != null ? String.valueOf(n.getRequest().getId()) : null)
                .taskId(n.getTaskId() != null ? String.valueOf(n.getTaskId()) : null)
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }

}
