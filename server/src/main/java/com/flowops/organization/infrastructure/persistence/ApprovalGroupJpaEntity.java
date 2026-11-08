package com.flowops.organization.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "approval_groups")
public class ApprovalGroupJpaEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ApprovalGroupMemberJpaEntity> members = new HashSet<>();

    public ApprovalGroupJpaEntity() {}

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return this.isActive;
    }

    public Boolean isActive() {
        return this.isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Set<ApprovalGroupMemberJpaEntity> getMembers() {
        return this.members;
    }

    public void setMembers(Set<ApprovalGroupMemberJpaEntity> members) {
        this.members = members;
    }

}
