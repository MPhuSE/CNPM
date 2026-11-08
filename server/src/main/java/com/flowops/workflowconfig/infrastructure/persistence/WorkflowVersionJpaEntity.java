package com.flowops.workflowconfig.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.shared.domain.BaseEntity;
import com.flowops.workflowconfig.domain.VersionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workflow_versions")
public class WorkflowVersionJpaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_definition_id", nullable = false)
    private WorkflowDefinitionJpaEntity workflowDefinition;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VersionStatus status = VersionStatus.DRAFT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_schema", nullable = false, columnDefinition = "jsonb")
    private String formSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_step_id")
    private WorkflowStepJpaEntity startStep;

    @Column(name = "change_note", columnDefinition = "TEXT")
    private String changeNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserJpaEntity createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private UserJpaEntity publishedBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToMany(mappedBy = "workflowVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowStepJpaEntity> steps = new ArrayList<>();

    @OneToMany(mappedBy = "workflowVersion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WorkflowTransitionJpaEntity> transitions = new ArrayList<>();

    public WorkflowVersionJpaEntity() {}

    public WorkflowDefinitionJpaEntity getWorkflowDefinition() {
        return this.workflowDefinition;
    }

    public void setWorkflowDefinition(WorkflowDefinitionJpaEntity workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public Integer getVersionNo() {
        return this.versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public VersionStatus getStatus() {
        return this.status;
    }

    public void setStatus(VersionStatus status) {
        this.status = status;
    }

    public String getFormSchema() {
        return this.formSchema;
    }

    public void setFormSchema(String formSchema) {
        this.formSchema = formSchema;
    }

    public WorkflowStepJpaEntity getStartStep() {
        return this.startStep;
    }

    public void setStartStep(WorkflowStepJpaEntity startStep) {
        this.startStep = startStep;
    }

    public String getChangeNote() {
        return this.changeNote;
    }

    public void setChangeNote(String changeNote) {
        this.changeNote = changeNote;
    }

    public UserJpaEntity getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(UserJpaEntity createdBy) {
        this.createdBy = createdBy;
    }

    public UserJpaEntity getPublishedBy() {
        return this.publishedBy;
    }

    public void setPublishedBy(UserJpaEntity publishedBy) {
        this.publishedBy = publishedBy;
    }

    public Instant getPublishedAt() {
        return this.publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<WorkflowStepJpaEntity> getSteps() {
        return this.steps;
    }

    public void setSteps(List<WorkflowStepJpaEntity> steps) {
        this.steps = steps;
    }

    public List<WorkflowTransitionJpaEntity> getTransitions() {
        return this.transitions;
    }

    public void setTransitions(List<WorkflowTransitionJpaEntity> transitions) {
        this.transitions = transitions;
    }

}
