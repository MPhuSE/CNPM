package com.flowops.audit.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataRequestEventRepository extends JpaRepository<RequestEventJpaEntity, Long> {
    List<RequestEventJpaEntity> findByRequestIdOrderByCreatedAtAsc(Long requestId);
    Page<RequestEventJpaEntity> findByRequestIdOrderByCreatedAtDesc(Long requestId, Pageable pageable);
}
