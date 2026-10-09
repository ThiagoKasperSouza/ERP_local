package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.model.FileKind;
import com.tks.erplocal.domain.vault.model.FileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vault_files", indexes = @Index(name = "idx_vault_files_project", columnList = "project_code"))
public class VaultFileEntity {
    @Id
    private UUID id;

    private String name;

    @Enumerated(EnumType.STRING)
    private FileKind kind;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Column(name = "pending_approval", nullable = false)
    private boolean pendingApproval;

    @Column(name = "uploaded_by", nullable = false)
    private UUID uploadedBy;

    private UUID approver;

    @Column(name = "external_reference")
    private String externalReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "project_code")
    private String projectCode;

    protected VaultFileEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FileKind getKind() { return kind; }
    public void setKind(FileKind kind) { this.kind = kind; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public FileStatus getStatus() { return status; }
    public void setStatus(FileStatus status) { this.status = status; }
    public boolean isPendingApproval() { return pendingApproval; }
    public void setPendingApproval(boolean pendingApproval) { this.pendingApproval = pendingApproval; }
    public UUID getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }
    public UUID getApprover() { return approver; }
    public void setApprover(UUID approver) { this.approver = approver; }
    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
}
