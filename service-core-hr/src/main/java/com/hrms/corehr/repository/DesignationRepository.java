package com.hrms.corehr.repository;

import com.hrms.corehr.entity.Designation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DesignationRepository extends JpaRepository<Designation, UUID> {

    Page<Designation> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<Designation> findByTenantIdAndDeletedFalse(String tenantId);

    Optional<Designation> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalseAndIdNot(String code, String tenantId, UUID id);

    List<Designation> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    @Query("SELECT d FROM Designation d WHERE d.tenantId = :tenantId AND d.deleted = false " +
           "AND (:search IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Designation> searchByTenant(@Param("tenantId") String tenantId,
                                     @Param("search") String search,
                                     Pageable pageable);

    long countByTenantIdAndDeletedFalse(String tenantId);
}
