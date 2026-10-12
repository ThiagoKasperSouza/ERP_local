package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class VaultFileRepositoryAdapter implements VaultFileRepositoryPort {

    private final VaultFileJpaRepository jpa;

    public VaultFileRepositoryAdapter(VaultFileJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public VaultFile save(VaultFile file) {
        return toDomain(jpa.save(toEntity(file)));
    }

    @Override
    public Optional<VaultFile> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public long count() {
        return jpa.count();
    }

    @Override
    public List<VaultFile> findVisible(UUID viewerId, boolean isAdmin, boolean canApprove,
                                       String projectCode, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        var pageable = PageRequest.of(safePage, safeSize);
        boolean filtered = projectCode != null && !projectCode.isBlank();
        if (isAdmin) {
            var result = filtered
                    ? jpa.findByProjectCodeOrderByCreatedAtDesc(projectCode, pageable)
                    : jpa.findAllByOrderByCreatedAtDesc(pageable);
            return result.stream().map(this::toDomain).toList();
        }
        if (canApprove) {
            var result = filtered
                    ? jpa.findNonTboByProjectOrderByCreatedAtDesc(projectCode, pageable)
                    : jpa.findNonTboOrderByCreatedAtDesc(pageable);
            return result.stream().map(this::toDomain).toList();
        }
        var result = filtered
                ? jpa.findByProjectCodeAndStatusInAndPendingApprovalFalseOrderByCreatedAtDesc(
                        projectCode, Set.of(FileStatus.APPLICATION, FileStatus.PROTOTYPE), pageable)
                : jpa.findByStatusInAndPendingApprovalFalseOrderByCreatedAtDesc(
                        Set.of(FileStatus.APPLICATION, FileStatus.PROTOTYPE), pageable);
        return result.stream().map(this::toDomain).toList();
    }

    @Override
    public Map<String, Long> countVisibleByProject(UUID viewerId, boolean isAdmin, boolean canApprove) {
        List<Object[]> rows;
        if (isAdmin) {
            rows = jpa.countByProject();
        } else if (canApprove) {
            rows = jpa.countNonTboByProject();
        } else {
            rows = jpa.countVisibleCommonByProject();
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : rows) {
            counts.put((String) row[0], (Long) row[1]);
        }
        return counts;
    }

    public boolean isVisibleToCommonUser(UUID id) {
        return jpa.isVisibleToCommonUser(id);
    }

    private VaultFile toDomain(VaultFileEntity e) {
        VaultFile file = new VaultFile(
                e.getId(), e.getName(), e.getKind(), e.getMimeType(), e.getSizeBytes(),
                e.getStorageKey(), e.getVersionNumber(), e.getStatus(), e.isPendingApproval(),
                e.getUploadedBy(), e.getApprover(), e.getExternalReference(),
                e.getCreatedAt(), e.getUpdatedAt());
        file.setProjectCode(e.getProjectCode());
        return file;
    }

    private VaultFileEntity toEntity(VaultFile f) {
        VaultFileEntity e = new VaultFileEntity();
        e.setId(f.getId());
        e.setName(f.getName());
        e.setKind(f.getKind());
        e.setMimeType(f.getMimeType());
        e.setSizeBytes(f.getSizeBytes());
        e.setStorageKey(f.getStorageKey());
        e.setVersionNumber(f.getVersionNumber());
        e.setStatus(f.getStatus());
        e.setPendingApproval(f.isPendingApproval());
        e.setUploadedBy(f.getUploadedBy());
        e.setApprover(f.getApprover());
        e.setExternalReference(f.getExternalReference());
        e.setCreatedAt(f.getCreatedAt());
        e.setUpdatedAt(f.getUpdatedAt());
        e.setProjectCode(f.getProjectCode());
        return e;
    }
}
