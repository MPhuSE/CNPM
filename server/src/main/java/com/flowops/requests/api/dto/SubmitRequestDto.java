package com.flowops.requests.api.dto;

public class SubmitRequestDto {
    private Long requestVersion;
    private Long editVersion;

    public SubmitRequestDto() {}

    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public Long getEditVersion() { return editVersion; }
    public void setEditVersion(Long editVersion) { this.editVersion = editVersion; }
}
