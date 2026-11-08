package com.flowops.organization.infrastructure.persistence;

import com.flowops.identity.infrastructure.persistence.UserJpaEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "approval_group_members")
public class ApprovalGroupMemberJpaEntity {

    @EmbeddedId
    private Pk id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private ApprovalGroupJpaEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private UserJpaEntity assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    @Embeddable
    public static class Pk implements Serializable {
        @Column(name = "group_id")
        private Long groupId;

        @Column(name = "user_id")
        private Long userId;

        public Pk(Long groupId, Long userId) {
            this.groupId = groupId;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(groupId, pk.groupId) && Objects.equals(userId, pk.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(groupId, userId);
        }
    }

    public ApprovalGroupMemberJpaEntity() {}

    public Pk getId() {
        return this.id;
    }

    public void setId(Pk id) {
        this.id = id;
    }

    public ApprovalGroupJpaEntity getGroup() {
        return this.group;
    }

    public void setGroup(ApprovalGroupJpaEntity group) {
        this.group = group;
    }

    public UserJpaEntity getUser() {
        return this.user;
    }

    public void setUser(UserJpaEntity user) {
        this.user = user;
    }

    public UserJpaEntity getAssignedBy() {
        return this.assignedBy;
    }

    public void setAssignedBy(UserJpaEntity assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Instant getAssignedAt() {
        return this.assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

}
