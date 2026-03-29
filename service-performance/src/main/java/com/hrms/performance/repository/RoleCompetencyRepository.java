package com.hrms.performance.repository;

import com.hrms.performance.entity.RoleCompetency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleCompetencyRepository extends JpaRepository<RoleCompetency, UUID> {

    Optional<RoleCompetency> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<RoleCompetency> findByTenantIdAndCompetencyIdAndDeletedFalse(
            String tenantId, UUID competencyId);

    List<RoleCompetency> findByTenantIdAndRoleIdAndDeletedFalse(String tenantId, UUID roleId);

    List<RoleCompetency> findByTenantIdAndDepartmentIdAndDeletedFalse(
            String tenantId, UUID departmentId);

    boolean existsByTenantIdAndCompetencyIdAndRoleIdAndDeletedFalse(
            String tenantId, UUID competencyId, UUID roleId);
}
