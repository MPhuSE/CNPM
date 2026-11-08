package com.flowops.audit.infrastructure.persistence;

import com.flowops.approvals.infrastructure.persistence.TaskJpaEntity;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestRevisionJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "request_events")
public class RequestEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_key", nullable = false, unique = true)
    private UUID eventKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private RequestJpaEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id")
    private RequestRevisionJpaEntity revision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskJpaEntity task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private UserJpaEntity actor;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_assignee_id")
    private UserJpaEntity oldAssignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_assignee_id")
    private UserJpaEntity newAssignee;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String details = "{}";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public RequestEventJpaEntity() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getEventKey() {
        return this.eventKey;
    }

    public void setEventKey(UUID eventKey) {
        this.eventKey = eventKey;
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

    public TaskJpaEntity getTask() {
        return this.task;
    }

    public void setTask(TaskJpaEntity task) {
        this.task = task;
    }

    public UserJpaEntity getActor() {
        return this.actor;
    }

    public void setActor(UserJpaEntity actor) {
        this.actor = actor;
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public UserJpaEntity getOldAssignee() {
        return this.oldAssignee;
    }

    public void setOldAssignee(UserJpaEntity oldAssignee) {
        this.oldAssignee = oldAssignee;
    }

    public UserJpaEntity getNewAssignee() {
        return this.newAssignee;
    }

    public void setNewAssignee(UserJpaEntity newAssignee) {
        this.newAssignee = newAssignee;
    }

    public String getDetails() {
        return this.details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
