package com.flowops.operations.infrastructure.persistence;

import com.flowops.approvals.infrastructure.persistence.TaskJpaEntity;
import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.requests.infrastructure.persistence.RequestJpaEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "command_receipts")
public class CommandReceiptJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private UserJpaEntity actor;

    @Column(name = "command_type", nullable = false, length = 40)
    private String commandType;

    @Column(name = "client_key", nullable = false)
    private UUID clientKey;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private RequestJpaEntity request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private TaskJpaEntity task;

    @Column(name = "response_code")
    private Integer responseCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public CommandReceiptJpaEntity() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserJpaEntity getActor() {
        return this.actor;
    }

    public void setActor(UserJpaEntity actor) {
        this.actor = actor;
    }

    public String getCommandType() {
        return this.commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public UUID getClientKey() {
        return this.clientKey;
    }

    public void setClientKey(UUID clientKey) {
        this.clientKey = clientKey;
    }

    public String getPayloadHash() {
        return this.payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public RequestJpaEntity getRequest() {
        return this.request;
    }

    public void setRequest(RequestJpaEntity request) {
        this.request = request;
    }

    public TaskJpaEntity getTask() {
        return this.task;
    }

    public void setTask(TaskJpaEntity task) {
        this.task = task;
    }

    public Integer getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return this.completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

}
