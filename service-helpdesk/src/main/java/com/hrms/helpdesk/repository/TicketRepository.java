package com.hrms.helpdesk.repository;

import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.entity.Ticket.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    @Query("SELECT t FROM Ticket t WHERE t.deleted = false " +
           "AND t.status IN (OPEN, " +
           "                 IN_PROGRESS) " +
           "AND t.dueBy IS NOT NULL AND t.dueBy < :now")
    List<Ticket> findOverdue(@Param("now") Instant now);

    @Query("SELECT t.assigneeId, COUNT(t) FROM Ticket t WHERE t.tenantId = :tenantId " +
           "AND t.deleted = false AND t.assigneeId IS NOT NULL " +
           "AND t.status IN (OPEN, " +
           "                 IN_PROGRESS) " +
           "GROUP BY t.assigneeId")
    List<Object[]> openTicketCountsByAssignee(@Param("tenantId") String tenantId);
}
