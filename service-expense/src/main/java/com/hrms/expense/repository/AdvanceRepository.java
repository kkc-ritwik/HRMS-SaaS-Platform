package com.hrms.expense.repository;

import com.hrms.expense.entity.Advance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, UUID> {

    Optional<Advance> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Advance> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Advance> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Advance> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, UUID employeeId);
}
