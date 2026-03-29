package com.hrms.helpdesk.repository;

import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.entity.Ticket.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<Ticket> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<Ticket> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    Page<Ticket> findByTenantIdAndRequesterIdAndDeletedFalseOrderByCreatedAtDesc(
            String tenantId, UUID requesterId, Pageable pageable);

    Page<Ticket> findByTenantIdAndAssigneeIdAndDeletedFalse(
            String tenantId, UUID assigneeId, Pageable pageable);

    Page<Ticket> findByTenantIdAndStatusAndDeletedFalse(
            String tenantId, TicketStatus status, Pageable pageable);
}
