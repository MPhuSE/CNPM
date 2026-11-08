package com.flowops.workflowconfig.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataWorkflowDefinitionRepository extends JpaRepository<WorkflowDefinitionJpaEntity, Long> {
    Optional<WorkflowDefinitionJpaEntity> findByCode(String code);
}
