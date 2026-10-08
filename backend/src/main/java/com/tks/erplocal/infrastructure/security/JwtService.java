package com.tks.erplocal.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final Algorithm algorithm;
    private final String issuer;
    private final long expirationHours;

    public JwtService(@Value("${app.jwt.secret:dev-secret-change-me}") String secret,
                      @Value("${app.jwt.issuer:erplocal}") String issuer,
                      @Value("${app.jwt.expiration-hours:12}") long expirationHours) {
        if ("dev-secret-change-me".equals(secret)) {
            log.warn("Usando JWT_SECRET de desenvolvimento. Defina JWT_SECRET em producao.");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationHours = expirationHours;
    }

    public String generate(User user) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getId().toString())
                .withClaim("email", user.getEmail())
                .withClaim("role", user.getRole().name())
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(expirationHours * 3600))
                .sign(algorithm);
    }

    public record Principal(UUID userId, String email, Role role) {}

    public Optional<Principal> verify(String token) {
        try {
            DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
            return Optional.of(new Principal(
                    UUID.fromString(jwt.getSubject()),
                    jwt.getClaim("email").asString(),
                    Role.fromString(jwt.getClaim("role").asString())));
        } catch (JWTVerificationException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
