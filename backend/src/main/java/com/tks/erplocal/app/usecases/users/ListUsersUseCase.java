package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListUsersUseCase {
    private final UserRepositoryPort users;

    public ListUsersUseCase(UserRepositoryPort users) {
        this.users = users;
    }

    public record Page(int page, int size, long total, List<User> items) {}

    public Page execute(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        return new Page(safePage, safeSize, users.count(), users.findAll(safePage, safeSize));
    }
}
