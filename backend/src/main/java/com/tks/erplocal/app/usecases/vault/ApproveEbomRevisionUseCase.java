package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.EbomRevision;
import com.tks.erplocal.domain.vault.ports.EbomRevisionRepositoryPort;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
@Component
public class ApproveEbomRevisionUseCase {
    private final EbomRevisionRepositoryPort repo;
    public ApproveEbomRevisionUseCase(EbomRevisionRepositoryPort repo) { this.repo = repo; }
    public EbomRevision execute(UUID revisionId, UUID actorId, boolean approve, String justification) {
        EbomRevision r = repo.findById(revisionId).orElseThrow();
        if (!r.isPendingApproval()) throw new IllegalStateException("Revisão não está pendente");
        if (!approve && (justification == null || justification.isBlank())) throw new IllegalArgumentException("Justificativa obrigatória");
        r.setPendingApproval(false);
        r.setApprovedBy(actorId);
        r.setApprovedAt(Instant.now());
        if (!approve) { r.setRejected(true); r.setRejectionReason(justification); }
        return repo.save(r);
    }
}
