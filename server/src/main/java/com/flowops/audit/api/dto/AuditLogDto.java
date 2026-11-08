package com.flowops.audit.api.dto;

import java.time.Instant;

public class AuditLogDto {
    private String id;
    private String eventKey;
    private String actorName;
    private String action;
    private String entityType;
    private String entityId;
    private Object details;
    private String ipAddress;
    private Instant createdAt;

    public AuditLogDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public Object getDetails() { return details; }
    public void setDetails(Object details) { this.details = details; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final AuditLogDto dto = new AuditLogDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder eventKey(String key) { dto.setEventKey(key); return this; }
        public Builder actorName(String name) { dto.setActorName(name); return this; }
        public Builder action(String action) { dto.setAction(action); return this; }
        public Builder entityType(String type) { dto.setEntityType(type); return this; }
        public Builder entityId(String id) { dto.setEntityId(id); return this; }
        public Builder details(Object details) { dto.setDetails(details); return this; }
        public Builder ipAddress(String ip) { dto.setIpAddress(ip); return this; }
        public Builder createdAt(Instant t) { dto.setCreatedAt(t); return this; }
        public AuditLogDto build() { return dto; }
    }
}
