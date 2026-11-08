package com.flowops.notifications.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {
    Page<NotificationJpaEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    boolean existsByEventKeyAndUserIdAndNotificationType(UUID eventKey, Long userId, String notificationType);
    boolean existsByTaskIdAndUserIdAndNotificationType(Long taskId, Long userId, String notificationType);
}
