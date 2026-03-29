package com.hrms.leave.repository;

import com.hrms.leave.entity.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

    Optional<Shift> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Shift> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<Shift> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalseAndIdNot(String code, String tenantId, UUID id);
}
