package com.hrms.workflow.repository;

import com.hrms.workflow.entity.WorkflowInstance;
import com.hrms.workflow.entity.WorkflowInstance.InstanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, UUID> {

    Optional<WorkflowInstance> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<WorkflowInstance> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<WorkflowInstance> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<WorkflowInstance> findByTenantIdAndEntityIdAndDeletedFalse(String tenantId, UUID entityId);

    Page<WorkflowInstance> findByTenantIdAndInitiatedByAndDeletedFalse(String tenantId, UUID initiatedBy, Pageable pageable);

    Page<WorkflowInstance> findByTenantIdAndStatusAndDeletedFalse(String tenantId, InstanceStatus status, Pageable pageable);

    @Query("SELECT w FROM WorkflowInstance w WHERE w.deleted = false " +
           "AND w.status IN (com.hrms.workflow.entity.WorkflowInstance.InstanceStatus.PENDING, " +
           "                 com.hrms.workflow.entity.WorkflowInstance.InstanceStatus.IN_PROGRESS) " +
           "AND w.slaDueAt IS NOT NULL AND w.slaDueAt < :now")
    List<WorkflowInstance> findOverdue(@Param("now") Instant now);
}
