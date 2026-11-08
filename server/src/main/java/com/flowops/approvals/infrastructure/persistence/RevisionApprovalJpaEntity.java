package com.flowops.approvals.infrastructure.persistence;

import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestRevisionJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "revision_approvals")
public class RevisionApprovalJpaEntity {

    @EmbeddedId
    private Pk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestJpaEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("revisionId")
    @JoinColumn(name = "revision_id")
    private RequestRevisionJpaEntity revision;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("stepId")
    @JoinColumn(name = "workflow_step_id")
    private WorkflowStepJpaEntity workflowStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_task_id", nullable = false)
    private TaskJpaEntity sourceTask;

    @Column(name = "is_inherited", nullable = false)
    private Boolean isInherited;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Embeddable
    public static class Pk implements Serializable {
        @Column(name = "revision_id")
        private Long revisionId;

        @Column(name = "workflow_step_id")
        private Long stepId;

        public Pk(Long revisionId, Long stepId) {
            this.revisionId = revisionId;
            this.stepId = stepId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(revisionId, pk.revisionId) && Objects.equals(stepId, pk.stepId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(revisionId, stepId);
        }
    }

    public RevisionApprovalJpaEntity() {}

    public Pk getId() {
        return this.id;
    }

    public void setId(Pk id) {
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

    public WorkflowStepJpaEntity getWorkflowStep() {
        return this.workflowStep;
    }

    public void setWorkflowStep(WorkflowStepJpaEntity workflowStep) {
        this.workflowStep = workflowStep;
    }

    public TaskJpaEntity getSourceTask() {
        return this.sourceTask;
    }

    public void setSourceTask(TaskJpaEntity sourceTask) {
        this.sourceTask = sourceTask;
    }

    public Boolean getIsInherited() {
        return this.isInherited;
    }

    public Boolean isInherited() {
        return this.isInherited;
    }

    public void setIsInherited(Boolean isInherited) {
        this.isInherited = isInherited;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
