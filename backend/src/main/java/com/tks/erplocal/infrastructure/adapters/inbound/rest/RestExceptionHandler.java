package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.domain.users.exceptions.EmailAlreadyExistsException;
import com.tks.erplocal.domain.users.exceptions.InvalidCredentialsException;
import com.tks.erplocal.domain.users.exceptions.ServerMisconfiguredException;
import com.tks.erplocal.domain.users.exceptions.UserNotFoundException;
import com.tks.erplocal.domain.vault.exceptions.VaultAccessDeniedException;
import com.tks.erplocal.domain.vault.exceptions.VaultFileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> notFound(UserNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(EmailAlreadyExistsException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> badRequest(IllegalArgumentException e) {
        return Map.of("error", e.getMessage() == null ? "Bad request" : e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> unauthorized(InvalidCredentialsException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> forbidden(IllegalStateException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(ServerMisconfiguredException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> misconfigured(ServerMisconfiguredException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(VaultFileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> vaultNotFound(VaultFileNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(VaultAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, String> vaultDenied(VaultAccessDeniedException e) {
        return Map.of("error", e.getMessage());
    }
}
