package com.flowops.operations.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<WorkflowIncidentJpaEntity, Long> {
    Page<WorkflowIncidentJpaEntity> findByStatus(String status, Pageable pageable);
    Optional<WorkflowIncidentJpaEntity> findByRequestIdAndStatus(Long requestId, String status);
    long countByStatus(String status);
}
