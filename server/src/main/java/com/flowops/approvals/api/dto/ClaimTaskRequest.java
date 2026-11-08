package com.flowops.approvals.api.dto;

public class ClaimTaskRequest {
    private Long taskVersion;
    private Long requestVersion;
    private String revisionId;

    public ClaimTaskRequest() {}

    public Long getTaskVersion() { return taskVersion; }
    public void setTaskVersion(Long taskVersion) { this.taskVersion = taskVersion; }
    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public String getRevisionId() { return revisionId; }
    public void setRevisionId(String revisionId) { this.revisionId = revisionId; }
}
