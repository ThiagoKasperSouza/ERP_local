package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Agrupamento por projeto respeitando a mesma visibilidade da listagem. */
@Service
public class ListProjectsUseCase {
    private final VaultFileRepositoryPort files;
    private final VaultVisibility visibility;

    public ListProjectsUseCase(VaultFileRepositoryPort files, VaultVisibility visibility) {
        this.files = files;
        this.visibility = visibility;
    }

    public record ProjectGroup(String code, long fileCount) {}

    public List<ProjectGroup> execute(UUID viewerId) {
        VaultVisibility.Viewer viewer = visibility.viewer(viewerId);
        Map<String, Long> counts = files.countVisibleByProject(
                viewerId, viewer.isAdmin(), viewer.canApprove());
        return counts.entrySet().stream()
                .map(e -> new ProjectGroup(e.getKey(), e.getValue()))
                .sorted((a, b) -> a.code().compareToIgnoreCase(b.code()))
                .toList();
    }
}
