package com.flowops.operations.api.dto;

import java.time.Instant;

public class IncidentDto {
    private String id;
    private String requestId;
    private String revisionId;
    private String incidentType;
    private String status;
    private String errorCode;
    private String details;
    private Instant openedAt;
    private Instant closedAt;

    public IncidentDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }
    public String getIncidentType() { return incidentType; }
    public void setIncidentType(String incidentType) { this.incidentType = incidentType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Instant getOpenedAt() { return openedAt; }
    public void setOpenedAt(Instant openedAt) { this.openedAt = openedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final IncidentDto dto = new IncidentDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder requestId(String reqId) { dto.setRequestId(reqId); return this; }
        public Builder revisionId(String revId) { dto.setRevisionId(revId); return this; }
        public Builder incidentType(String type) { dto.setIncidentType(type); return this; }
        public Builder status(String status) { dto.setStatus(status); return this; }
        public Builder errorCode(String code) { dto.setErrorCode(code); return this; }
        public Builder details(String details) { dto.setDetails(details); return this; }
        public Builder openedAt(Instant openedAt) { dto.setOpenedAt(openedAt); return this; }
        public Builder closedAt(Instant closedAt) { dto.setClosedAt(closedAt); return this; }
        public IncidentDto build() { return dto; }
    }
}
