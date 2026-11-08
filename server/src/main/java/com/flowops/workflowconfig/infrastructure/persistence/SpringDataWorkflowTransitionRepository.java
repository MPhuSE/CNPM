package com.flowops.workflowconfig.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataWorkflowTransitionRepository extends JpaRepository<WorkflowTransitionJpaEntity, Long> {
    List<WorkflowTransitionJpaEntity> findByWorkflowVersionId(Long workflowVersionId);
    List<WorkflowTransitionJpaEntity> findByFromStepIdOrderByPriorityAsc(Long fromStepId);
}
