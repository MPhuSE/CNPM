package com.flowops.requests.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CancelRequestDto {
    private Long requestVersion;

    @NotBlank(message = "Lý do hủy không được để trống")
    private String reason;

    public CancelRequestDto() {}

    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
