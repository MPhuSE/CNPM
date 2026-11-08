package com.flowops.operations.infrastructure.persistence;

import com.flowops.approvals.infrastructure.persistence.TaskJpaEntity;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestRevisionJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowVersionJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workflow_incidents")
public class WorkflowIncidentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestJpaEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id", nullable = false)
    private RequestRevisionJpaEntity revision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersionJpaEntity workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private WorkflowStepJpaEntity step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskJpaEntity task;

    @Column(name = "incident_type", nullable = false, length = 30)
    private String incidentType; // ASSIGNMENT_REQUIRED / ROUTING_ERROR

    @Column(nullable = false, length = 20)
    private String status = "OPEN"; // OPEN / RESOLVED / CLOSED_CANCELLED

    @Column(name = "operation_key", nullable = false)
    private UUID operationKey;

    @Column(name = "error_code", nullable = false, length = 100)
    private String errorCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String details = "{}";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private UserJpaEntity resolvedBy;

    @Column(name = "resolution_reason", columnDefinition = "TEXT")
    private String resolutionReason;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    public WorkflowIncidentJpaEntity() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RequestJpaEntity getRequest() {
        return this.request;
    }

    public void setRequest(RequestJpaEntity request) {
        this.request = request;
    }

    public RequestRevisionJpaEntity getRevision() {
        return this.revision;
    }

    public void setRevision(RequestRevisionJpaEntity revision) {
        this.revision = revision;
    }

    public WorkflowVersionJpaEntity getWorkflowVersion() {
        return this.workflowVersion;
    }

    public void setWorkflowVersion(WorkflowVersionJpaEntity workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public WorkflowStepJpaEntity getStep() {
        return this.step;
    }

    public void setStep(WorkflowStepJpaEntity step) {
        this.step = step;
    }

    public TaskJpaEntity getTask() {
        return this.task;
    }

    public void setTask(TaskJpaEntity task) {
        this.task = task;
    }

    public UUID getOperationKey() {
        return this.operationKey;
    }

    public void setOperationKey(UUID operationKey) {
        this.operationKey = operationKey;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public UserJpaEntity getResolvedBy() {
        return this.resolvedBy;
    }

    public void setResolvedBy(UserJpaEntity resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionReason() {
        return this.resolutionReason;
    }

    public void setResolutionReason(String resolutionReason) {
        this.resolutionReason = resolutionReason;
    }

    public Instant getOpenedAt() {
        return this.openedAt;
    }

    public void setOpenedAt(Instant openedAt) {
        this.openedAt = openedAt;
    }

    public String getIncidentType() {
        return this.incidentType;
    }

    public void setIncidentType(String incidentType) {
        this.incidentType = incidentType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getClosedAt() {
        return this.closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

}
