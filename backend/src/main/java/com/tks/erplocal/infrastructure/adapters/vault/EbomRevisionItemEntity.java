package com.tks.erplocal.infrastructure.adapters.vault;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ebom_revision_items")
public class EbomRevisionItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "revision_id", nullable = false)
    private UUID revisionId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "part_number")
    private String partNumber;

    private String description;

    private int quantity;

    @Column(name = "file_id")
    private UUID fileId;

    protected EbomRevisionItemEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRevisionId() { return revisionId; }
    public void setRevisionId(UUID revisionId) { this.revisionId = revisionId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }
}
