package com.flowops.workflowconfig.infrastructure.persistence;

import com.flowops.workflowconfig.domain.TargetType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "workflow_transitions")
public class WorkflowTransitionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_version_id", nullable = false)
    private WorkflowVersionJpaEntity workflowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_step_id", nullable = false)
    private WorkflowStepJpaEntity fromStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_step_id")
    private WorkflowStepJpaEntity toStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_expression", nullable = false, columnDefinition = "jsonb")
    private String conditionExpression = "{}";

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public WorkflowTransitionJpaEntity() {}

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

    public WorkflowStepJpaEntity getFromStep() {
        return this.fromStep;
    }

    public void setFromStep(WorkflowStepJpaEntity fromStep) {
        this.fromStep = fromStep;
    }

    public TargetType getTargetType() {
        return this.targetType;
    }

    public void setTargetType(TargetType targetType) {
        this.targetType = targetType;
    }

    public WorkflowStepJpaEntity getToStep() {
        return this.toStep;
    }

    public void setToStep(WorkflowStepJpaEntity toStep) {
        this.toStep = toStep;
    }

    public String getConditionExpression() {
        return this.conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Boolean getIsDefault() {
        return this.isDefault;
    }

    public Boolean isDefault() {
        return this.isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
