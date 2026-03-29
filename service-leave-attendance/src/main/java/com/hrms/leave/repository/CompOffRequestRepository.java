package com.hrms.leave.repository;

import com.hrms.leave.entity.CompOffRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompOffRequestRepository extends JpaRepository<CompOffRequest, UUID> {

    Optional<CompOffRequest> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<CompOffRequest> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);

    List<CompOffRequest> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, CompOffRequest.CompOffStatus status);
}
