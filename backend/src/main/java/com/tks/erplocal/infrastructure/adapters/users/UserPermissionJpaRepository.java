package com.tks.erplocal.infrastructure.adapters.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserPermissionJpaRepository extends JpaRepository<UserPermissionEntity, UUID> {
    List<UserPermissionEntity> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);

    @Query("select g.userId from UserPermissionEntity g where g.permissionCode = :code")
    List<UUID> findUserIdsByPermissionCode(@Param("code") String code);
}
