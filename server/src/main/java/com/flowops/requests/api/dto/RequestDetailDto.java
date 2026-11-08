package com.flowops.requests.api.dto;

import com.flowops.requests.domain.RequestStatus;

import java.time.Instant;
import java.util.List;

public class RequestDetailDto {
    private String id;
    private String requestCode;
    private String workflowVersionId;
    private String workflowName;
    private Object formSchema;
    private String requesterId;
    private String requesterName;
    private String title;
    private RequestStatus status;
    private String currentRevisionId;
    private Integer currentRevisionNo;
    private Object formData;
    private Object workingCopyData;
    private Instant firstSubmittedAt;
    private Instant lastSubmittedAt;
    private String cancelReason;
    private Long requestVersion;
    private Long editVersion;
    private List<String> flags;
    private List<String> allowedActions;

    public RequestDetailDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRequestCode() { return requestCode; }
    public void setRequestCode(String requestCode) { this.requestCode = requestCode; }
    public String getWorkflowVersionId() { return workflowVersionId; }
    public void setWorkflowVersionId(String workflowVersionId) { this.workflowVersionId = workflowVersionId; }
    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
    public Object getFormSchema() { return formSchema; }
    public void setFormSchema(Object formSchema) { this.formSchema = formSchema; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public String getCurrentRevisionId() { return currentRevisionId; }
    public void setCurrentRevisionId(String currentRevisionId) { this.currentRevisionId = currentRevisionId; }
    public Integer getCurrentRevisionNo() { return currentRevisionNo; }
    public void setCurrentRevisionNo(Integer currentRevisionNo) { this.currentRevisionNo = currentRevisionNo; }
    public Object getFormData() { return formData; }
    public void setFormData(Object formData) { this.formData = formData; }
    public Object getWorkingCopyData() { return workingCopyData; }
    public void setWorkingCopyData(Object workingCopyData) { this.workingCopyData = workingCopyData; }
    public Instant getFirstSubmittedAt() { return firstSubmittedAt; }
    public void setFirstSubmittedAt(Instant firstSubmittedAt) { this.firstSubmittedAt = firstSubmittedAt; }
    public Instant getLastSubmittedAt() { return lastSubmittedAt; }
    public void setLastSubmittedAt(Instant lastSubmittedAt) { this.lastSubmittedAt = lastSubmittedAt; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public Long getEditVersion() { return editVersion; }
    public void setEditVersion(Long editVersion) { this.editVersion = editVersion; }
    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }
    public List<String> getAllowedActions() { return allowedActions; }
    public void setAllowedActions(List<String> allowedActions) { this.allowedActions = allowedActions; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RequestDetailDto dto = new RequestDetailDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder requestCode(String code) { dto.setRequestCode(code); return this; }
        public Builder workflowVersionId(String vId) { dto.setWorkflowVersionId(vId); return this; }
        public Builder workflowName(String name) { dto.setWorkflowName(name); return this; }
        public Builder formSchema(Object schema) { dto.setFormSchema(schema); return this; }
        public Builder requesterId(String id) { dto.setRequesterId(id); return this; }
        public Builder requesterName(String name) { dto.setRequesterName(name); return this; }
        public Builder title(String title) { dto.setTitle(title); return this; }
        public Builder status(RequestStatus status) { dto.setStatus(status); return this; }
        public Builder currentRevisionId(String id) { dto.setCurrentRevisionId(id); return this; }
        public Builder currentRevisionNo(Integer no) { dto.setCurrentRevisionNo(no); return this; }
        public Builder formData(Object data) { dto.setFormData(data); return this; }
        public Builder workingCopyData(Object data) { dto.setWorkingCopyData(data); return this; }
        public Builder firstSubmittedAt(Instant t) { dto.setFirstSubmittedAt(t); return this; }
        public Builder lastSubmittedAt(Instant t) { dto.setLastSubmittedAt(t); return this; }
        public Builder cancelReason(String reason) { dto.setCancelReason(reason); return this; }
        public Builder requestVersion(Long v) { dto.setRequestVersion(v); return this; }
        public Builder editVersion(Long v) { dto.setEditVersion(v); return this; }
        public Builder flags(List<String> flags) { dto.setFlags(flags); return this; }
        public Builder allowedActions(List<String> actions) { dto.setAllowedActions(actions); return this; }
        public RequestDetailDto build() { return dto; }
    }
}
