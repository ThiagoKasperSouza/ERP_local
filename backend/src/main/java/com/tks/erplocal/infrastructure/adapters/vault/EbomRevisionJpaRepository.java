package com.tks.erplocal.infrastructure.adapters.vault;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EbomRevisionJpaRepository extends JpaRepository<EbomRevisionEntity, UUID> {
    List<EbomRevisionEntity> findByAssemblyIdOrderByCreatedAtDesc(UUID assemblyId);
}
