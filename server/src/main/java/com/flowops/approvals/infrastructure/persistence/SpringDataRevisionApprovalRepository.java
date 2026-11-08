package com.flowops.approvals.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataRevisionApprovalRepository extends JpaRepository<RevisionApprovalJpaEntity, RevisionApprovalJpaEntity.Pk> {
    List<RevisionApprovalJpaEntity> findByRevisionId(Long revisionId);
}
