package com.flowops.requests.api.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDraftRequest {
    @NotBlank(message = "workflowVersionId không được để trống")
    private String workflowVersionId;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    public CreateDraftRequest() {}

    public String getWorkflowVersionId() { return workflowVersionId; }
    public void setWorkflowVersionId(String workflowVersionId) { this.workflowVersionId = workflowVersionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
