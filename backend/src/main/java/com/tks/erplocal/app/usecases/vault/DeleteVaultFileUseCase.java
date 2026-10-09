package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.exceptions.VaultFileNotFoundException;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.FileStoragePort;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteVaultFileUseCase {
    private static final Logger log = LoggerFactory.getLogger(DeleteVaultFileUseCase.class);

    private final VaultFileRepositoryPort files;
    private final FileStoragePort storage;

    public DeleteVaultFileUseCase(VaultFileRepositoryPort files, FileStoragePort storage) {
        this.files = files;
        this.storage = storage;
    }

    public void execute(UUID fileId) {
        VaultFile file = files.findById(fileId).orElseThrow(() -> new VaultFileNotFoundException(fileId));
        try {
            storage.delete(file.getStorageKey());
        } catch (Exception e) {
            log.warn("Bytes do arquivo {} não puderam ser removidos: {}", fileId, e.getMessage());
        }
        files.delete(fileId);
    }
}
