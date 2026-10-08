package com.tks.erplocal.infrastructure.adapters.users;

import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;
    private final UserPermissionJpaRepository grants;

    public UserRepositoryAdapter(UserJpaRepository jpa, UserPermissionJpaRepository grants) {
        this.jpa = jpa;
        this.grants = grants;
    }

    @Override
    public List<User> findAll(int page, int size) {
        return jpa.findAllByOrderByCreatedAtAsc(PageRequest.of(page, size))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity saved = jpa.save(toEntity(user));
        syncGrants(saved.getId(), user.getPermissions(), user.getPermissionsGrantedBy());
        return toDomain(saved);
    }

    private void syncGrants(UUID userId, Set<String> codes, String grantedBy) {
        Set<String> desired = codes == null ? Set.of() : codes;
        Set<String> current = grants.findByUserId(userId).stream()
                .map(UserPermissionEntity::getPermissionCode).collect(Collectors.toSet());
        if (current.equals(desired)) {
            return;
        }
        grants.deleteByUserId(userId);
        Instant now = Instant.now();
        for (String code : desired) {
            grants.save(new UserPermissionEntity(UUID.randomUUID(), userId, code, now, grantedBy));
        }
    }

    private User toDomain(UserEntity e) {
        Set<String> permissions = grants.findByUserId(e.getId()).stream()
                .map(UserPermissionEntity::getPermissionCode).collect(Collectors.toSet());
        return new User(
                e.getId(), e.getName(), e.getEmail(), e.getPasswordHash(),
                e.getRole(), e.isActive(), e.getConsentAt(), e.getCreatedAt(), permissions);
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setName(u.getName());
        e.setEmail(u.getEmail());
        e.setPasswordHash(u.getPasswordHash());
        e.setRole(u.getRole());
        e.setActive(u.isActive());
        e.setConsentAt(u.getConsentAt());
        e.setCreatedAt(u.getCreatedAt());
        return e;
    }
}
