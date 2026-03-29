package com.hrms.reports.repository;

import com.hrms.reports.entity.DashboardWidget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardWidgetRepository extends JpaRepository<DashboardWidget, UUID> {

    Optional<DashboardWidget> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    org.springframework.data.domain.Page<DashboardWidget> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, org.springframework.data.domain.Pageable pageable);

    List<DashboardWidget> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<DashboardWidget> findByTenantIdAndDashboardIdAndDeletedFalseOrderByPositionYAscPositionXAsc(
            String tenantId, UUID dashboardId);
}
