package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.EbomParserPort;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;

import java.util.List;
import java.util.UUID;

public class UploadAssemblyWithEbomUseCase {

    private final VaultFileRepositoryPort repository;
    private final EbomParserPort ebomParser;

    public UploadAssemblyWithEbomUseCase(VaultFileRepositoryPort repository, EbomParserPort ebomParser) {
        this.repository = repository;
        this.ebomParser = ebomParser;
    }

    public VaultFile execute(VaultFile assembly, byte[] ebomContent, String ebomFileName) {
        if (ebomContent == null || ebomContent.length == 0) {
            throw new IllegalArgumentException("EBOM é obrigatório para upload de montagem");
        }
        VaultFile savedAssembly = repository.save(assembly);

        List<EbomEntry> entries = ebomParser.parse(ebomContent, ebomFileName);
        for (EbomEntry entry : entries) {
            VaultFile child = new VaultFile(
                UUID.randomUUID(),
                entry.getFileName(),
                assembly.getKind(), // ou inferir do fileName
                "application/octet-stream",
                0,
                "",
                1,
                assembly.getStatus(),
                false,
                assembly.getUploadedBy(),
                null,
                null,
                null,
                null
            );
            child.setParentId(savedAssembly.getId());
            child.setEbomReference(savedAssembly.getId().toString());
            repository.save(child);
            entry.setFileId(child.getId());
        }
        return savedAssembly;
    }
}
