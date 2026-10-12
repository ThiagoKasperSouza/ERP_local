package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.exceptions.InvalidCredentialsException;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.PasswordHasherPort;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;

@Service
public class LoginUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;

    public LoginUseCase(UserRepositoryPort users, PasswordHasherPort hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public User execute(String email, String plainPassword) {
        User user = users.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
        if (!user.isActive() || !hasher.matches(plainPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
