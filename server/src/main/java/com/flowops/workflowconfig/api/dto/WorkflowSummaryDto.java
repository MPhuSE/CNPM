package com.flowops.workflowconfig.api.dto;

public class WorkflowSummaryDto {
    private String id;
    private String code;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer activeVersionNo;
    private String activeVersionId;

    public WorkflowSummaryDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public Integer getActiveVersionNo() { return activeVersionNo; }
    public void setActiveVersionNo(Integer activeVersionNo) { this.activeVersionNo = activeVersionNo; }
    public String getActiveVersionId() { return activeVersionId; }
    public void setActiveVersionId(String activeVersionId) { this.activeVersionId = activeVersionId; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final WorkflowSummaryDto dto = new WorkflowSummaryDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder code(String code) { dto.setCode(code); return this; }
        public Builder name(String name) { dto.setName(name); return this; }
        public Builder description(String description) { dto.setDescription(description); return this; }
        public Builder isActive(Boolean isActive) { dto.setIsActive(isActive); return this; }
        public Builder activeVersionNo(Integer activeVersionNo) { dto.setActiveVersionNo(activeVersionNo); return this; }
        public Builder activeVersionId(String activeVersionId) { dto.setActiveVersionId(activeVersionId); return this; }
        public WorkflowSummaryDto build() { return dto; }
    }
}
