package com.flowops.requests.infrastructure.persistence;

import com.flowops.requests.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataRequestRepository extends JpaRepository<RequestJpaEntity, Long> {
    Page<RequestJpaEntity> findByRequesterId(Long requesterId, Pageable pageable);
    Page<RequestJpaEntity> findByRequesterIdAndStatus(Long requesterId, RequestStatus status, Pageable pageable);
    Optional<RequestJpaEntity> findByRequestCode(String requestCode);
    long countByStatus(RequestStatus status);
    long countByFirstSubmittedAtIsNotNull();
}
