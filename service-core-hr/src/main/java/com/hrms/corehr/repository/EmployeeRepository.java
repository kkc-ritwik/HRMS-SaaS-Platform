package com.hrms.corehr.repository;

import com.hrms.corehr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Optional<Employee> findByEmployeeCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    Optional<Employee> findByEmailAndTenantIdAndDeletedFalse(String email, String tenantId);

    boolean existsByEmailAndTenantIdAndDeletedFalse(String email, String tenantId);

    boolean existsByEmailAndTenantIdAndDeletedFalseAndIdNot(String email, String tenantId, UUID id);

    boolean existsByEmployeeCodeAndTenantIdAndDeletedFalse(String code, String tenantId);

    Page<Employee> findByTenantIdAndDeletedFalse(String tenantId, Pageable pageable);

    List<Employee> findAllByTenantIdAndDeletedFalse(String tenantId);

    List<Employee> findByManagerIdAndTenantIdAndDeletedFalse(UUID managerId, String tenantId);

    List<Employee> findByDepartmentIdAndTenantIdAndDeletedFalse(UUID departmentId, String tenantId);

    long countByTenantIdAndDeletedFalse(String tenantId);

    long countByDepartmentIdAndTenantIdAndDeletedFalse(UUID departmentId, String tenantId);

    long countByTenantIdAndEmploymentStatusAndDeletedFalse(String tenantId,
                                                            Employee.EmploymentStatus status);

    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId AND e.deleted = false " +
           "AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchByTenant(@Param("tenantId") String tenantId,
                                   @Param("search") String search,
                                   Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.tenantId = :tenantId AND e.deleted = false " +
           "AND (:deptId IS NULL OR e.departmentId = :deptId) " +
           "AND (:locId IS NULL OR e.locationId = :locId) " +
           "AND (:status IS NULL OR e.employmentStatus = :status) " +
           "AND (:search IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> filterByTenant(@Param("tenantId") String tenantId,
                                   @Param("deptId") UUID deptId,
                                   @Param("locId") UUID locId,
                                   @Param("status") Employee.EmploymentStatus status,
                                   @Param("search") String search,
                                   Pageable pageable);
}
