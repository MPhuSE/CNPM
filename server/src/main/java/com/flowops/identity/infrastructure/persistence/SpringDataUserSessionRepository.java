package com.flowops.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserSessionRepository extends JpaRepository<UserSessionJpaEntity, UUID> {
    Optional<UserSessionJpaEntity> findByTokenHash(String tokenHash);
    @Query("select s.user.id from UserSessionJpaEntity s where s.id = :id")
    Optional<Long> findUserIdBySessionId(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSessionJpaEntity s where s.id = :id")
    Optional<UserSessionJpaEntity> findByIdForUpdate(UUID id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSessionJpaEntity s where s.user.id = :userId order by s.createdAt, s.id")
    List<UserSessionJpaEntity> findByUserIdForUpdate(Long userId);
}
