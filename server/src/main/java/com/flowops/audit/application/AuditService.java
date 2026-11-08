package com.flowops.audit.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.audit.api.dto.AuditLogDto;
import com.flowops.audit.infrastructure.persistence.AuditLogJpaEntity;
import com.flowops.audit.infrastructure.persistence.SpringDataAuditLogRepository;
import com.flowops.shared.application.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    public AuditService(SpringDataAuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }


    private final SpringDataAuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogDto> getAuditLogs(int page, int size) {
        Page<AuditLogJpaEntity> logPage = auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        return PageResponse.of(
                logPage.getContent().stream().map(this::toDto).toList(),
                logPage.getNumber(),
                logPage.getSize(),
                logPage.getTotalElements()
        );
    }

    private AuditLogDto toDto(AuditLogJpaEntity a) {
        Object parsedDetails = parseJson(a.getDetails());
        return AuditLogDto.builder()
                .id(String.valueOf(a.getId()))
                .eventKey(a.getEventKey().toString())
                .actorName(a.getActor() != null ? a.getActor().getFullName() : "Hệ thống")
                .action(a.getAction())
                .entityType(a.getEntityType())
                .entityId(a.getEntityId())
                .details(parsedDetails)
                .ipAddress(a.getIpAddress())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (Exception e) {
            return json;
        }
    }

}
