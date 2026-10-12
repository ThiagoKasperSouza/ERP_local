package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.FileStoragePort;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class DownloadVaultFileUseCase {
    private final VaultFileRepositoryPort files;
    private final FileStoragePort storage;
    private final VaultVisibility visibility;

    public DownloadVaultFileUseCase(VaultFileRepositoryPort files, FileStoragePort storage,
                                    VaultVisibility visibility) {
        this.files = files;
        this.storage = storage;
        this.visibility = visibility;
    }

    public record Download(VaultFile file, InputStream content) {}

    public Download execute(UUID viewerId, UUID fileId) throws IOException {
        VaultFile file = visibility.requireVisible(viewerId, files.findById(fileId), fileId);
        return new Download(file, storage.load(file.getStorageKey()));
    }
}
