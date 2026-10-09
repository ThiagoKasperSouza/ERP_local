package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.domain.vault.model.VaultFile;

import java.time.Instant;
import java.util.UUID;

public record VaultFileResponse(UUID id, String name, String kind, String mimeType, long sizeBytes,
                                int versionNumber, String status, boolean pendingApproval,
                                UUID uploadedBy, UUID approver, String externalReference,
                                String projectCode,
                                Instant createdAt, Instant updatedAt) {
    public static VaultFileResponse from(VaultFile f) {
        return new VaultFileResponse(f.getId(), f.getName(), f.getKind().name(), f.getMimeType(),
                f.getSizeBytes(), f.getVersionNumber(), f.getStatus().name(), f.isPendingApproval(),
                f.getUploadedBy(), f.getApprover(), f.getExternalReference(), f.getProjectCode(),
                f.getCreatedAt(), f.getUpdatedAt());
    }
}
