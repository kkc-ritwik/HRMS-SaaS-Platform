package com.hrms.notification.repository;

import com.hrms.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Optional<Notification> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Notification> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Notification> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<Notification> findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID employeeId, Pageable pageable);

    List<Notification> findByTenantIdAndEmployeeIdAndReadFalseAndDeletedFalse(
            String tenantId, UUID employeeId);
}
