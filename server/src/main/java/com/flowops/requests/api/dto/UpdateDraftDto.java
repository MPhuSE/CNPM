package com.flowops.requests.api.dto;

import java.util.Map;

public class UpdateDraftDto {
    private String title;
    private Map<String, Object> formData;
    private Long requestVersion;
    private Long editVersion;

    public UpdateDraftDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Map<String, Object> getFormData() { return formData; }
    public void setFormData(Map<String, Object> formData) { this.formData = formData; }
    public Long getRequestVersion() { return requestVersion; }
    public void setRequestVersion(Long requestVersion) { this.requestVersion = requestVersion; }
    public Long getEditVersion() { return editVersion; }
    public void setEditVersion(Long editVersion) { this.editVersion = editVersion; }
}
