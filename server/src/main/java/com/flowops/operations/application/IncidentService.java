package com.flowops.operations.application;

import com.flowops.operations.api.dto.IncidentDto;
import com.flowops.operations.infrastructure.persistence.SpringDataIncidentRepository;
import com.flowops.operations.infrastructure.persistence.WorkflowIncidentJpaEntity;
import com.flowops.shared.application.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {
    public IncidentService(SpringDataIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }


    private final SpringDataIncidentRepository incidentRepository;

    @Transactional(readOnly = true)
    public PageResponse<IncidentDto> getIncidents(String status, int page, int size) {
        Page<WorkflowIncidentJpaEntity> incidentPage = status != null && !status.isBlank()
                ? incidentRepository.findByStatus(status, PageRequest.of(page, size))
                : incidentRepository.findAll(PageRequest.of(page, size));

        return PageResponse.of(
                incidentPage.getContent().stream().map(this::toDto).toList(),
                incidentPage.getNumber(),
                incidentPage.getSize(),
                incidentPage.getTotalElements()
        );
    }

    private IncidentDto toDto(WorkflowIncidentJpaEntity inc) {
        return IncidentDto.builder()
                .id(String.valueOf(inc.getId()))
                .requestId(String.valueOf(inc.getRequest().getId()))
                .revisionId(String.valueOf(inc.getRevision().getId()))
                .incidentType(inc.getIncidentType())
                .status(inc.getStatus())
                .errorCode(inc.getErrorCode())
                .details(inc.getDetails())
                .openedAt(inc.getOpenedAt())
                .closedAt(inc.getClosedAt())
                .build();
    }

}
