package com.flowops.requests.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.workflowconfig.infrastructure.persistence.WorkflowStepJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "request_revisions")
public class RequestRevisionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestJpaEntity request;

    @Column(name = "revision_no", nullable = false)
    private Integer revisionNo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data", nullable = false, columnDefinition = "jsonb")
    private String formData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "changed_fields", nullable = false, columnDefinition = "jsonb")
    private String changedFields = "[]";

    @Column(name = "restart_required", nullable = false)
    private Boolean restartRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_step_id", nullable = false)
    private WorkflowStepJpaEntity resumeStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private UserJpaEntity submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt = Instant.now();

    public RequestRevisionJpaEntity() {}

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

    public Integer getRevisionNo() {
        return this.revisionNo;
    }

    public void setRevisionNo(Integer revisionNo) {
        this.revisionNo = revisionNo;
    }

    public String getFormData() {
        return this.formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public String getChangedFields() {
        return this.changedFields;
    }

    public void setChangedFields(String changedFields) {
        this.changedFields = changedFields;
    }

    public Boolean getRestartRequired() {
        return this.restartRequired;
    }

    public Boolean isRestartRequired() {
        return this.restartRequired;
    }

    public void setRestartRequired(Boolean restartRequired) {
        this.restartRequired = restartRequired;
    }

    public WorkflowStepJpaEntity getResumeStep() {
        return this.resumeStep;
    }

    public void setResumeStep(WorkflowStepJpaEntity resumeStep) {
        this.resumeStep = resumeStep;
    }

    public UserJpaEntity getSubmittedBy() {
        return this.submittedBy;
    }

    public void setSubmittedBy(UserJpaEntity submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Instant getSubmittedAt() {
        return this.submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

}
