package com.hrms.asset.repository;

import com.hrms.asset.entity.AssetAssignment;
import com.hrms.asset.entity.AssetAssignment.AssignmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, UUID> {

    Optional<AssetAssignment> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<AssetAssignment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<AssetAssignment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<AssetAssignment> findByTenantIdAndAssetIdAndDeletedFalse(String tenantId, UUID assetId);

    List<AssetAssignment> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    Optional<AssetAssignment> findByTenantIdAndAssetIdAndStatusAndDeletedFalse(
            String tenantId, UUID assetId, AssignmentStatus status);
}
