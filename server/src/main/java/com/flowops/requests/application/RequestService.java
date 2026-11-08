package com.flowops.requests.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.approvals.domain.AssignmentState;
import com.flowops.approvals.domain.TaskStatus;
import com.flowops.approvals.infrastructure.persistence.SpringDataTaskRepository;
import com.flowops.approvals.infrastructure.persistence.TaskJpaEntity;
import com.flowops.audit.infrastructure.persistence.AuditLogJpaEntity;
import com.flowops.audit.infrastructure.persistence.RequestEventJpaEntity;
import com.flowops.audit.infrastructure.persistence.SpringDataAuditLogRepository;
import com.flowops.audit.infrastructure.persistence.SpringDataRequestEventRepository;
import com.flowops.identity.infrastructure.persistence.SpringDataUserRepository;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.notifications.infrastructure.persistence.NotificationJpaEntity;
import com.flowops.notifications.infrastructure.persistence.SpringDataNotificationRepository;
import com.flowops.operations.infrastructure.persistence.SpringDataIncidentRepository;
import com.flowops.operations.infrastructure.persistence.WorkflowIncidentJpaEntity;
import com.flowops.requests.api.dto.*;
import com.flowops.requests.domain.RequestStatus;
import com.flowops.requests.infrastructure.persistence.*;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.domain.BusinessRuleException;
import com.flowops.shared.domain.EntityNotFoundException;
import com.flowops.shared.domain.ForbiddenActionException;
import com.flowops.workflowconfig.domain.VersionStatus;
import com.flowops.workflowconfig.infrastructure.persistence.SpringDataWorkflowVersionRepository;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowVersionJpaEntity;
import com.flowops.workflowruntime.application.AssignmentResolver;
import com.flowops.workflowruntime.domain.RoutingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class RequestService {
    public RequestService(SpringDataRequestRepository requestRepository, SpringDataRequestRevisionRepository revisionRepository, SpringDataRequestEditRepository editRepository, SpringDataWorkflowVersionRepository versionRepository, SpringDataTaskRepository taskRepository, SpringDataUserRepository userRepository, SpringDataIncidentRepository incidentRepository, SpringDataRequestEventRepository eventRepository, SpringDataAuditLogRepository auditLogRepository, SpringDataNotificationRepository notificationRepository, AssignmentResolver assignmentResolver, ObjectMapper objectMapper) {
        this.requestRepository = requestRepository;
        this.revisionRepository = revisionRepository;
        this.editRepository = editRepository;
        this.versionRepository = versionRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.eventRepository = eventRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.assignmentResolver = assignmentResolver;
        this.objectMapper = objectMapper;
    }


    private final SpringDataRequestRepository requestRepository;
    private final SpringDataRequestRevisionRepository revisionRepository;
    private final SpringDataRequestEditRepository editRepository;
    private final SpringDataWorkflowVersionRepository versionRepository;
    private final SpringDataTaskRepository taskRepository;
    private final SpringDataUserRepository userRepository;
    private final SpringDataIncidentRepository incidentRepository;
    private final SpringDataRequestEventRepository eventRepository;
    private final SpringDataAuditLogRepository auditLogRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final AssignmentResolver assignmentResolver;
    private final ObjectMapper objectMapper;

    @Transactional
    public RequestDetailDto createDraft(CreateDraftRequest req, Long requesterId) {
        UserJpaEntity requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", requesterId));

        WorkflowVersionJpaEntity version = versionRepository.findById(Long.valueOf(req.getWorkflowVersionId()))
                .orElseThrow(() -> new EntityNotFoundException("Phiên bản quy trình", req.getWorkflowVersionId()));

        if (version.getStatus() != VersionStatus.PUBLISHED) {
            throw new BusinessRuleException("WORKFLOW_VERSION_CLOSED", "Chỉ có thể tạo hồ sơ từ phiên bản quy trình đã PUBLISHED");
        }

        RequestJpaEntity request = new RequestJpaEntity();
        request.setWorkflowVersion(version);
        request.setRequester(requester);
        request.setTitle(req.getTitle());
        request.setStatus(RequestStatus.DRAFT);
        request = requestRepository.save(request);

        RequestEditJpaEntity edit = new RequestEditJpaEntity();
        edit.setRequest(request);
        edit.setFormData("{}");
        edit.setUpdatedBy(requester);
        editRepository.save(edit);

        return toDetailDto(request, edit, requesterId);
    }

    @Transactional
    public RequestDetailDto updateDraft(Long requestId, UpdateDraftDto dto, Long requesterId) {
        RequestJpaEntity request = getOwnedRequest(requestId, requesterId);

        if (request.getStatus() != RequestStatus.DRAFT && request.getStatus() != RequestStatus.RETURNED) {
            throw new BusinessRuleException("INVALID_STATE", "Chỉ được sửa dữ liệu khi hồ sơ ở trạng thái DRAFT hoặc RETURNED");
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            request.setTitle(dto.getTitle());
            requestRepository.save(request);
        }

        RequestEditJpaEntity edit = editRepository.findById(requestId)
                .orElseGet(() -> {
                    RequestEditJpaEntity e = new RequestEditJpaEntity();
                    e.setRequest(request);
                    e.setUpdatedBy(request.getRequester());
                    return e;
                });

        try {
            edit.setFormData(objectMapper.writeValueAsString(dto.getFormData() != null ? dto.getFormData() : Map.of()));
        } catch (JsonProcessingException e) {
            edit.setFormData("{}");
        }
        edit.setUpdatedAt(Instant.now());
        edit = editRepository.save(edit);

        return toDetailDto(request, edit, requesterId);
    }

    @Transactional
    public RequestDetailDto submitRequest(Long requestId, SubmitRequestDto dto, Long requesterId) {
        RequestJpaEntity request = getOwnedRequest(requestId, requesterId);

        if (request.getStatus() != RequestStatus.DRAFT) {
            throw new BusinessRuleException("INVALID_STATE", "Chỉ hồ sơ DRAFT mới được gửi duyệt lần đầu");
        }

        RequestEditJpaEntity edit = editRepository.findById(requestId)
                .orElseThrow(() -> new BusinessRuleException("MISSING_WORKING_COPY", "Không tìm thấy dữ liệu soạn thảo"));

        WorkflowVersionJpaEntity version = request.getWorkflowVersion();
        WorkflowStepJpaEntity startStep = version.getStartStep();
        if (startStep == null) {
            throw new BusinessRuleException("MISSING_START_STEP", "Quy trình chưa cấu hình bước bắt đầu");
        }

        // Snapshot source org context
        UserJpaEntity requester = request.getRequester();
        request.setSourceDepartment(requester.getDepartment());
        request.setSourceManager(requester.getManager());
        if (requester.getDepartment() != null) {
            request.setSourceDepartmentManager(requester.getDepartment().getManager());
        }

        // Assign formal code
        request.setRequestCode(String.format("REQ-%d-%06d", Instant.now().atZone(java.time.ZoneOffset.UTC).getYear(), request.getId()));
        request.setStatus(RequestStatus.PENDING);
        request.setCurrentStep(startStep);
        request.setFirstSubmittedAt(Instant.now());
        request.setLastSubmittedAt(Instant.now());

        // Create Revision 1
        RequestRevisionJpaEntity rev1 = new RequestRevisionJpaEntity();
        rev1.setRequest(request);
        rev1.setRevisionNo(1);
        rev1.setFormData(edit.getFormData());
        rev1.setChangedFields("[]");
        rev1.setRestartRequired(true);
        rev1.setResumeStep(startStep);
        rev1.setSubmittedBy(requester);
        rev1.setSubmittedAt(Instant.now());
        rev1 = revisionRepository.save(rev1);

        request.setCurrentRevision(rev1);
        request = requestRepository.save(request);

        // Create first Task
        RoutingResult routing = new RoutingResult();
        assignmentResolver.resolveAssignment(startStep, request, routing);

        TaskJpaEntity task = new TaskJpaEntity();
        task.setRequest(request);
        task.setWorkflowVersion(version);
        task.setWorkflowStep(startStep);
        task.setRevision(rev1);
        task.setStatus(TaskStatus.PENDING);
        task.setAssignmentState(routing.getAssignmentState() != null ? routing.getAssignmentState() : AssignmentState.NEEDS_ASSIGNMENT);
        task.setAssignee(routing.getAssignee());
        task.setCandidateGroup(routing.getCandidateGroup());
        if (startStep.getSlaHours() != null) {
            task.setDueAt(Instant.now().plus(startStep.getSlaHours(), ChronoUnit.HOURS));
        }
        task = taskRepository.save(task);

        // If needs assignment, create incident
        if (task.getAssignmentState() == AssignmentState.NEEDS_ASSIGNMENT) {
            WorkflowIncidentJpaEntity incident = new WorkflowIncidentJpaEntity();
            incident.setRequest(request);
            incident.setRevision(rev1);
            incident.setWorkflowVersion(version);
            incident.setStep(startStep);
            incident.setTask(task);
            incident.setIncidentType("ASSIGNMENT_REQUIRED");
            incident.setStatus("OPEN");
            incident.setOperationKey(UUID.randomUUID());
            incident.setErrorCode("MISSING_APPROVER");
            incidentRepository.save(incident);
        }

        // Record event and audit
        UUID eventKey = UUID.randomUUID();
        RequestEventJpaEntity event = new RequestEventJpaEntity();
        event.setEventKey(eventKey);
        event.setRequest(request);
        event.setRevision(rev1);
        event.setTask(task);
        event.setActor(requester);
        event.setEventType("SUBMITTED");
        eventRepository.save(event);

        AuditLogJpaEntity audit = new AuditLogJpaEntity();
        audit.setEventKey(eventKey);
        audit.setActor(requester);
        audit.setRequest(request);
        audit.setRevisionId(rev1.getId());
        audit.setAction("REQUEST_SUBMITTED");
        audit.setEntityType("REQUEST");
        audit.setEntityId(String.valueOf(request.getId()));
        auditLogRepository.save(audit);

        return toDetailDto(request, edit, requesterId);
    }

    @Transactional
    public RequestDetailDto resubmitRequest(Long requestId, ResubmitRequestDto dto, Long requesterId) {
        RequestJpaEntity request = getOwnedRequest(requestId, requesterId);

        if (request.getStatus() != RequestStatus.RETURNED) {
            throw new BusinessRuleException("INVALID_STATE", "Chỉ hồ sơ RETURNED mới được gửi lại");
        }

        RequestEditJpaEntity edit = editRepository.findById(requestId)
                .orElseThrow(() -> new BusinessRuleException("MISSING_WORKING_COPY", "Không tìm thấy dữ liệu soạn thảo"));

        WorkflowVersionJpaEntity version = request.getWorkflowVersion();
        WorkflowStepJpaEntity startStep = version.getStartStep();

        // Increment revision
        int nextRevNo = request.getCurrentRevision() != null ? request.getCurrentRevision().getRevisionNo() + 1 : 2;

        RequestRevisionJpaEntity newRev = new RequestRevisionJpaEntity();
        newRev.setRequest(request);
        newRev.setRevisionNo(nextRevNo);
        newRev.setFormData(edit.getFormData());
        newRev.setChangedFields("[]");
        newRev.setRestartRequired(true);
        newRev.setResumeStep(startStep);
        newRev.setSubmittedBy(request.getRequester());
        newRev.setSubmittedAt(Instant.now());
        newRev = revisionRepository.save(newRev);

        request.setCurrentRevision(newRev);
        request.setStatus(RequestStatus.PENDING);
        request.setLastSubmittedAt(Instant.now());
        request.setCurrentStep(startStep);
        request = requestRepository.save(request);

        // Create new Task at startStep
        RoutingResult routing = new RoutingResult();
        assignmentResolver.resolveAssignment(startStep, request, routing);

        TaskJpaEntity task = new TaskJpaEntity();
        task.setRequest(request);
        task.setWorkflowVersion(version);
        task.setWorkflowStep(startStep);
        task.setRevision(newRev);
        task.setStatus(TaskStatus.PENDING);
        task.setAssignmentState(routing.getAssignmentState() != null ? routing.getAssignmentState() : AssignmentState.NEEDS_ASSIGNMENT);
        task.setAssignee(routing.getAssignee());
        task.setCandidateGroup(routing.getCandidateGroup());
        if (startStep.getSlaHours() != null) {
            task.setDueAt(Instant.now().plus(startStep.getSlaHours(), ChronoUnit.HOURS));
        }
        task = taskRepository.save(task);

        return toDetailDto(request, edit, requesterId);
    }

    @Transactional
    public RequestDetailDto cancelRequest(Long requestId, CancelRequestDto dto, Long requesterId) {
        RequestJpaEntity request = getOwnedRequest(requestId, requesterId);

        if (request.getStatus() == RequestStatus.APPROVED ||
                request.getStatus() == RequestStatus.REJECTED ||
                request.getStatus() == RequestStatus.CANCELLED) {
            throw new BusinessRuleException("INVALID_STATE", "Hồ sơ đã kết thúc không thể hủy");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setCancelReason(dto.getReason());
        request.setCompletedAt(Instant.now());
        request = requestRepository.save(request);

        // Close any pending task
        taskRepository.findByRequestIdAndStatus(requestId, TaskStatus.PENDING)
                .ifPresent(t -> {
                    t.setStatus(TaskStatus.CANCELLED);
                    t.setCompletedAt(Instant.now());
                    taskRepository.save(t);
                });

        return toDetailDto(request, null, requesterId);
    }

    @Transactional(readOnly = true)
    public PageResponse<RequestSummaryDto> getMyRequests(Long requesterId, RequestStatus status, int page, int size) {
        Page<RequestJpaEntity> requestPage = status == null
                ? requestRepository.findByRequesterId(requesterId, PageRequest.of(page, size))
                : requestRepository.findByRequesterIdAndStatus(requesterId, status, PageRequest.of(page, size));

        List<RequestSummaryDto> dtos = requestPage.getContent().stream()
                .map(r -> RequestSummaryDto.builder()
                        .id(String.valueOf(r.getId()))
                        .requestCode(r.getRequestCode())
                        .workflowName(r.getWorkflowVersion().getWorkflowDefinition().getName())
                        .workflowCode(r.getWorkflowVersion().getWorkflowDefinition().getCode())
                        .workflowVersionNo(r.getWorkflowVersion().getVersionNo())
                        .title(r.getTitle())
                        .status(r.getStatus())
                        .requesterName(r.getRequester().getFullName())
                        .firstSubmittedAt(r.getFirstSubmittedAt())
                        .lastSubmittedAt(r.getLastSubmittedAt())
                        .completedAt(r.getCompletedAt())
                        .flags(Collections.emptyList())
                        .build())
                .toList();

        return PageResponse.of(dtos, requestPage.getNumber(), requestPage.getSize(), requestPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RequestDetailDto getRequestDetail(Long requestId, Long requesterId) {
        RequestJpaEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Hồ sơ", requestId));

        RequestEditJpaEntity edit = editRepository.findById(requestId).orElse(null);
        return toDetailDto(request, edit, requesterId);
    }

    private RequestJpaEntity getOwnedRequest(Long requestId, Long requesterId) {
        RequestJpaEntity request = requestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Hồ sơ", requestId));

        if (!Objects.equals(request.getRequester().getId(), requesterId)) {
            throw new ForbiddenActionException("ACCESS_DENIED", "Bạn không có quyền thao tác trên hồ sơ của người khác");
        }
        return request;
    }

    private RequestDetailDto toDetailDto(RequestJpaEntity req, RequestEditJpaEntity edit, Long callerId) {
        Object parsedSchema = parseJson(req.getWorkflowVersion().getFormSchema());
        Object parsedRevFormData = req.getCurrentRevision() != null ? parseJson(req.getCurrentRevision().getFormData()) : null;
        Object parsedWorkingCopy = edit != null ? parseJson(edit.getFormData()) : null;

        List<String> allowedActions = new ArrayList<>();
        boolean isOwner = Objects.equals(req.getRequester().getId(), callerId);

        if (isOwner) {
            if (req.getStatus() == RequestStatus.DRAFT) {
                allowedActions.addAll(List.of("EDIT", "SUBMIT", "CANCEL", "DELETE"));
            } else if (req.getStatus() == RequestStatus.RETURNED) {
                allowedActions.addAll(List.of("EDIT", "RESUBMIT", "CANCEL"));
            } else if (req.getStatus() == RequestStatus.PENDING) {
                allowedActions.add("CANCEL");
            }
        }
        allowedActions.add("VIEW");

        return RequestDetailDto.builder()
                .id(String.valueOf(req.getId()))
                .requestCode(req.getRequestCode())
                .workflowVersionId(String.valueOf(req.getWorkflowVersion().getId()))
                .workflowName(req.getWorkflowVersion().getWorkflowDefinition().getName())
                .formSchema(parsedSchema)
                .requesterId(String.valueOf(req.getRequester().getId()))
                .requesterName(req.getRequester().getFullName())
                .title(req.getTitle())
                .status(req.getStatus())
                .currentRevisionId(req.getCurrentRevision() != null ? String.valueOf(req.getCurrentRevision().getId()) : null)
                .currentRevisionNo(req.getCurrentRevision() != null ? req.getCurrentRevision().getRevisionNo() : null)
                .formData(parsedRevFormData)
                .workingCopyData(parsedWorkingCopy)
                .firstSubmittedAt(req.getFirstSubmittedAt())
                .lastSubmittedAt(req.getLastSubmittedAt())
                .cancelReason(req.getCancelReason())
                .requestVersion(req.getLockVersion())
                .editVersion(edit != null ? edit.getLockVersion() : 0L)
                .flags(Collections.emptyList())
                .allowedActions(allowedActions)
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
