package com.hrms.notification.repository;

import com.hrms.notification.entity.NotificationPreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<NotificationPreference> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, Pageable pageable);

    List<NotificationPreference> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<NotificationPreference> findByTenantIdAndEmployeeIdAndDeletedFalse(
            String tenantId, UUID employeeId);

    Optional<NotificationPreference> findByTenantIdAndEmployeeIdAndNotificationTypeAndDeletedFalse(
            String tenantId, UUID employeeId, NotificationPreference.NotificationType notificationType);
}
