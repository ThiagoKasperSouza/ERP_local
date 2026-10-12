package com.tks.erplocal.app.usecases.users;

import com.tks.erplocal.domain.users.model.AuthProvider;
import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.PasswordHasherPort;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import com.tks.erplocal.infrastructure.security.GoogleTokenVerifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

/** Login com Google: verifica o idToken e faz find-or-create por email. */
@Service
public class GoogleLoginUseCase {
    private final UserRepositoryPort users;
    private final PasswordHasherPort hasher;
    private final GoogleTokenVerifier verifier;

    public GoogleLoginUseCase(UserRepositoryPort users, PasswordHasherPort hasher,
                              GoogleTokenVerifier verifier) {
        this.users = users;
        this.hasher = hasher;
        this.verifier = verifier;
    }

    public User execute(String idToken) {
        GoogleTokenVerifier.GoogleAccount account = verifier.verify(idToken);
        return findOrCreate(account);
    }

    /** Fluxo PKCE do app Tauri: troca o code pelo idToken e segue o mesmo find-or-create. */
    public User executeWithCode(String code, String codeVerifier, String redirectUri) {
        String idToken = verifier.exchangeCodeForIdToken(code, codeVerifier, redirectUri);
        return execute(idToken);
    }

    private User findOrCreate(GoogleTokenVerifier.GoogleAccount account) {
        return users.findByEmail(account.email()).map(existing -> {
            if (!existing.isActive()) {
                throw new IllegalStateException("User is deactivated");
            }
            return existing;
        }).orElseGet(() -> {
            User user = new User(UUID.randomUUID(), account.name(), account.email(),
                    hasher.hash(UUID.randomUUID().toString()),
                    Role.USER, true, null, Instant.now(), new HashSet<>());
            user.setProvider(AuthProvider.GOOGLE);
            return users.save(user);
        });
    }
}
