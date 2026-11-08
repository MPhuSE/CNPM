package com.flowops.workflowconfig.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataWorkflowStepRepository extends JpaRepository<WorkflowStepJpaEntity, Long> {
    List<WorkflowStepJpaEntity> findByWorkflowVersionIdOrderByDisplayOrderAsc(Long workflowVersionId);
    Optional<WorkflowStepJpaEntity> findByWorkflowVersionIdAndStepKey(Long workflowVersionId, String stepKey);
}
