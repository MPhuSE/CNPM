package com.flowops.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserSessionRepository extends JpaRepository<UserSessionJpaEntity, UUID> {
    Optional<UserSessionJpaEntity> findByTokenHash(String tokenHash);
}
