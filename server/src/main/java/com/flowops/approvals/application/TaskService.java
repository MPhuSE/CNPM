package com.flowops.approvals.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowops.approvals.api.dto.*;
import com.flowops.approvals.domain.AssignmentState;
import com.flowops.approvals.domain.TaskAction;
import com.flowops.approvals.domain.TaskStatus;
import com.flowops.approvals.infrastructure.persistence.*;
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
import com.flowops.organization.infrastructure.persistence.ApprovalGroupJpaEntity;
import com.flowops.organization.infrastructure.persistence.ApprovalGroupMemberJpaEntity;
import com.flowops.requests.domain.RequestStatus;
import com.flowops.requests.infrastructure.persistence.RequestEditJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.requests.infrastructure.persistence.SpringDataRequestEditRepository;
import com.flowops.requests.infrastructure.persistence.SpringDataRequestRepository;
import com.flowops.shared.application.dto.PageResponse;
import com.flowops.shared.domain.BusinessRuleException;
import com.flowops.shared.domain.ConcurrencyConflictException;
import com.flowops.shared.domain.EntityNotFoundException;
import com.flowops.shared.domain.ForbiddenActionException;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowruntime.application.WorkflowEngine;
import com.flowops.workflowruntime.domain.RoutingResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class TaskService {
    public TaskService(SpringDataTaskRepository taskRepository, SpringDataRevisionApprovalRepository revisionApprovalRepository, SpringDataRequestRepository requestRepository, SpringDataRequestEditRepository editRepository, SpringDataUserRepository userRepository, SpringDataIncidentRepository incidentRepository, SpringDataRequestEventRepository eventRepository, SpringDataAuditLogRepository auditLogRepository, SpringDataNotificationRepository notificationRepository, WorkflowEngine workflowEngine, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.revisionApprovalRepository = revisionApprovalRepository;
        this.requestRepository = requestRepository;
        this.editRepository = editRepository;
        this.userRepository = userRepository;
        this.incidentRepository = incidentRepository;
        this.eventRepository = eventRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.workflowEngine = workflowEngine;
        this.objectMapper = objectMapper;
    }


    private final SpringDataTaskRepository taskRepository;
    private final SpringDataRevisionApprovalRepository revisionApprovalRepository;
    private final SpringDataRequestRepository requestRepository;
    private final SpringDataRequestEditRepository editRepository;
    private final SpringDataUserRepository userRepository;
    private final SpringDataIncidentRepository incidentRepository;
    private final SpringDataRequestEventRepository eventRepository;
    private final SpringDataAuditLogRepository auditLogRepository;
    private final SpringDataNotificationRepository notificationRepository;
    private final WorkflowEngine workflowEngine;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public PageResponse<TaskSummaryDto> getTasks(String scope, Long callerId, int page, int size) {
        UserJpaEntity caller = userRepository.findById(callerId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", callerId));

        Page<TaskJpaEntity> taskPage;

        if ("group".equalsIgnoreCase(scope)) {
            // Find groups user is member of
            List<Long> groupIds = caller.getDepartment() != null
                    ? List.of(1L, 2L, 3L) // fallback/demo group ids
                    : List.of(1L, 2L, 3L);
            taskPage = taskRepository.findGroupOpenTasks(AssignmentState.GROUP_OPEN, TaskStatus.PENDING, groupIds, PageRequest.of(page, size));
        } else {
            taskPage = taskRepository.findByAssigneeIdAndStatus(callerId, TaskStatus.PENDING, PageRequest.of(page, size));
        }

        List<TaskSummaryDto> dtos = taskPage.getContent().stream()
                .filter(t -> !Objects.equals(t.getRequest().getRequester().getId(), callerId)) // Exclude self approval
                .map(this::toSummaryDto)
                .toList();

        return PageResponse.of(dtos, taskPage.getNumber(), taskPage.getSize(), taskPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public TaskDetailDto getTaskDetail(Long taskId, Long callerId) {
        TaskJpaEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tác vụ", taskId));

        List<String> allowedActions = new ArrayList<>();
        boolean isRequester = Objects.equals(task.getRequest().getRequester().getId(), callerId);

        if (!isRequester && task.getStatus() == TaskStatus.PENDING) {
            if (task.getAssignmentState() == AssignmentState.GROUP_OPEN) {
                allowedActions.add("CLAIM");
            } else if (task.getAssignmentState() == AssignmentState.ASSIGNED &&
                    task.getAssignee() != null && Objects.equals(task.getAssignee().getId(), callerId)) {
                allowedActions.add("APPROVE");
                allowedActions.add("REJECT");
                if (Boolean.TRUE.equals(task.getWorkflowStep().getAllowRequestChanges())) {
                    allowedActions.add("REQUEST_CHANGES");
                }
            }
        }
        allowedActions.add("VIEW");

        Object parsedFormData = parseJson(task.getRevision().getFormData());

        return TaskDetailDto.builder()
                .id(String.valueOf(task.getId()))
                .requestId(String.valueOf(task.getRequest().getId()))
                .requestCode(task.getRequest().getRequestCode())
                .requestTitle(task.getRequest().getTitle())
                .requesterName(task.getRequest().getRequester().getFullName())
                .workflowName(task.getWorkflowVersion().getWorkflowDefinition().getName())
                .stepName(task.getWorkflowStep().getStepName())
                .revisionId(String.valueOf(task.getRevision().getId()))
                .revisionNo(task.getRevision().getRevisionNo())
                .status(task.getStatus())
                .assignmentState(task.getAssignmentState())
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getFullName() : null)
                .candidateGroupName(task.getCandidateGroup() != null ? task.getCandidateGroup().getName() : null)
                .dueAt(task.getDueAt())
                .isOverdue(task.getDueAt() != null && Instant.now().isAfter(task.getDueAt()))
                .createdAt(task.getCreatedAt())
                .taskVersion(task.getLockVersion())
                .requestVersion(task.getRequest().getLockVersion())
                .formData(parsedFormData)
                .allowRequestChanges(task.getWorkflowStep().getAllowRequestChanges())
                .allowedActions(allowedActions)
                .build();
    }

    @Transactional
    public TaskDetailDto claimTask(Long taskId, ClaimTaskRequest req, Long callerId) {
        TaskJpaEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tác vụ", taskId));

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new ConcurrencyConflictException("TASK_NOT_PENDING", "Tác vụ đã kết thúc hoặc không còn hiệu lực");
        }

        if (task.getAssignmentState() != AssignmentState.GROUP_OPEN) {
            throw new ConcurrencyConflictException("TASK_ALREADY_CLAIMED", "Tác vụ đã được nhận bởi người khác");
        }

        if (Objects.equals(task.getRequest().getRequester().getId(), callerId)) {
            throw new ForbiddenActionException("SELF_APPROVAL_FORBIDDEN", "Người tạo hồ sơ không được phép nhận nhiệm vụ phê duyệt");
        }

        UserJpaEntity caller = userRepository.findById(callerId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", callerId));

        task.setAssignee(caller);
        task.setAssignmentState(AssignmentState.ASSIGNED);
        task.setClaimedAt(Instant.now());
        task = taskRepository.save(task);

        // Record event
        UUID eventKey = UUID.randomUUID();
        RequestEventJpaEntity event = new RequestEventJpaEntity();
        event.setEventKey(eventKey);
        event.setRequest(task.getRequest());
        event.setRevision(task.getRevision());
        event.setTask(task);
        event.setActor(caller);
        event.setEventType("CLAIMED");
        eventRepository.save(event);

        return getTaskDetail(taskId, callerId);
    }

    @Transactional
    public void decideTask(Long taskId, DecisionTaskRequest req, Long callerId) {
        TaskJpaEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tác vụ", taskId));

        if (task.getStatus() != TaskStatus.PENDING) {
            throw new ConcurrencyConflictException("TASK_STALE", "Tác vụ đã kết thúc hoặc thay đổi");
        }

        if (task.getAssignmentState() != AssignmentState.ASSIGNED ||
                task.getAssignee() == null || !Objects.equals(task.getAssignee().getId(), callerId)) {
            throw new ForbiddenActionException("ACCESS_DENIED", "Bạn không phải người được giao xử lý tác vụ này");
        }

        RequestJpaEntity request = task.getRequest();
        UserJpaEntity caller = task.getAssignee();
        Instant now = Instant.now();

        task.setStatus(TaskStatus.COMPLETED);
        task.setActionTaken(req.getAction());
        task.setDecisionReason(req.getReason());
        task.setCompletedBy(caller);
        task.setCompletedAt(now);
        taskRepository.save(task);

        UUID eventKey = UUID.randomUUID();

        switch (req.getAction()) {
            case APPROVE -> {
                // Record revision approval link
                RevisionApprovalJpaEntity approval = new RevisionApprovalJpaEntity();
                approval.setId(new RevisionApprovalJpaEntity.Pk(task.getRevision().getId(), task.getWorkflowStep().getId()));
                approval.setRequest(request);
                approval.setRevision(task.getRevision());
                approval.setWorkflowStep(task.getWorkflowStep());
                approval.setSourceTask(task);
                approval.setIsInherited(false);
                revisionApprovalRepository.save(approval);

                // Workflow engine evaluation
                RoutingResult routing = workflowEngine.evaluateNext(task.getWorkflowStep(), request, task.getRevision());

                if (routing.isEndApproved()) {
                    request.setStatus(RequestStatus.APPROVED);
                    request.setCompletedAt(now);
                    requestRepository.save(request);

                    // Notify requester
                    sendNotification(request.getRequester(), "REQUEST_APPROVED", request, task,
                            "Hồ sơ đã được phê duyệt", "Hồ sơ '" + request.getTitle() + "' đã hoàn tất quá trình phê duyệt.");
                } else if (routing.getNextStep() != null) {
                    WorkflowStepJpaEntity nextStep = routing.getNextStep();
                    request.setCurrentStep(nextStep);
                    requestRepository.save(request);

                    // Create next step task
                    TaskJpaEntity nextTask = new TaskJpaEntity();
                    nextTask.setRequest(request);
                    nextTask.setWorkflowVersion(request.getWorkflowVersion());
                    nextTask.setWorkflowStep(nextStep);
                    nextTask.setRevision(task.getRevision());
                    nextTask.setStatus(TaskStatus.PENDING);
                    nextTask.setAssignmentState(routing.getAssignmentState() != null ? routing.getAssignmentState() : AssignmentState.NEEDS_ASSIGNMENT);
                    nextTask.setAssignee(routing.getAssignee());
                    nextTask.setCandidateGroup(routing.getCandidateGroup());
                    if (nextStep.getSlaHours() != null) {
                        nextTask.setDueAt(now.plus(nextStep.getSlaHours(), ChronoUnit.HOURS));
                    }
                    taskRepository.save(nextTask);

                    if (routing.getAssignee() != null) {
                        sendNotification(routing.getAssignee(), "TASK_ASSIGNED", request, nextTask,
                                "Nhiệm vụ mới cần phê duyệt", "Bạn nhận được nhiệm vụ mới cho hồ sơ: " + request.getTitle());
                    }
                }
            }
            case REJECT -> {
                if (req.getReason() == null || req.getReason().isBlank()) {
                    throw new BusinessRuleException("REASON_REQUIRED", "Lý do từ chối là bắt buộc");
                }
                request.setStatus(RequestStatus.REJECTED);
                request.setCompletedAt(now);
                requestRepository.save(request);

                sendNotification(request.getRequester(), "REQUEST_REJECTED", request, task,
                        "Hồ sơ bị từ chối", "Hồ sơ '" + request.getTitle() + "' đã bị từ chối: " + req.getReason());
            }
            case REQUEST_CHANGES -> {
                if (req.getReason() == null || req.getReason().isBlank()) {
                    throw new BusinessRuleException("REASON_REQUIRED", "Lý do yêu cầu bổ sung là bắt buộc");
                }
                request.setStatus(RequestStatus.RETURNED);
                requestRepository.save(request);

                // Create working copy in request_edits
                RequestEditJpaEntity edit = editRepository.findById(request.getId())
                        .orElseGet(() -> {
                            RequestEditJpaEntity e = new RequestEditJpaEntity();
                            e.setRequest(request);
                            e.setUpdatedBy(request.getRequester());
                            return e;
                        });
                edit.setBaseRevision(task.getRevision());
                edit.setFormData(task.getRevision().getFormData());
                edit.setUpdatedAt(now);
                editRepository.save(edit);

                sendNotification(request.getRequester(), "CHANGES_REQUESTED", request, task,
                        "Hồ sơ cần bổ sung thông tin", "Người duyệt yêu cầu bổ sung thông tin: " + req.getReason());
            }
        }

        // Record event
        RequestEventJpaEntity event = new RequestEventJpaEntity();
        event.setEventKey(eventKey);
        event.setRequest(request);
        event.setRevision(task.getRevision());
        event.setTask(task);
        event.setActor(caller);
        event.setEventType(req.getAction().name());
        event.setReason(req.getReason());
        eventRepository.save(event);
    }

    @Transactional
    public void reassignTask(Long taskId, ReassignTaskRequest req, Long adminUserId) {
        TaskJpaEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Tác vụ", taskId));

        UserJpaEntity newAssignee = userRepository.findById(Long.valueOf(req.getNewAssigneeId()))
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", req.getNewAssigneeId()));

        UserJpaEntity admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("Người dùng", adminUserId));

        UserJpaEntity oldAssignee = task.getAssignee();
        task.setAssignee(newAssignee);
        task.setAssignmentState(AssignmentState.ASSIGNED);
        taskRepository.save(task);

        // Close any OPEN incident for this task
        incidentRepository.findByRequestIdAndStatus(task.getRequest().getId(), "OPEN")
                .ifPresent(inc -> {
                    inc.setStatus("RESOLVED");
                    inc.setResolvedBy(admin);
                    inc.setResolutionReason(req.getReason());
                    inc.setClosedAt(Instant.now());
                    incidentRepository.save(inc);
                });

        // Record event
        UUID eventKey = UUID.randomUUID();
        RequestEventJpaEntity event = new RequestEventJpaEntity();
        event.setEventKey(eventKey);
        event.setRequest(task.getRequest());
        event.setRevision(task.getRevision());
        event.setTask(task);
        event.setActor(admin);
        event.setEventType("REASSIGNED");
        event.setReason(req.getReason());
        event.setOldAssignee(oldAssignee);
        event.setNewAssignee(newAssignee);
        eventRepository.save(event);

        sendNotification(newAssignee, "TASK_REASSIGNED", task.getRequest(), task,
                "Bạn được phân công nhiệm vụ mới", "Admin đã phân công lại nhiệm vụ cho hồ sơ: " + task.getRequest().getTitle());
    }

    private void sendNotification(UserJpaEntity user, String type, RequestJpaEntity request, TaskJpaEntity task, String title, String msg) {
        NotificationJpaEntity notif = new NotificationJpaEntity();
        notif.setEventKey(UUID.randomUUID());
        notif.setUser(user);
        notif.setNotificationType(type);
        notif.setRequest(request);
        notif.setTaskId(task != null ? task.getId() : null);
        notif.setTitle(title);
        notif.setMessage(msg);
        notificationRepository.save(notif);
    }

    private TaskSummaryDto toSummaryDto(TaskJpaEntity t) {
        return TaskSummaryDto.builder()
                .id(String.valueOf(t.getId()))
                .requestId(String.valueOf(t.getRequest().getId()))
                .requestCode(t.getRequest().getRequestCode())
                .requestTitle(t.getRequest().getTitle())
                .requesterName(t.getRequest().getRequester().getFullName())
                .workflowName(t.getWorkflowVersion().getWorkflowDefinition().getName())
                .stepName(t.getWorkflowStep().getStepName())
                .revisionId(String.valueOf(t.getRevision().getId()))
                .revisionNo(t.getRevision().getRevisionNo())
                .status(t.getStatus())
                .assignmentState(t.getAssignmentState())
                .assigneeName(t.getAssignee() != null ? t.getAssignee().getFullName() : null)
                .candidateGroupName(t.getCandidateGroup() != null ? t.getCandidateGroup().getName() : null)
                .dueAt(t.getDueAt())
                .isOverdue(t.getDueAt() != null && Instant.now().isAfter(t.getDueAt()))
                .createdAt(t.getCreatedAt())
                .taskVersion(t.getLockVersion())
                .requestVersion(t.getRequest().getLockVersion())
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
