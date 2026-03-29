package com.hrms.performance.repository;

import com.hrms.performance.entity.Competency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CompetencyRepository extends JpaRepository<Competency, UUID> {

    Optional<Competency> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    boolean existsByTenantIdAndNameAndDeletedFalse(String tenantId, String name);

    Page<Competency> findByTenantIdAndDeletedFalseOrderByNameAsc(String tenantId, Pageable pageable);

    List<Competency> findByTenantIdAndCategoryAndActiveAndDeletedFalse(
            String tenantId, Competency.CompetencyCategory category, boolean active);

    List<Competency> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);
}
