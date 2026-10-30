package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ebom_revisions")
public class EbomRevisionEntity {
    @Id
    private UUID id;

    @Column(name = "assembly_id", nullable = false)
    private UUID assemblyId;

    @Column(name = "version_label", nullable = false)
    private String versionLabel;

    private int major;
    private int minor;

    private String reason;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "pending_approval", nullable = false)
    private boolean pendingApproval;

    @Column(name = "rejected", nullable = false)
    private boolean rejected;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Version
    private long version;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "revision_id")
    private List<EbomRevisionItemEntity> items = new ArrayList<>();

    protected EbomRevisionEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAssemblyId() { return assemblyId; }
    public void setAssemblyId(UUID assemblyId) { this.assemblyId = assemblyId; }
    public String getVersionLabel() { return versionLabel; }
    public void setVersionLabel(String versionLabel) { this.versionLabel = versionLabel; }
    public int getMajor() { return major; }
    public void setMajor(int major) { this.major = major; }
    public int getMinor() { return minor; }
    public void setMinor(int minor) { this.minor = minor; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public boolean isPendingApproval() { return pendingApproval; }
    public void setPendingApproval(boolean pendingApproval) { this.pendingApproval = pendingApproval; }
    public boolean isRejected() { return rejected; }
    public void setRejected(boolean rejected) { this.rejected = rejected; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public UUID getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UUID approvedBy) { this.approvedBy = approvedBy; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
    public List<EbomRevisionItemEntity> getItems() { return items; }
    public void setItems(List<EbomRevisionItemEntity> items) { this.items = items; }
}
