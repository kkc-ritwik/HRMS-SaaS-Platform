package com.hrms.asset.repository;

import com.hrms.asset.entity.AssetMaintenance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, UUID> {

    Optional<AssetMaintenance> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<AssetMaintenance> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<AssetMaintenance> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<AssetMaintenance> findByTenantIdAndAssetIdAndDeletedFalse(String tenantId, UUID assetId);
}
