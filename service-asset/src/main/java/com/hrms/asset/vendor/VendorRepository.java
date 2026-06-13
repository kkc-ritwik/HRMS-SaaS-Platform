package com.hrms.asset.vendor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, UUID> {

    Optional<Vendor> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<Vendor> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Vendor> findByTenantIdAndCategoryAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Vendor.Category category);
}
