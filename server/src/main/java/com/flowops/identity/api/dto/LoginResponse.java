package com.flowops.identity.api.dto;

import java.util.List;

public class LoginResponse {
    private String accessToken;
    private UserDto user;

    public LoginResponse() {}

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public UserDto getUser() { return user; }
    public void setUser(UserDto user) { this.user = user; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final LoginResponse resp = new LoginResponse();
        public Builder accessToken(String token) { resp.setAccessToken(token); return this; }
        public Builder user(UserDto user) { resp.setUser(user); return this; }
        public LoginResponse build() { return resp; }
    }

    public static class UserDto {
        private String id;
        private String username;
        private String email;
        private String fullName;
        private String departmentId;
        private String departmentName;
        private List<String> roles;

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
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final UserDto dto = new UserDto();
            public Builder id(String id) { dto.setId(id); return this; }
            public Builder username(String username) { dto.setUsername(username); return this; }
            public Builder email(String email) { dto.setEmail(email); return this; }
            public Builder fullName(String fullName) { dto.setFullName(fullName); return this; }
            public Builder departmentId(String deptId) { dto.setDepartmentId(deptId); return this; }
            public Builder departmentName(String deptName) { dto.setDepartmentName(deptName); return this; }
            public Builder roles(List<String> roles) { dto.setRoles(roles); return this; }
            public UserDto build() { return dto; }
        }
    }
}
