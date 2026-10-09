package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.exceptions.VaultFileNotFoundException;
import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/** Admin pode ajustar nome/status/aprovador/referência externa (US9/US10 assumem depois). */
@Service
public class UpdateVaultFileMetadataUseCase {
    private final VaultFileRepositoryPort files;

    public UpdateVaultFileMetadataUseCase(VaultFileRepositoryPort files) {
        this.files = files;
    }

    public VaultFile execute(UUID fileId, String name, FileStatus status, UUID approver,
                             String externalReference, String projectCode) {
        VaultFile file = files.findById(fileId).orElseThrow(() -> new VaultFileNotFoundException(fileId));
        if (name != null && !name.isBlank()) {
            file.setName(name);
        }
        if (status != null) {
            file.setStatus(status);
        }
        if (approver != null) {
            file.setApprover(approver);
        }
        if (externalReference != null) {
            file.setExternalReference(externalReference.isBlank() ? null : externalReference);
        }
        if (projectCode != null) {
            file.setProjectCode(projectCode.isBlank() ? null : projectCode.trim());
        }
        file.setUpdatedAt(Instant.now());
        return files.save(file);
    }
}
