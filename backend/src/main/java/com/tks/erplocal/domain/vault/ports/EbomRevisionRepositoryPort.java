package com.tks.erplocal.domain.vault.ports;

import com.tks.erplocal.domain.vault.model.EbomRevision;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EbomRevisionRepositoryPort {
    EbomRevision save(EbomRevision revision);
    Optional<EbomRevision> findById(UUID id);
    List<EbomRevision> findByAssemblyId(UUID assemblyId);
    Optional<EbomRevision> findLatestByAssemblyId(UUID assemblyId);
}
