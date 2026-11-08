package com.flowops.workflowconfig.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.organization.infrastructure.persistence.ApprovalGroupJpaEntity;
import com.flowops.workflowconfig.domain.AssigneeType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "workflow_steps")
public class WorkflowStepJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersionJpaEntity workflowVersion;

    @Column(name = "step_key", nullable = false, length = 100)
    private String stepKey;

    @Column(name = "step_name", nullable = false, length = 200)
    private String stepName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignee_type", nullable = false, length = 30)
    private AssigneeType assigneeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_user_id")
    private UserJpaEntity assigneeUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_group_id")
    private ApprovalGroupJpaEntity assigneeGroup;

    @Column(name = "sla_hours")
    private Integer slaHours;

    @Column(name = "allow_request_changes", nullable = false)
    private Boolean allowRequestChanges = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public WorkflowStepJpaEntity() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkflowVersionJpaEntity getWorkflowVersion() {
        return this.workflowVersion;
    }

    public void setWorkflowVersion(WorkflowVersionJpaEntity workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public String getStepKey() {
        return this.stepKey;
    }

    public void setStepKey(String stepKey) {
        this.stepKey = stepKey;
    }

    public String getStepName() {
        return this.stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public Integer getDisplayOrder() {
        return this.displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public AssigneeType getAssigneeType() {
        return this.assigneeType;
    }

    public void setAssigneeType(AssigneeType assigneeType) {
        this.assigneeType = assigneeType;
    }

    public UserJpaEntity getAssigneeUser() {
        return this.assigneeUser;
    }

    public void setAssigneeUser(UserJpaEntity assigneeUser) {
        this.assigneeUser = assigneeUser;
    }

    public ApprovalGroupJpaEntity getAssigneeGroup() {
        return this.assigneeGroup;
    }

    public void setAssigneeGroup(ApprovalGroupJpaEntity assigneeGroup) {
        this.assigneeGroup = assigneeGroup;
    }

    public Integer getSlaHours() {
        return this.slaHours;
    }

    public void setSlaHours(Integer slaHours) {
        this.slaHours = slaHours;
    }

    public Boolean getAllowRequestChanges() {
        return this.allowRequestChanges;
    }

    public Boolean isAllowRequestChanges() {
        return this.allowRequestChanges;
    }

    public void setAllowRequestChanges(Boolean allowRequestChanges) {
        this.allowRequestChanges = allowRequestChanges;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
