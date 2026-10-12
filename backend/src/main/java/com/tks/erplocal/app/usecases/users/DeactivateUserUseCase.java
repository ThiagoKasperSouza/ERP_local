package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.exceptions.UserNotFoundException;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateUserUseCase {
    private final UserRepositoryPort users;

    public DeactivateUserUseCase(UserRepositoryPort users) {
        this.users = users;
    }

    /** Soft delete: preserva o registro (auditoria/LGPD) e só desativa. */
    public User execute(UUID id) {
        User user = users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        return users.save(user);
    }
}
