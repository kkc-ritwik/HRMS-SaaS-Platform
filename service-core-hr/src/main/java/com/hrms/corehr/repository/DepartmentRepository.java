package com.hrms.corehr.repository;

import com.hrms.corehr.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    Page<Department> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<Department> findByTenantIdAndDeletedFalse(String tenantId);

    Optional<Department> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalseAndIdNot(String code, String tenantId, UUID id);

    List<Department> findByParentIdAndTenantIdAndDeletedFalse(UUID parentId, String tenantId);

    List<Department> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    @Query("SELECT d FROM Department d WHERE d.tenantId = :tenantId AND d.deleted = false " +
           "AND (:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Department> searchByTenant(@Param("tenantId") String tenantId,
                                    @Param("search") String search,
                                    Pageable pageable);

    long countByTenantIdAndDeletedFalse(String tenantId);

    long countByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);
}
