package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetVaultFileUseCase {
    private final VaultFileRepositoryPort files;
    private final VaultVisibility visibility;

    public GetVaultFileUseCase(VaultFileRepositoryPort files, VaultVisibility visibility) {
        this.files = files;
        this.visibility = visibility;
    }

    public VaultFile execute(UUID viewerId, UUID fileId) {
        return visibility.requireVisible(viewerId, files.findById(fileId), fileId);
    }
}
