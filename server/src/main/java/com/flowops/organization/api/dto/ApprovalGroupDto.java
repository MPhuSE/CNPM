package com.flowops.organization.api.dto;

import java.util.List;

public class ApprovalGroupDto {
    private String id;
    private String code;
    private String name;
    private Boolean isActive;
    private int memberCount;
    private List<MemberSummaryDto> members;

    public ApprovalGroupDto() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
    public List<MemberSummaryDto> getMembers() { return members; }
    public void setMembers(List<MemberSummaryDto> members) { this.members = members; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ApprovalGroupDto dto = new ApprovalGroupDto();
        public Builder id(String id) { dto.setId(id); return this; }
        public Builder code(String code) { dto.setCode(code); return this; }
        public Builder name(String name) { dto.setName(name); return this; }
        public Builder isActive(Boolean isActive) { dto.setIsActive(isActive); return this; }
        public Builder memberCount(int memberCount) { dto.setMemberCount(memberCount); return this; }
        public Builder members(List<MemberSummaryDto> members) { dto.setMembers(members); return this; }
        public ApprovalGroupDto build() { return dto; }
    }

    public static class MemberSummaryDto {
        private String id;
        private String username;
        private String fullName;

        public MemberSummaryDto() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private final MemberSummaryDto dto = new MemberSummaryDto();
            public Builder id(String id) { dto.setId(id); return this; }
            public Builder username(String username) { dto.setUsername(username); return this; }
            public Builder fullName(String fullName) { dto.setFullName(fullName); return this; }
            public MemberSummaryDto build() { return dto; }
        }
    }
}
