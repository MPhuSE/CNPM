package com.flowops.operations.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataCommandReceiptRepository extends JpaRepository<CommandReceiptJpaEntity, Long> {
    Optional<CommandReceiptJpaEntity> findByActorIdAndCommandTypeAndClientKey(Long actorId, String commandType, UUID clientKey);
}
