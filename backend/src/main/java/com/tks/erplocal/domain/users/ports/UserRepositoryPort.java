package com.tks.erplocal.domain.users.ports;

import com.tks.erplocal.domain.users.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    List<User> findAll(int page, int size);
    long count();
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
}
