package com.tks.erplocal.infrastructure.adapters.inbound.rest;

public record GoogleCodeLoginRequest(String code, String codeVerifier, String redirectUri) {
}
