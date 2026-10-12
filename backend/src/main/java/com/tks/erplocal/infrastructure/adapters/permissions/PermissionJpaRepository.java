package com.tks.erplocal.infrastructure.adapters.permissions;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, String> {
}
