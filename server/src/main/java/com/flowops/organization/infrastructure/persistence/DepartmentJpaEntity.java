package com.flowops.organization.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import com.flowops.shared.domain.BaseEntity;
import jakarta.persistence.*;
@Entity
@Table(name = "departments")
public class DepartmentJpaEntity extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DepartmentJpaEntity parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserJpaEntity manager;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public DepartmentJpaEntity() {}

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

    public DepartmentJpaEntity getParent() {
        return this.parent;
    }

    public void setParent(DepartmentJpaEntity parent) {
        this.parent = parent;
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

}
