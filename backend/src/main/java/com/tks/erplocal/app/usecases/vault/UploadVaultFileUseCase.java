package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.FileKind;
import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.FileStoragePort;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class UploadVaultFileUseCase {
    private final VaultFileRepositoryPort files;
    private final FileStoragePort storage;

    public UploadVaultFileUseCase(VaultFileRepositoryPort files, FileStoragePort storage) {
        this.files = files;
        this.storage = storage;
    }

    public VaultFile execute(UUID uploadedBy, String originalFilename, String mimeType,
                             InputStream content, long sizeBytes, String externalReference,
                             String projectCode) throws IOException {
        String filename = originalFilename == null || originalFilename.isBlank() ? "arquivo" : originalFilename;
        String ext = extensionOf(filename);
        if (!FileKind.ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed: ." + ext);
        }
        String key = UUID.randomUUID() + "." + ext;
        storage.store(key, content, sizeBytes);
        Instant now = Instant.now();
        VaultFile file = new VaultFile(UUID.randomUUID(), filename, FileKind.fromFilename(filename),
                mimeType == null ? "application/octet-stream" : mimeType, sizeBytes, key,
                1, FileStatus.PROTOTYPE, false, uploadedBy, null, externalReference, now, now);
        file.setProjectCode(normalizeProjectCode(projectCode));
        return files.save(file);
    }

    static String normalizeProjectCode(String projectCode) {
        if (projectCode == null || projectCode.isBlank()) {
            return null;
        }
        return projectCode.trim();
    }

    static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }
}
