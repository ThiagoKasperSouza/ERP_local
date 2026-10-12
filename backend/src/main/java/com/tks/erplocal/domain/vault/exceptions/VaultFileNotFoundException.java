package com.tks.erplocal.domain.vault.exceptions;

import java.util.UUID;

public class VaultFileNotFoundException extends RuntimeException {
    public VaultFileNotFoundException(UUID id) {
        super("File not found: " + id);
    }
}
