package com.tks.erplocal.domain.users.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private Instant consentAt;
    private Instant createdAt;
    private Set<String> permissions;
    private String permissionsGrantedBy;

    public User(UUID id, String name, String email, String passwordHash, Role role,
                boolean active, Instant consentAt, Instant createdAt, Set<String> permissions) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.consentAt = consentAt;
        this.createdAt = createdAt;
        this.permissions = permissions;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getConsentAt() { return consentAt; }
    public void setConsentAt(Instant consentAt) { this.consentAt = consentAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    public String getPermissionsGrantedBy() { return permissionsGrantedBy; }
    public void setPermissionsGrantedBy(String permissionsGrantedBy) { this.permissionsGrantedBy = permissionsGrantedBy; }
}
