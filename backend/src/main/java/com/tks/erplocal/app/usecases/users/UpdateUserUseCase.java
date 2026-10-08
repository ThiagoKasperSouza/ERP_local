package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.exceptions.UserNotFoundException;
import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.PasswordHasherPort;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateUserUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;

    public UpdateUserUseCase(UserRepositoryPort users, PasswordHasherPort hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public User execute(UUID id, String name, Role role, Boolean active, String plainPassword) {
        User user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (role != null) {
            user.setRole(role);
        }
        if (active != null) {
            user.setActive(active);
        }
        if (plainPassword != null && !plainPassword.isBlank()) {
            user.setPasswordHash(hasher.hash(plainPassword));
        }
        return users.save(user);
    }
}
