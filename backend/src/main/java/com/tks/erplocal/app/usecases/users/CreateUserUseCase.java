package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.exceptions.EmailAlreadyExistsException;
import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.PasswordHasherPort;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

@Service
public class CreateUserUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;

    public CreateUserUseCase(UserRepositoryPort users, PasswordHasherPort hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public User execute(String name, String email, String plainPassword, Role role) {
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        User user = new User(UUID.randomUUID(), name, email, hasher.hash(plainPassword),
                role == null ? Role.USER : role, true, null, Instant.now(), new HashSet<>());
        return users.save(user);
    }
}
