package com.flowops.approvals.api.dto;

import com.flowops.approvals.domain.TaskAction;
import jakarta.validation.constraints.NotNull;

public class DecisionTaskRequest {
    @NotNull(message = "action không được để trống")
    private TaskAction action;

    private String reason;
    private Long taskVersion;
    private Long requestVersion;
    private String revisionId;

    public DecisionTaskRequest() {}

    public TaskAction getAction() { return action; }
    public void setAction(TaskAction action) { this.action = action; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getTaskVersion() { return taskVersion; }
    public void setTaskVersion(Long taskVersion) { this.taskVersion = taskVersion; }
    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }
}
