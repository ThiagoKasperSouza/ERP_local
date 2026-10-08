package com.tks.erplocal.infrastructure.adapters.inbound.rest;

public record UpdateUserRequest(String name, String role, Boolean active, String password) {
}
