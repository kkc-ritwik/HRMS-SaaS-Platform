package com.hrms.onboarding.repository;

import com.hrms.onboarding.entity.BuddyAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuddyAssignmentRepository extends JpaRepository<BuddyAssignment, UUID> {

    Page<BuddyAssignment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<BuddyAssignment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Optional<BuddyAssignment> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<BuddyAssignment> findByTenantIdAndEmployeeIdAndDeletedFalse(String tenantId, UUID employeeId);
}
