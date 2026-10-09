package com.tks.erplocal.domain.vault.model;

import java.time.Instant;
import java.util.UUID;

public class VaultFile {
    private UUID id;
    private String name;
    private FileKind kind;
    private String mimeType;
    private long sizeBytes;
    private String storageKey;
    private int versionNumber;
    private FileStatus status;
    private boolean pendingApproval;
    private UUID uploadedBy;
    private UUID approver;
    private String externalReference;
    private Instant createdAt;
    private Instant updatedAt;
    private String projectCode;

    public VaultFile(UUID id, String name, FileKind kind, String mimeType, long sizeBytes,
                     String storageKey, int versionNumber, FileStatus status, boolean pendingApproval,
                     UUID uploadedBy, UUID approver, String externalReference,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.storageKey = storageKey;
        this.versionNumber = versionNumber;
        this.status = status;
        this.pendingApproval = pendingApproval;
        this.uploadedBy = uploadedBy;
        this.approver = approver;
        this.externalReference = externalReference;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FileKind getKind() { return kind; }
    public String getMimeType() { return mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public String getStorageKey() { return storageKey; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public FileStatus getStatus() { return status; }
    public void setStatus(FileStatus status) { this.status = status; }
    public boolean isPendingApproval() { return pendingApproval; }
    public void setPendingApproval(boolean pendingApproval) { this.pendingApproval = pendingApproval; }
    public UUID getUploadedBy() { return uploadedBy; }
    public UUID getApprover() { return approver; }
    public void setApprover(UUID approver) { this.approver = approver; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
