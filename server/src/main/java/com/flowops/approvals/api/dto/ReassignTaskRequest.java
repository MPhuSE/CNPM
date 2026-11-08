package com.flowops.approvals.api.dto;

import jakarta.validation.constraints.NotBlank;

public class ReassignTaskRequest {
    @NotBlank(message = "newAssigneeId không được để trống")
    private String newAssigneeId;

    @NotBlank(message = "Lý do phân công lại không được để trống")
    private String reason;

    private Long taskVersion;
    private Long requestVersion;

    public ReassignTaskRequest() {}

    public String getNewAssigneeId() { return newAssigneeId; }
    public void setNewAssigneeId(String newAssigneeId) { this.newAssigneeId = newAssigneeId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getTaskVersion() { return taskVersion; }
    public void setTaskVersion(Long taskVersion) { this.taskVersion = taskVersion; }
    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
}
