package com.flowops.approvals.infrastructure.persistence;

import com.flowops.approvals.domain.AssignmentState;
import com.flowops.approvals.domain.TaskAction;
import com.flowops.approvals.domain.TaskStatus;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.organization.infrastructure.persistence.ApprovalGroupJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestRevisionJpaEntity;
import com.flowops.shared.domain.BaseEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowVersionJpaEntity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tasks")
public class TaskJpaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestJpaEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersionJpaEntity workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_step_id", nullable = false)
    private WorkflowStepJpaEntity workflowStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id", nullable = false)
    private RequestRevisionJpaEntity revision;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_state", nullable = false, length = 20)
    private AssignmentState assignmentState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserJpaEntity assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_group_id")
    private ApprovalGroupJpaEntity candidateGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_taken", length = 30)
    private TaskAction actionTaken;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by")
    private UserJpaEntity completedBy;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "due_at")
    private Instant dueAt;

    public TaskJpaEntity() {}

    public RequestJpaEntity getRequest() {
        return this.request;
    }

    public void setRequest(RequestJpaEntity request) {
        this.request = request;
    }

    public WorkflowVersionJpaEntity getWorkflowVersion() {
        return this.workflowVersion;
    }

    public void setWorkflowVersion(WorkflowVersionJpaEntity workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public WorkflowStepJpaEntity getWorkflowStep() {
        return this.workflowStep;
    }

    public void setWorkflowStep(WorkflowStepJpaEntity workflowStep) {
        this.workflowStep = workflowStep;
    }

    public RequestRevisionJpaEntity getRevision() {
        return this.revision;
    }

    public void setRevision(RequestRevisionJpaEntity revision) {
        this.revision = revision;
    }

    public AssignmentState getAssignmentState() {
        return this.assignmentState;
    }

    public void setAssignmentState(AssignmentState assignmentState) {
        this.assignmentState = assignmentState;
    }

    public UserJpaEntity getAssignee() {
        return this.assignee;
    }

    public void setAssignee(UserJpaEntity assignee) {
        this.assignee = assignee;
    }

    public ApprovalGroupJpaEntity getCandidateGroup() {
        return this.candidateGroup;
    }

    public void setCandidateGroup(ApprovalGroupJpaEntity candidateGroup) {
        this.candidateGroup = candidateGroup;
    }

    public TaskStatus getStatus() {
        return this.status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskAction getActionTaken() {
        return this.actionTaken;
    }

    public void setActionTaken(TaskAction actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getDecisionReason() {
        return this.decisionReason;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public Instant getClaimedAt() {
        return this.claimedAt;
    }

    public void setClaimedAt(Instant claimedAt) {
        this.claimedAt = claimedAt;
    }

    public UserJpaEntity getCompletedBy() {
        return this.completedBy;
    }

    public void setCompletedBy(UserJpaEntity completedBy) {
        this.completedBy = completedBy;
    }

    public Instant getCompletedAt() {
        return this.completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public Instant getDueAt() {
        return this.dueAt;
    }

    public void setDueAt(Instant dueAt) {
        this.dueAt = dueAt;
    }

}
