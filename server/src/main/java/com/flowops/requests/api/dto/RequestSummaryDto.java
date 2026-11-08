package com.flowops.requests.api.dto;

import com.flowops.requests.domain.RequestStatus;

import java.time.Instant;
import java.util.List;

public class RequestSummaryDto {
    private String id;
    private String requestCode;
    private String workflowName;
    private String workflowCode;
    private Integer workflowVersionNo;
    private String title;
    private RequestStatus status;
    private String requesterName;
    private Instant firstSubmittedAt;
    private Instant lastSubmittedAt;
    private Instant completedAt;
    private List<String> flags;

    public RequestSummaryDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public String getWorkflowCode() { return workflowCode; }
    public void setWorkflowCode(String workflowCode) { this.workflowCode = workflowCode; }
    public Integer getWorkflowVersionNo() { return workflowVersionNo; }
    public void setWorkflowVersionNo(Integer workflowVersionNo) { this.workflowVersionNo = workflowVersionNo; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public Instant getFirstSubmittedAt() { return firstSubmittedAt; }
    public void setFirstSubmittedAt(Instant firstSubmittedAt) { this.firstSubmittedAt = firstSubmittedAt; }
    public Instant getLastSubmittedAt() { return lastSubmittedAt; }
    public void setLastSubmittedAt(Instant lastSubmittedAt) { this.lastSubmittedAt = lastSubmittedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RequestSummaryDto dto = new RequestSummaryDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder requestCode(String code) { dto.setRequestCode(code); return this; }
        public Builder workflowName(String name) { dto.setWorkflowName(name); return this; }
        public Builder workflowCode(String code) { dto.setWorkflowCode(code); return this; }
        public Builder workflowVersionNo(Integer no) { dto.setWorkflowVersionNo(no); return this; }
        public Builder title(String title) { dto.setTitle(title); return this; }
        public Builder status(RequestStatus status) { dto.setStatus(status); return this; }
        public Builder requesterName(String name) { dto.setRequesterName(name); return this; }
        public Builder firstSubmittedAt(Instant t) { dto.setFirstSubmittedAt(t); return this; }
        public Builder lastSubmittedAt(Instant t) { dto.setLastSubmittedAt(t); return this; }
        public Builder completedAt(Instant t) { dto.setCompletedAt(t); return this; }
        public Builder flags(List<String> flags) { dto.setFlags(flags); return this; }
        public RequestSummaryDto build() { return dto; }
    }
}
