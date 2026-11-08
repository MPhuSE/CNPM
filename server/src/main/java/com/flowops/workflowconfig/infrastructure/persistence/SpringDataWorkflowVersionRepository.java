package com.flowops.workflowconfig.infrastructure.persistence;

import com.flowops.workflowconfig.domain.VersionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataWorkflowVersionRepository extends JpaRepository<WorkflowVersionJpaEntity, Long> {
    Optional<WorkflowVersionJpaEntity> findByWorkflowDefinitionIdAndVersionNo(Long definitionId, Integer versionNo);
    Optional<WorkflowVersionJpaEntity> findByWorkflowDefinitionIdAndStatus(Long definitionId, VersionStatus status);
    List<WorkflowVersionJpaEntity> findByStatus(VersionStatus status);
}
