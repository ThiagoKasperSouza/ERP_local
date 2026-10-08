package com.tks.erplocal.infrastructure.adapters.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "permission_code"}))
public class UserPermissionEntity {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "permission_code", nullable = false)
    private String permissionCode;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    @Column(name = "granted_by")
    private String grantedBy;

    protected UserPermissionEntity() {
    }

    public UserPermissionEntity(UUID id, UUID userId, String permissionCode, Instant grantedAt, String grantedBy) {
        this.id = id;
        this.userId = userId;
        this.permissionCode = permissionCode;
        this.grantedAt = grantedAt;
        this.grantedBy = grantedBy;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getPermissionCode() { return permissionCode; }
    public Instant getGrantedAt() { return grantedAt; }
    public String getGrantedBy() { return grantedBy; }
}
