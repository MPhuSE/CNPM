package com.flowops.requests.api.dto;

public class ResubmitRequestDto {
    private Long requestVersion;
    private Long editVersion;
    private String baseRevisionId;

    public ResubmitRequestDto() {}

    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public Long getEditVersion() { return editVersion; }
    public void setEditVersion(Long editVersion) { this.editVersion = editVersion; }
    public String getBaseRevisionId() { return baseRevisionId; }
    public void setBaseRevisionId(String baseRevisionId) { this.baseRevisionId = baseRevisionId; }
}
