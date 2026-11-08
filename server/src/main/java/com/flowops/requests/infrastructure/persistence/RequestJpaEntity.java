package com.flowops.requests.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.organization.infrastructure.persistence.DepartmentJpaEntity;
import com.flowops.requests.domain.RequestStatus;
import com.flowops.shared.domain.BaseEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowVersionJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requests")
public class RequestJpaEntity extends BaseEntity {

    @Column(name = "request_code", unique = true, length = 100)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersionJpaEntity workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserJpaEntity requester;

    @Column(nullable = false, length = 250)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_department_id")
    private DepartmentJpaEntity sourceDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_manager_id")
    private UserJpaEntity sourceManager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_department_manager_id")
    private UserJpaEntity sourceDepartmentManager;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_context", columnDefinition = "jsonb")
    private String sourceContext;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_revision_id")
    private RequestRevisionJpaEntity currentRevision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_step_id")
    private WorkflowStepJpaEntity currentStep;

    @Column(name = "first_submitted_at")
    private Instant firstSubmittedAt;

    @Column(name = "last_submitted_at")
    private Instant lastSubmittedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RequestRevisionJpaEntity> revisions = new ArrayList<>();

    public RequestJpaEntity() {}

    public String getRequestCode() {
        return this.requestCode;
    }

    public void setRequestCode(String requestCode) {
        this.requestCode = requestCode;
    }

    public WorkflowVersionJpaEntity getWorkflowVersion() {
        return this.workflowVersion;
    }

    public void setWorkflowVersion(WorkflowVersionJpaEntity workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public UserJpaEntity getRequester() {
        return this.requester;
    }

    public void setRequester(UserJpaEntity requester) {
        this.requester = requester;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public RequestStatus getStatus() {
        return this.status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public DepartmentJpaEntity getSourceDepartment() {
        return this.sourceDepartment;
    }

    public void setSourceDepartment(DepartmentJpaEntity sourceDepartment) {
        this.sourceDepartment = sourceDepartment;
    }

    public UserJpaEntity getSourceManager() {
        return this.sourceManager;
    }

    public void setSourceManager(UserJpaEntity sourceManager) {
        this.sourceManager = sourceManager;
    }

    public UserJpaEntity getSourceDepartmentManager() {
        return this.sourceDepartmentManager;
    }

    public void setSourceDepartmentManager(UserJpaEntity sourceDepartmentManager) {
        this.sourceDepartmentManager = sourceDepartmentManager;
    }

    public String getSourceContext() {
        return this.sourceContext;
    }

    public void setSourceContext(String sourceContext) {
        this.sourceContext = sourceContext;
    }

    public RequestRevisionJpaEntity getCurrentRevision() {
        return this.currentRevision;
    }

    public void setCurrentRevision(RequestRevisionJpaEntity currentRevision) {
        this.currentRevision = currentRevision;
    }

    public WorkflowStepJpaEntity getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(WorkflowStepJpaEntity currentStep) {
        this.currentStep = currentStep;
    }

    public Instant getFirstSubmittedAt() {
        return this.firstSubmittedAt;
    }

    public void setFirstSubmittedAt(Instant firstSubmittedAt) {
        this.firstSubmittedAt = firstSubmittedAt;
    }

    public Instant getLastSubmittedAt() {
        return this.lastSubmittedAt;
    }

    public void setLastSubmittedAt(Instant lastSubmittedAt) {
        this.lastSubmittedAt = lastSubmittedAt;
    }

    public Instant getCompletedAt() {
        return this.completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getCancelReason() {
        return this.cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public List<RequestRevisionJpaEntity> getRevisions() {
        return this.revisions;
    }

    public void setRevisions(List<RequestRevisionJpaEntity> revisions) {
        this.revisions = revisions;
    }

}
