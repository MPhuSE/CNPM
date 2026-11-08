package com.flowops.organization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataApprovalGroupRepository extends JpaRepository<ApprovalGroupJpaEntity, Long> {
    Optional<ApprovalGroupJpaEntity> findByCode(String code);
}
