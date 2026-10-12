package com.tks.erplocal.infrastructure.adapters.permissions;

import com.tks.erplocal.domain.permissions.model.PermissionsEnum;
import com.tks.erplocal.domain.permissions.ports.PermissionRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PermissionRepositoryAdapter implements PermissionRepositoryPort {

    private final PermissionJpaRepository jpa;

    public PermissionRepositoryAdapter(PermissionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<PermissionsEnum> findAll() {
        return jpa.findAll().stream()
                .map(e -> PermissionsEnum.fromCode(e.getCode()))
                .toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpa.existsById(code);
    }

    @Override
    public void save(PermissionsEnum p) {
        jpa.save(new PermissionEntity(p.getCode(), p.getDescription()));
    }
}
