package com.tks.erplocal.infrastructure.adapters.inbound.rest;

public record CreateUserRequest(String name, String email, String password, String role) {
}
