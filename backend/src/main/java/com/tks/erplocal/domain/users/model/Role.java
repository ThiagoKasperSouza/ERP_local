package com.tks.erplocal.domain.users.model;

public enum Role {
    ADMIN,
    USER;

    public static Role fromString(String value) {
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
