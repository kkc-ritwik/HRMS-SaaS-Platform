package com.hrms.helpdesk.repository;

import com.hrms.helpdesk.entity.TicketCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    Optional<TicketCategory> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<TicketCategory> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<TicketCategory> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);
}
