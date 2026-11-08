package com.flowops.identity.infrastructure.persistence;

import com.flowops.organization.infrastructure.persistence.DepartmentJpaEntity;
import com.flowops.shared.domain.BaseEntity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserJpaEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentJpaEntity department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserJpaEntity manager;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "auth_version", nullable = false)
    private Long authVersion = 0L;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleJpaEntity> roles = new HashSet<>();

    public UserJpaEntity() {}

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public DepartmentJpaEntity getDepartment() {
        return this.department;
    }

    public void setDepartment(DepartmentJpaEntity department) {
        this.department = department;
    }

    public UserJpaEntity getManager() {
        return this.manager;
    }

    public void setManager(UserJpaEntity manager) {
        this.manager = manager;
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

    public Long getAuthVersion() {
        return this.authVersion;
    }

    public void setAuthVersion(Long authVersion) {
        this.authVersion = authVersion;
    }

    public Set<RoleJpaEntity> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<RoleJpaEntity> roles) {
        this.roles = roles;
    }

}
