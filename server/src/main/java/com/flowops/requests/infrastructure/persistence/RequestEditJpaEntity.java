package com.flowops.requests.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "request_edits")
public class RequestEditJpaEntity {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "request_id")
    private RequestJpaEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_revision_id")
    private RequestRevisionJpaEntity baseRevision;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_data", nullable = false, columnDefinition = "jsonb")
    private String formData = "{}";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", nullable = false)
    private UserJpaEntity updatedBy;

    @Version
    @Column(name = "lock_version", nullable = false)
    private Long lockVersion = 0L;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public RequestEditJpaEntity() {}

    public Long getRequestId() {
        return this.requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public RequestJpaEntity getRequest() {
        return this.request;
    }

    public void setRequest(RequestJpaEntity request) {
        this.request = request;
    }

    public RequestRevisionJpaEntity getBaseRevision() {
        return this.baseRevision;
    }

    public void setBaseRevision(RequestRevisionJpaEntity baseRevision) {
        this.baseRevision = baseRevision;
    }

    public String getFormData() {
        return this.formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public UserJpaEntity getUpdatedBy() {
        return this.updatedBy;
    }

    public void setUpdatedBy(UserJpaEntity updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getLockVersion() {
        return this.lockVersion;
    }

    public void setLockVersion(Long lockVersion) {
        this.lockVersion = lockVersion;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

}
