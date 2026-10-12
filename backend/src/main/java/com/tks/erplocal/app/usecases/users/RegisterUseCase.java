package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.model.AuthProvider;
import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import org.springframework.stereotype.Service;

/** Auto-cadastro público: sempre USER/LOCAL (admin só via PUT /users/:id). */
@Service
public class RegisterUseCase {
    private final CreateUserUseCase createUserUseCase;

    public RegisterUseCase(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    public User execute(String name, String email, String plainPassword) {
        User user = createUserUseCase.execute(name, email, plainPassword, Role.USER);
        user.setProvider(AuthProvider.LOCAL);
        return user;
    }
}
