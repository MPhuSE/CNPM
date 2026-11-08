package com.flowops.organization.api.dto;

public class DepartmentDto {
    private String id;
    private String code;
    private String name;
    private String parentId;
    private String parentName;
    private String managerId;
    private String managerName;
    private Boolean isActive;

    public DepartmentDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DepartmentDto dto = new DepartmentDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder code(String code) { dto.setCode(code); return this; }
        public Builder name(String name) { dto.setName(name); return this; }
        public Builder parentId(String parentId) { dto.setParentId(parentId); return this; }
        public Builder parentName(String parentName) { dto.setParentName(parentName); return this; }
        public Builder managerId(String managerId) { dto.setManagerId(managerId); return this; }
        public Builder managerName(String managerName) { dto.setManagerName(managerName); return this; }
        public Builder isActive(Boolean isActive) { dto.setIsActive(isActive); return this; }
        public DepartmentDto build() { return dto; }
    }
}
