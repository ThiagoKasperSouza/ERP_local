package com.tks.erplocal.infrastructure.adapters.inbound.rest;

import com.tks.erplocal.domain.vault.model.VaultFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record VaultFileResponse(UUID id, String name, String kind, String mimeType, long sizeBytes,
                                int versionNumber, String status, boolean pendingApproval,
                                UUID uploadedBy, UUID approver, String externalReference,
                                String projectCode, UUID parentId, String ebomReference,
                                List<VaultFileResponse> children,
                                Instant createdAt, Instant updatedAt) {
    public static VaultFileResponse from(VaultFile f) {
        List<VaultFileResponse> kids = f.getChildren() == null ? null
                : f.getChildren().stream().map(VaultFileResponse::from).toList();
        return new VaultFileResponse(f.getId(), f.getName(), f.getKind().name(), f.getMimeType(),
                f.getSizeBytes(), f.getVersionNumber(), f.getStatus().name(), f.isPendingApproval(),
                f.getUploadedBy(), f.getApprover(), f.getExternalReference(), f.getProjectCode(),
                f.getParentId(), f.getEbomReference(), kids, f.getCreatedAt(), f.getUpdatedAt());
    }
}
