package com.tks.erplocal.app.usecases.vault;

import com.tks.erplocal.domain.vault.model.EbomEntry;
import com.tks.erplocal.domain.vault.model.EbomRevision;
import com.tks.erplocal.domain.vault.ports.EbomRevisionRepositoryPort;
import com.tks.erplocal.domain.vault.ports.VaultFileRepositoryPort;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
@Component
public class CreateEbomRevisionUseCase {
    private final EbomRevisionRepositoryPort revisionRepo;
    private final VaultFileRepositoryPort fileRepo;

    public CreateEbomRevisionUseCase(EbomRevisionRepositoryPort revisionRepo, VaultFileRepositoryPort fileRepo) {
        this.revisionRepo = revisionRepo;
        this.fileRepo = fileRepo;
    }

    public EbomRevision execute(UUID assemblyId, List<EbomEntry> items, String reason, UUID createdBy, boolean major) {
        var latest = revisionRepo.findLatestByAssemblyId(assemblyId);
        int majorV = 1, minorV = 0;
        if (latest.isPresent()) {
            majorV = latest.get().getMajor();
            minorV = latest.get().getMinor();
            if (major) { majorV++; minorV = 0; } else { minorV++; }
        }
        String label = "v" + majorV + "." + minorV;
        EbomRevision r = new EbomRevision(UUID.randomUUID(), assemblyId, label, majorV, minorV,
                reason, createdBy, Instant.now(), items);
        return revisionRepo.save(r);
    }
}
