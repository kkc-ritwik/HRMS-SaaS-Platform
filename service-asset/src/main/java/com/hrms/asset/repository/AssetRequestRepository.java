package com.hrms.asset.repository;

import com.hrms.asset.entity.AssetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssetRequestRepository extends JpaRepository<AssetRequest, UUID> {

    Optional<AssetRequest> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<AssetRequest> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<AssetRequest> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<AssetRequest> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);
}
