package com.flowops.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long>, JpaSpecificationExecutor<UserJpaEntity> {
    Optional<UserJpaEntity> findByUsername(String username);
    @org.springframework.data.jpa.repository.Query("select u.id from UserJpaEntity u where u.username = :username")
    Optional<Long> findIdByUsername(String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserJpaEntity u where u.id = :id")
    Optional<UserJpaEntity> findByIdForUpdate(Long id);
    Optional<UserJpaEntity> findByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmailIgnoreCase(String email);
}
