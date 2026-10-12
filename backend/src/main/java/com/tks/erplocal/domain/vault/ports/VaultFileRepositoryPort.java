package com.tks.erplocal.domain.vault.ports;

import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface VaultFileRepositoryPort {
    VaultFile save(VaultFile file);
    Optional<VaultFile> findById(UUID id);
    void delete(UUID id);
    long count();
    List<VaultFile> findVisible(UUID viewerId, boolean isAdmin, boolean canApprove,
                                String projectCode, int page, int size);
    Map<String, Long> countVisibleByProject(UUID viewerId, boolean isAdmin, boolean canApprove);
}
