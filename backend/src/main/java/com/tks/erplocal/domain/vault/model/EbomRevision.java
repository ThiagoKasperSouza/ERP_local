package com.tks.erplocal.domain.vault.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EbomRevision {
    private UUID id;
    private UUID assemblyId;
    private String versionLabel; // v1.0, v1.1, v2.0
    private int major;
    private int minor;
    private String reason;
    private UUID createdBy;
    private Instant createdAt;
    private List<EbomEntry> items;
    private boolean pendingApproval;
    private boolean rejected;
    private String rejectionReason;
    private UUID approvedBy;
    private Instant approvedAt;
    private long version; // optimistic locking

    public EbomRevision(UUID id, UUID assemblyId, String versionLabel, int major, int minor,
                        String reason, UUID createdBy, Instant createdAt, List<EbomEntry> items) {
        this.id = id;
        this.assemblyId = assemblyId;
        this.versionLabel = versionLabel;
        this.major = major;
        this.minor = minor;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.items = items;
        this.pendingApproval = true;
    }

    public UUID getId() { return id; }
    public UUID getAssemblyId() { return assemblyId; }
    public String getVersionLabel() { return versionLabel; }
    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public String getReason() { return reason; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public List<EbomEntry> getItems() { return items; }
    public boolean isPendingApproval() { return pendingApproval; }
    public void setPendingApproval(boolean pendingApproval) { this.pendingApproval = pendingApproval; }
    public boolean isRejected() { return rejected; }
    public void setRejected(boolean rejected) { this.rejected = rejected; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}