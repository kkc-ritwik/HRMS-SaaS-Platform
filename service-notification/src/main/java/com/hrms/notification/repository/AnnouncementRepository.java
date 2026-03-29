package com.hrms.notification.repository;

import com.hrms.notification.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    Optional<Announcement> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Announcement> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Announcement> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
