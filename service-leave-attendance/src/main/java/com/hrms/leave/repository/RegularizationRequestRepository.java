package com.hrms.leave.repository;

import com.hrms.leave.entity.RegularizationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegularizationRequestRepository extends JpaRepository<RegularizationRequest, UUID> {

    Optional<RegularizationRequest> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<RegularizationRequest> findByEmployeeIdAndTenantIdAndDeletedFalse(
            UUID employeeId, String tenantId, Pageable pageable);

    List<RegularizationRequest> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, RegularizationRequest.RegularizationStatus status);

    List<RegularizationRequest> findByTenantIdAndEmployeeIdInAndStatusAndDeletedFalse(
            String tenantId, List<UUID> employeeIds,
            RegularizationRequest.RegularizationStatus status);
}
