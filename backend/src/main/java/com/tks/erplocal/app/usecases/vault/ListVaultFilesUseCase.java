package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.VaultFile;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListVaultFilesUseCase {
    private final VaultFileRepositoryPort files;
    private final VaultVisibility visibility;

    public ListVaultFilesUseCase(VaultFileRepositoryPort files, VaultVisibility visibility) {
        this.files = files;
        this.visibility = visibility;
    }

    public record Page(int page, int size, List<VaultFile> items) {}

    public Page execute(UUID viewerId, String projectCode, int page, int size) {
        VaultVisibility.Viewer viewer = visibility.viewer(viewerId);
        List<VaultFile> items = files.findVisible(
                viewerId, viewer.isAdmin(), viewer.canApprove(), projectCode, page, size);
        return new Page(page, size, items);
    }
}
