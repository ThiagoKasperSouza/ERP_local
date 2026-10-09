package com.tks.erplocal.domain.vault.exceptions;

public class VaultAccessDeniedException extends RuntimeException {
    public VaultAccessDeniedException(String message) {
        super(message);
    }
}
