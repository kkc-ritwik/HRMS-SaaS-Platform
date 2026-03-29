package com.hrms.compliance.repository;

import com.hrms.compliance.entity.License;
import com.hrms.compliance.entity.License.LicenseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LicenseRepository extends JpaRepository<License, UUID> {

    Optional<License> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<License> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<License> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<License> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    List<License> findByTenantIdAndStatusAndDeletedFalse(String tenantId, LicenseStatus status);
}
