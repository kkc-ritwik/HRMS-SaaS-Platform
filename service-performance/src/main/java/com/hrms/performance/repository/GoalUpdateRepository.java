package com.hrms.performance.repository;

import com.hrms.performance.entity.GoalUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalUpdateRepository extends JpaRepository<GoalUpdate, UUID> {

    Optional<GoalUpdate> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    List<GoalUpdate> findByGoalIdAndTenantIdAndDeletedFalseOrderByCreatedAtDesc(
            UUID goalId, String tenantId);
}
