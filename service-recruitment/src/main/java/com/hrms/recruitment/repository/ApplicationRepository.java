package com.hrms.recruitment.repository;

import com.hrms.recruitment.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    boolean existsByTenantIdAndRequisitionIdAndCandidateIdAndDeletedFalse(
            String tenantId, UUID requisitionId, UUID candidateId);

    Page<Application> findByTenantIdAndRequisitionIdAndDeletedFalseOrderByAppliedAtDesc(
            String tenantId, UUID requisitionId, Pageable pageable);

    Page<Application> findByTenantIdAndCandidateIdAndDeletedFalseOrderByAppliedAtDesc(
            String tenantId, UUID candidateId, Pageable pageable);

    Page<Application> findByTenantIdAndStageAndDeletedFalseOrderByStageChangedAtAsc(
            String tenantId, Application.ApplicationStage stage, Pageable pageable);

    List<Application> findByTenantIdAndDeletedFalse(String tenantId);

    /** Stage distribution for a requisition — used for pipeline funnel. */
    @Query("SELECT a.stage, COUNT(a) FROM Application a " +
           "WHERE a.tenantId = :tenantId AND a.requisitionId = :requisitionId " +
           "AND a.deleted = false GROUP BY a.stage")
    List<Object[]> stageBreakdown(@Param("tenantId")      String tenantId,
                                  @Param("requisitionId") UUID   requisitionId);

    /** Stage distribution across all requisitions. */
    @Query("SELECT a.stage, COUNT(a) FROM Application a " +
           "WHERE a.tenantId = :tenantId AND a.deleted = false GROUP BY a.stage")
    List<Object[]> stageBreakdownAll(@Param("tenantId") String tenantId);

    /** Hired applications for a requisition — for time-to-hire calculation. */
    List<Application> findByTenantIdAndRequisitionIdAndStageAndDeletedFalse(
            String tenantId, UUID requisitionId, Application.ApplicationStage stage);

    /** All applications in a date range — for volume analytics. */
    List<Application> findByTenantIdAndDeletedFalseAndAppliedAtBetween(
            String tenantId, Instant from, Instant to);

    int countByTenantIdAndCandidateIdAndDeletedFalse(String tenantId, UUID candidateId);
}
