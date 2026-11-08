package com.flowops.notifications.api.dto;

import java.time.Instant;

public class NotificationDto {
    private String id;
    private String eventKey;
    private String type;
    private String title;
    private String message;
    private String requestId;
    private String taskId;
    private Instant readAt;
    private Instant createdAt;

    public NotificationDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEventKey() { return eventKey; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final NotificationDto dto = new NotificationDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder eventKey(String key) { dto.setEventKey(key); return this; }
        public Builder type(String type) { dto.setType(type); return this; }
        public Builder title(String title) { dto.setTitle(title); return this; }
        public Builder message(String message) { dto.setMessage(message); return this; }
        public Builder requestId(String reqId) { dto.setRequestId(reqId); return this; }
        public Builder taskId(String taskId) { dto.setTaskId(taskId); return this; }
        public Builder readAt(Instant readAt) { dto.setReadAt(readAt); return this; }
        public Builder createdAt(Instant createdAt) { dto.setCreatedAt(createdAt); return this; }
        public NotificationDto build() { return dto; }
    }
}
