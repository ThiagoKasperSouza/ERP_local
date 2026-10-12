package com.tks.erplocal.domain.vault.model;

import java.util.UUID;

public class EbomEntry {
    private String partNumber;
    private String fileName;
    private UUID fileId; // referência ao VaultFile filho

    public EbomEntry(String partNumber, String fileName) {
        this.partNumber = partNumber;
        this.fileName = fileName;
    }

    public String getPartNumber() { return partNumber; }
    public String getFileName() { return fileName; }
    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }
}
