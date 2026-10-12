package com.tks.erplocal.infrastructure.adapters.vault;

import com.tks.erplocal.domain.vault.model.FileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VaultFileJpaRepository extends JpaRepository<VaultFileEntity, UUID> {

    /** Admin: tudo. */
    Page<VaultFileEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Comum: só aprovados de APPLICATION/PROTOTYPE (sem pendentes, sem TBO). */
    Page<VaultFileEntity> findByStatusInAndPendingApprovalFalseOrderByCreatedAtDesc(
            Iterable<FileStatus> statuses, Pageable pageable);

    /** Com CanApprove: aprovados + pendentes, mas sem TBO. */
    @Query("select f from VaultFileEntity f where f.status <> com.tks.erplocal.domain.vault.model.FileStatus.TBO order by f.createdAt desc")
    Page<VaultFileEntity> findNonTboOrderByCreatedAtDesc(Pageable pageable);

    Page<VaultFileEntity> findByProjectCodeOrderByCreatedAtDesc(String projectCode, Pageable pageable);

    @Query("select f from VaultFileEntity f where f.projectCode = :project "
            + "and f.status <> com.tks.erplocal.domain.vault.model.FileStatus.TBO order by f.createdAt desc")
    Page<VaultFileEntity> findNonTboByProjectOrderByCreatedAtDesc(@Param("project") String project, Pageable pageable);

    Page<VaultFileEntity> findByProjectCodeAndStatusInAndPendingApprovalFalseOrderByCreatedAtDesc(
            String projectCode, Iterable<FileStatus> statuses, Pageable pageable);

    @Query("select f.projectCode, count(f) from VaultFileEntity f "
            + "where f.projectCode is not null group by f.projectCode")
    List<Object[]> countByProject();

    @Query("select f.projectCode, count(f) from VaultFileEntity f "
            + "where f.projectCode is not null "
            + "and f.status <> com.tks.erplocal.domain.vault.model.FileStatus.TBO group by f.projectCode")
    List<Object[]> countNonTboByProject();

    @Query("select f.projectCode, count(f) from VaultFileEntity f "
            + "where f.projectCode is not null and f.pendingApproval = false "
            + "and f.status in (com.tks.erplocal.domain.vault.model.FileStatus.APPLICATION, "
            + "com.tks.erplocal.domain.vault.model.FileStatus.PROTOTYPE) group by f.projectCode")
    List<Object[]> countVisibleCommonByProject();

    /** Visibilidade de um arquivo avulso para não-admin sem CanApprove. */    @Query("select case when count(f) > 0 then true else false end from VaultFileEntity f "
            + "where f.id = :id and f.pendingApproval = false "
            + "and f.status in (com.tks.erplocal.domain.vault.model.FileStatus.APPLICATION, "
            + "com.tks.erplocal.domain.vault.model.FileStatus.PROTOTYPE)")
    boolean isVisibleToCommonUser(@Param("id") UUID id);
}
