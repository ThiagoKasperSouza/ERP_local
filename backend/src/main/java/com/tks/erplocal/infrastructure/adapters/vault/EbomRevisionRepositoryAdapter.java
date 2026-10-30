package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import com.tks.erplocal.domain.vault.model.EbomRevision;
import com.tks.erplocal.domain.vault.ports.EbomRevisionRepositoryPort;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class EbomRevisionRepositoryAdapter implements EbomRevisionRepositoryPort {
    private final EbomRevisionJpaRepository jpa;

    public EbomRevisionRepositoryAdapter(EbomRevisionJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public EbomRevision save(EbomRevision r) {
        EbomRevisionEntity e = toEntity(r);
        EbomRevisionEntity saved = jpa.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<EbomRevision> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<EbomRevision> findByAssemblyId(UUID assemblyId) {
        return jpa.findByAssemblyIdOrderByCreatedAtDesc(assemblyId).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<EbomRevision> findLatestByAssemblyId(UUID assemblyId) {
        List<EbomRevision> list = findByAssemblyId(assemblyId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private EbomRevision toDomain(EbomRevisionEntity e) {
        List<EbomEntry> items = e.getItems().stream().map(i -> {
            EbomEntry entry = new EbomEntry(i.getPartNumber(), i.getFileName());
            entry.setFileId(i.getFileId());
            return entry;
        }).collect(Collectors.toList());
        EbomRevision r = new EbomRevision(e.getId(), e.getAssemblyId(), e.getVersionLabel(),
                e.getMajor(), e.getMinor(), e.getReason(), e.getCreatedBy(), e.getCreatedAt(), items);
        r.setVersion(e.getVersion());
        r.setPendingApproval(e.isPendingApproval());
        r.setRejected(e.isRejected());
        r.setRejectionReason(e.getRejectionReason());
        r.setApprovedBy(e.getApprovedBy());
        r.setApprovedAt(e.getApprovedAt());
        return r;
    }

    private EbomRevisionEntity toEntity(EbomRevision r) {
        EbomRevisionEntity e = new EbomRevisionEntity();
        e.setId(r.getId());
        e.setAssemblyId(r.getAssemblyId());
        e.setVersionLabel(r.getVersionLabel());
        e.setMajor(r.getMajor());
        e.setMinor(r.getMinor());
        e.setReason(r.getReason());
        e.setCreatedBy(r.getCreatedBy());
        e.setCreatedAt(r.getCreatedAt() == null ? Instant.now() : r.getCreatedAt());
        e.setVersion(r.getVersion());
        e.setApprovedAt(r.getApprovedAt());
        e.setPendingApproval(r.isPendingApproval());
        e.setRejected(r.isRejected());
        e.setRejectionReason(r.getRejectionReason());
        e.setApprovedBy(r.getApprovedBy());
        List<EbomRevisionItemEntity> items = r.getItems().stream().map(entry -> {
            EbomRevisionItemEntity i = new EbomRevisionItemEntity();
            i.setRevisionId(r.getId());
            i.setPartNumber(entry.getPartNumber());
            i.setFileName(entry.getFileName());
            i.setFileId(entry.getFileId());
            return i;
        }).collect(Collectors.toList());
        e.setItems(items);
        return e;
    }
}
