package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.domain.users.model.User;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, String role,
                           boolean active, Instant consentAt, Instant createdAt,
                           Set<String> permissions) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name(),
                u.isActive(), u.getConsentAt(), u.getCreatedAt(), Set.copyOf(u.getPermissions()));
    }
}
