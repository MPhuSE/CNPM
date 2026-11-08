package com.flowops.workflowconfig.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.identity.infrastructure.persistence.SpringDataUserRepository;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.domain.BusinessRuleException;
import com.flowops.shared.domain.EntityNotFoundException;
import com.flowops.workflowconfig.api.dto.WorkflowSummaryDto;
import com.flowops.workflowconfig.api.dto.WorkflowVersionDetailDto;
import com.flowops.workflowconfig.domain.VersionStatus;
import com.flowops.workflowconfig.infrastructure.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkflowConfigService {
    public WorkflowConfigService(SpringDataWorkflowDefinitionRepository definitionRepository, SpringDataWorkflowVersionRepository versionRepository, SpringDataWorkflowStepRepository stepRepository, SpringDataWorkflowTransitionRepository transitionRepository, SpringDataUserRepository userRepository, ObjectMapper objectMapper) {
        this.definitionRepository = definitionRepository;
        this.versionRepository = versionRepository;
        this.stepRepository = stepRepository;
        this.transitionRepository = transitionRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }


    private final SpringDataWorkflowDefinitionRepository definitionRepository;
    private final SpringDataWorkflowVersionRepository versionRepository;
    private final SpringDataWorkflowStepRepository stepRepository;
    private final SpringDataWorkflowTransitionRepository transitionRepository;
    private final SpringDataUserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<WorkflowSummaryDto> getAvailableWorkflows() {
        return versionRepository.findByStatus(VersionStatus.PUBLISHED).stream()
                .filter(v -> Boolean.TRUE.equals(v.getWorkflowDefinition().getIsActive()))
                .map(v -> WorkflowSummaryDto.builder()
                        .id(String.valueOf(v.getWorkflowDefinition().getId()))
                        .code(v.getWorkflowDefinition().getCode())
                        .name(v.getWorkflowDefinition().getName())
                        .description(v.getWorkflowDefinition().getDescription())
                        .isActive(v.getWorkflowDefinition().getIsActive())
                        .activeVersionNo(v.getVersionNo())
                        .activeVersionId(String.valueOf(v.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkflowSummaryDto> getAllWorkflows(int page, int size) {
        Page<WorkflowDefinitionJpaEntity> defPage = definitionRepository.findAll(PageRequest.of(page, size));
        List<WorkflowSummaryDto> items = defPage.getContent().stream()
                .map(def -> {
                    Optional<WorkflowVersionJpaEntity> published = versionRepository
                            .findByWorkflowDefinitionIdAndStatus(def.getId(), VersionStatus.PUBLISHED);
                    return WorkflowSummaryDto.builder()
                            .id(String.valueOf(def.getId()))
                            .code(def.getCode())
                            .name(def.getName())
                            .description(def.getDescription())
                            .isActive(def.getIsActive())
                            .activeVersionNo(published.map(WorkflowVersionJpaEntity::getVersionNo).orElse(null))
                            .activeVersionId(published.map(v -> String.valueOf(v.getId())).orElse(null))
                            .build();
                })
                .collect(Collectors.toList());

        return PageResponse.of(items, defPage.getNumber(), defPage.getSize(), defPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public WorkflowVersionDetailDto getVersionDetail(Long versionId) {
        WorkflowVersionJpaEntity version = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Phiên bản quy trình", versionId));

        Object parsedSchema = parseJson(version.getFormSchema());

        List<WorkflowStepJpaEntity> steps = stepRepository.findByWorkflowVersionIdOrderByDisplayOrderAsc(versionId);
        List<WorkflowTransitionJpaEntity> transitions = transitionRepository.findByWorkflowVersionId(versionId);

        return WorkflowVersionDetailDto.builder()
                .id(String.valueOf(version.getId()))
                .workflowDefinitionId(String.valueOf(version.getWorkflowDefinition().getId()))
                .versionNo(version.getVersionNo())
                .status(version.getStatus())
                .formSchema(parsedSchema)
                .startStepKey(version.getStartStep() != null ? version.getStartStep().getStepKey() : null)
                .changeNote(version.getChangeNote())
                .lockVersion(version.getLockVersion())
                .publishedAt(version.getPublishedAt())
                .steps(steps.stream().map(s -> WorkflowVersionDetailDto.StepDto.builder()
                        .id(String.valueOf(s.getId()))
                        .stepKey(s.getStepKey())
                        .stepName(s.getStepName())
                        .displayOrder(s.getDisplayOrder())
                        .assigneeType(s.getAssigneeType())
                        .assigneeUserId(s.getAssigneeUser() != null ? String.valueOf(s.getAssigneeUser().getId()) : null)
                        .assigneeGroupId(s.getAssigneeGroup() != null ? String.valueOf(s.getAssigneeGroup().getId()) : null)
                        .slaHours(s.getSlaHours())
                        .allowRequestChanges(s.getAllowRequestChanges())
                        .build()).collect(Collectors.toList()))
                .transitions(transitions.stream().map(t -> WorkflowVersionDetailDto.TransitionDto.builder()
                        .id(String.valueOf(t.getId()))
                        .fromStepKey(t.getFromStep().getStepKey())
                        .targetType(t.getTargetType())
                        .toStepKey(t.getToStep() != null ? t.getToStep().getStepKey() : null)
                        .conditionExpression(parseJson(t.getConditionExpression()))
                        .isDefault(t.getIsDefault())
                        .priority(t.getPriority())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void publishVersion(Long versionId, Long publishedByUserId) {
        WorkflowVersionJpaEntity draft = versionRepository.findById(versionId)
                .orElseThrow(() -> new EntityNotFoundException("Phiên bản quy trình", versionId));

        if (draft.getStatus() != VersionStatus.DRAFT) {
            throw new BusinessRuleException("VERSION_NOT_DRAFT", "Chỉ có thể xuất bản phiên bản DRAFT");
        }

        if (draft.getStartStep() == null) {
            throw new BusinessRuleException("MISSING_START_STEP", "Quy trình chưa chọn bước bắt đầu (start step)");
        }

        UserJpaEntity publisher = userRepository.findById(publishedByUserId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", publishedByUserId));

        // Archive previous published version if any
        versionRepository.findByWorkflowDefinitionIdAndStatus(draft.getWorkflowDefinition().getId(), VersionStatus.PUBLISHED)
                .ifPresent(prev -> {
                    prev.setStatus(VersionStatus.ARCHIVED);
                    versionRepository.save(prev);
                });

        draft.setStatus(VersionStatus.PUBLISHED);
        draft.setPublishedBy(publisher);
        draft.setPublishedAt(Instant.now());
        versionRepository.save(draft);
    }

    private Object parseJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) return null;
        try {
            return objectMapper.readValue(jsonStr, Object.class);
        } catch (JsonProcessingException e) {
            return jsonStr;
        }
    }

}
