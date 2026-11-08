package com.flowops.requests.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringDataRequestRevisionRepository extends JpaRepository<RequestRevisionJpaEntity, Long> {
    List<RequestRevisionJpaEntity> findByRequestIdOrderByRevisionNoDesc(Long requestId);
    Optional<RequestRevisionJpaEntity> findByRequestIdAndRevisionNo(Long requestId, Integer revisionNo);
}
