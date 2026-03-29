package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.JobRequisition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRequisitionRepository extends JpaRepository<JobRequisition, UUID> {

    Optional<JobRequisition> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<JobRequisition> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, Pageable pageable);

    Page<JobRequisition> findByTenantIdAndStatusAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, JobRequisition.RequisitionStatus status, Pageable pageable);

    /** Open positions for a department. */
    List<JobRequisition> findByTenantIdAndDepartmentIdAndStatusAndDeletedFalse(
            String tenantId, UUID departmentId, JobRequisition.RequisitionStatus status);

    /** Count open requisitions per status for dashboard. */
    @Query("SELECT r.status, COUNT(r) FROM JobRequisition r " +
           "WHERE r.tenantId = :tenantId AND r.deleted = false GROUP BY r.status")
    List<Object[]> countByStatus(@Param("tenantId") String tenantId);
}
