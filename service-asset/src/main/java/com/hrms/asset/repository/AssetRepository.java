package com.hrms.asset.repository;

import com.hrms.asset.entity.Asset;
import com.hrms.asset.entity.Asset.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {

    Optional<Asset> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Asset> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Asset> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Asset> findByTenantIdAndStatusAndDeletedFalse(String tenantId, AssetStatus status);

    List<Asset> findByTenantIdAndCategoryIdAndDeletedFalse(String tenantId, UUID categoryId);
}
