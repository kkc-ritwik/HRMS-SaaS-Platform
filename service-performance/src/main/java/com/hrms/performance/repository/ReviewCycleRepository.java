package com.hrms.performance.repository;

import com.hrms.performance.entity.ReviewCycle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewCycleRepository extends JpaRepository<ReviewCycle, UUID> {

    Optional<ReviewCycle> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<ReviewCycle> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<ReviewCycle> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, ReviewCycle.CycleStatus status);

    boolean existsByTenantIdAndNameAndDeletedFalse(String tenantId, String name);
}
