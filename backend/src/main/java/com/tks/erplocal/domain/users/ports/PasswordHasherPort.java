package com.tks.erplocal.domain.users.ports;

public interface PasswordHasherPort {
    String hash(String plainPassword);
    boolean matches(String plainPassword, String passwordHash);
}
