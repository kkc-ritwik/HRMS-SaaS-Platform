package com.hrms.performance.repository;

import com.hrms.performance.entity.OneOnOne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OneOnOneRepository extends JpaRepository<OneOnOne, UUID> {

    Optional<OneOnOne> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<OneOnOne> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByScheduledAtDesc(
            String tenantId, UUID employeeId, Pageable pageable);

    Page<OneOnOne> findByTenantIdAndManagerIdAndDeletedFalseOrderByScheduledAtDesc(
            String tenantId, UUID managerId, Pageable pageable);

    Page<OneOnOne> findByTenantIdAndManagerIdAndEmployeeIdAndDeletedFalseOrderByScheduledAtDesc(
            String tenantId, UUID managerId, UUID employeeId, Pageable pageable);
}
