package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.users.model.Role;
import com.tks.erplocal.domain.users.model.User;
import com.tks.erplocal.domain.users.ports.UserRepositoryPort;
import com.tks.erplocal.domain.vault.exceptions.VaultAccessDeniedException;
import com.tks.erplocal.domain.vault.exceptions.VaultFileNotFoundException;
import com.tks.erplocal.domain.vault.model.FileStatus;
import com.tks.erplocal.domain.vault.model.VaultFile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Regras de visibilidade US3:
 * admin vê tudo; CanApprove vê pendentes mas não TBO;
 * comum vê só APPLICATION/PROTOTYPE aprovados.
 */
@Service
public class VaultVisibility {

    static final String CAN_APPROVE = "CanApprove";

    private final UserRepositoryPort users;

    public VaultVisibility(UserRepositoryPort users) {
        this.users = users;
    }

    public record Viewer(User user, boolean isAdmin, boolean canApprove) {}

    public Viewer viewer(UUID viewerId) {
        User user = users.findById(viewerId)
                .orElseThrow(() -> new VaultAccessDeniedException("Unknown user"));
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean canApprove = isAdmin || user.getPermissions().contains(CAN_APPROVE);
        return new Viewer(user, isAdmin, canApprove);
    }

    public VaultFile requireVisible(UUID viewerId, Optional<VaultFile> maybe, UUID fileId) {
        VaultFile file = maybe.orElseThrow(() -> new VaultFileNotFoundException(fileId));
        Viewer viewer = viewer(viewerId);
        if (viewer.isAdmin()) {
            return file;
        }
        if (file.getStatus() == FileStatus.TBO) {
            throw new VaultAccessDeniedException("File is obsolete (TBO)");
        }
        if (file.isPendingApproval() && !viewer.canApprove()) {
            throw new VaultAccessDeniedException("File is pending approval");
        }
        return file;
    }
}
