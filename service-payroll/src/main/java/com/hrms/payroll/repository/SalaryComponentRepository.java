package com.hrms.payroll.repository;

import com.hrms.payroll.entity.SalaryComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, UUID> {

    Optional<SalaryComponent> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<SalaryComponent> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<SalaryComponent> findByTenantIdAndActiveAndDeletedFalse(String tenantId, boolean active);

    List<SalaryComponent> findByTenantIdAndTypeAndDeletedFalse(
            String tenantId, SalaryComponent.ComponentType type);

    boolean existsByCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    boolean existsByCodeAndTenantIdAndDeletedFalseAndIdNot(String code, String tenantId, UUID id);

    @Query("SELECT sc FROM SalaryComponent sc WHERE sc.tenantId = :tenantId " +
           "AND sc.id IN :ids AND sc.deleted = false")
    List<SalaryComponent> findAllByIdInAndTenantId(@Param("ids") List<UUID> ids,
                                                    @Param("tenantId") String tenantId);
}
