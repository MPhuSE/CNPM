package com.flowops.notifications.application;

import com.flowops.approvals.infrastructure.persistence.SpringDataTaskRepository;
import com.flowops.approvals.infrastructure.persistence.TaskJpaEntity;
import com.flowops.notifications.infrastructure.persistence.NotificationJpaEntity;
import com.flowops.notifications.infrastructure.persistence.SpringDataNotificationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "flowops.scheduler.sla-check-enabled", havingValue = "true", matchIfMissing = true)
public class SlaNotificationScheduler {
    public SlaNotificationScheduler(SpringDataTaskRepository taskRepository, SpringDataNotificationRepository notificationRepository) {
        this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SlaNotificationScheduler.class);


    private final SpringDataTaskRepository taskRepository;
    private final SpringDataNotificationRepository notificationRepository;

    @Scheduled(cron = "${flowops.scheduler.cron:0 * * * * *}")
    @Transactional
    public void checkOverdueTasks() {
        Instant now = Instant.now();
        List<TaskJpaEntity> overdueTasks = taskRepository.findOverdueTasks(now);

        for (TaskJpaEntity task : overdueTasks) {
            if (task.getAssignee() != null) {
                boolean alreadySent = notificationRepository.existsByTaskIdAndUserIdAndNotificationType(
                        task.getId(),
                        task.getAssignee().getId(),
                        "TASK_OVERDUE"
                );

                if (!alreadySent) {
                    NotificationJpaEntity notif = new NotificationJpaEntity();
                    notif.setEventKey(UUID.randomUUID());
                    notif.setUser(task.getAssignee());
                    notif.setNotificationType("TASK_OVERDUE");
                    notif.setRequest(task.getRequest());
                    notif.setTaskId(task.getId());
                    notif.setTitle("Cảnh báo: Tác vụ quá hạn SLA");
                    notif.setMessage("Tác vụ '" + task.getWorkflowStep().getStepName() +
                            "' cho hồ sơ '" + task.getRequest().getTitle() + "' đã vượt quá thời hạn SLA.");
                    notificationRepository.save(notif);
                    log.info("Sent SLA overdue alert for task {} to user {}", task.getId(), task.getAssignee().getUsername());
                }
            }
        }
    }

}
