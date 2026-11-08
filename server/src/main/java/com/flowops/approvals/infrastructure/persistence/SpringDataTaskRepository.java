package com.flowops.approvals.infrastructure.persistence;

import com.flowops.approvals.domain.AssignmentState;
import com.flowops.approvals.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataTaskRepository extends JpaRepository<TaskJpaEntity, Long> {
    Page<TaskJpaEntity> findByAssigneeIdAndStatus(Long assigneeId, TaskStatus status, Pageable pageable);
    
    @Query("SELECT t FROM TaskJpaEntity t WHERE t.assignmentState = :assignmentState AND t.status = :status AND t.candidateGroup.id IN :groupIds")
    Page<TaskJpaEntity> findGroupOpenTasks(AssignmentState assignmentState, TaskStatus status, List<Long> groupIds, Pageable pageable);

    Page<TaskJpaEntity> findByAssignmentStateAndStatus(AssignmentState assignmentState, TaskStatus status, Pageable pageable);

    Optional<TaskJpaEntity> findByRequestIdAndStatus(Long requestId, TaskStatus status);

    long countByStatus(TaskStatus status);
    long countByAssignmentStateAndStatus(AssignmentState state, TaskStatus status);

    @Query("SELECT COUNT(t) FROM TaskJpaEntity t WHERE t.status = 'PENDING' AND t.dueAt IS NOT NULL AND t.dueAt < :now")
    long countOverdueTasks(Instant now);

    @Query("SELECT t FROM TaskJpaEntity t WHERE t.status = 'PENDING' AND t.dueAt IS NOT NULL AND t.dueAt < :now")
    List<TaskJpaEntity> findOverdueTasks(Instant now);
}
