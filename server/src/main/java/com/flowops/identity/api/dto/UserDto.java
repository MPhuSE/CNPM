package com.flowops.identity.api.dto;

import java.time.Instant;
import java.util.List;

public class UserDto {
    private String id;
    private String username;
    private String email;
    private String fullName;
    private String departmentId;
    private String departmentName;
    private String managerId;
    private String managerName;
    private Boolean isActive;
    private List<String> roles;
    private Long lockVersion;
    private Instant createdAt;

    public UserDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public Long getLockVersion() { return lockVersion; }
    public void setLockVersion(Long lockVersion) { this.lockVersion = lockVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UserDto dto = new UserDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder username(String username) { dto.setUsername(username); return this; }
        public Builder email(String email) { dto.setEmail(email); return this; }
        public Builder fullName(String fullName) { dto.setFullName(fullName); return this; }
        public Builder departmentId(String deptId) { dto.setDepartmentId(deptId); return this; }
        public Builder departmentName(String deptName) { dto.setDepartmentName(deptName); return this; }
        public Builder managerId(String managerId) { dto.setManagerId(managerId); return this; }
        public Builder managerName(String managerName) { dto.setManagerName(managerName); return this; }
        public Builder isActive(Boolean isActive) { dto.setIsActive(isActive); return this; }
        public Builder roles(List<String> roles) { dto.setRoles(roles); return this; }
        public Builder lockVersion(Long lockVersion) { dto.setLockVersion(lockVersion); return this; }
        public Builder createdAt(Instant createdAt) { dto.setCreatedAt(createdAt); return this; }
        public UserDto build() { return dto; }
    }
}
