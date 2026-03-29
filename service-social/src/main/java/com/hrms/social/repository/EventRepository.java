package com.hrms.social.repository;

import com.hrms.social.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Optional<Event> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Event> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Event> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<Event> findByTenantIdAndOrganizerIdAndDeletedFalse(String tenantId, UUID organizerId);

    List<Event> findByTenantIdAndGroupIdAndDeletedFalse(String tenantId, UUID groupId);
}
