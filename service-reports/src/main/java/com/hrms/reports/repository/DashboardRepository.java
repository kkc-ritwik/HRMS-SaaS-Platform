package com.hrms.reports.repository;

import com.hrms.reports.entity.Dashboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

    Optional<Dashboard> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Dashboard> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Dashboard> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Dashboard> findByTenantIdAndOwnerIdAndDeletedFalse(String tenantId, UUID ownerId);

    Page<Dashboard> findByTenantIdAndSharedAndDeletedFalse(String tenantId, boolean shared, Pageable pageable);

    Optional<Dashboard> findByTenantIdAndOwnerIdAndDefaultDashboardAndDeletedFalse(
            String tenantId, UUID ownerId, boolean defaultDashboard);
}
