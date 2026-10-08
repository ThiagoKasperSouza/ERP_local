package com.tks.erplocal.infrastructure.adapters.permissions;

import com.tks.erplocal.domain.permissions.ports.PermissionRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.tks.erplocal.domain.permissions.model.PermissionsEnum;

@Component
public class PermissionSeeder implements CommandLineRunner {
    private final PermissionRepositoryPort repo;

    public PermissionSeeder(PermissionRepositoryPort repo) {
        this.repo = repo;
    }

    public void run(String... a){
        for(var p : PermissionsEnum.values())
            if(!repo.existsByCode(p.getCode()))
            repo.save(p);
    }
}