package com.flowops.requests.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataRequestEditRepository extends JpaRepository<RequestEditJpaEntity, Long> {
}
