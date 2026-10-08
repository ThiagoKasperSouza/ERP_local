package com.tks.erplocal.infrastructure.adapters.users;

import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.PasswordHasherPort;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Component
@Order(2)
public class AdminSeedRunner implements CommandLineRunner {

    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeedRunner(UserRepositoryPort users,
                           PasswordHasherPort hasher,
                           @Value("${app.admin.email:admin@local}") String adminEmail,
                           @Value("${app.admin.password:changeme}") String adminPassword) {
        this.users = users;
        this.hasher = hasher;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (users.existsByEmail(adminEmail)) {
            return;
        }
        User admin = new User(UUID.randomUUID(), "Admin", adminEmail,
                hasher.hash(adminPassword), Role.ADMIN, true, null,
                Instant.now(), new HashSet<>());
        users.save(admin);
    }
}
