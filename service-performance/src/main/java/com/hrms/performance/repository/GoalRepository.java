package com.hrms.performance.repository;

import com.hrms.performance.entity.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

    Optional<Goal> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Goal> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID employeeId, Pageable pageable);

    List<Goal> findByTenantIdAndCycleIdAndDeletedFalse(String tenantId, UUID cycleId);

    List<Goal> findByTenantIdAndEmployeeIdAndCycleIdAndDeletedFalse(
            String tenantId, UUID employeeId, UUID cycleId);

    /** Key Results (children) of an Objective. */
    List<Goal> findByParentGoalIdAndTenantIdAndDeletedFalse(UUID parentGoalId, String tenantId);

    /** Top-level goals (no parent) for an employee. */
    List<Goal> findByTenantIdAndEmployeeIdAndParentGoalIdIsNullAndDeletedFalse(
            String tenantId, UUID employeeId);

    /** All active goals in a cycle for team view. */
    @Query("SELECT g FROM Goal g WHERE g.tenantId = :tenantId AND g.cycleId = :cycleId " +
           "AND g.deleted = false AND g.managerId = :managerId")
    List<Goal> findTeamGoals(@Param("tenantId") String tenantId,
                              @Param("cycleId")  UUID cycleId,
                              @Param("managerId") UUID managerId);
}
