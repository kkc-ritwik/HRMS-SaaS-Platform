package com.hrms.helpdesk.repository;

import com.hrms.helpdesk.entity.TicketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, UUID> {

    Optional<TicketComment> findByIdAndTenantIdAndDeletedFalse(UUID id, String tenantId);

    Page<TicketComment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<TicketComment> findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(String tenantId);

    List<TicketComment> findByTenantIdAndTicketIdAndDeletedFalseOrderByCreatedAtAsc(
            String tenantId, UUID ticketId);
}
