package com.hrms.offboarding.repository;

import com.hrms.offboarding.entity.Separation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeparationRepository extends JpaRepository<Separation, UUID> {

    Optional<Separation> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Separation> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Separation> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Separation> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);

    Page<Separation> findByTenantIdAndStatusAndDeletedFalse(String tenantId, Separation.SeparationStatus status, Pageable pageable);
}
