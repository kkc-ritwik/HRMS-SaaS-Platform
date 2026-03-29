package com.hrms.helpdesk.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.helpdesk.dto.TicketDto;
import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.entity.Ticket.Priority;
import com.hrms.helpdesk.entity.Ticket.TicketStatus;
import com.hrms.helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketDto.Response create(String tenantId, String currentUser,
                                     TicketDto.CreateRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTenantId(tenantId);
        ticket.setCreatedBy(currentUser);
        ticket.setUpdatedBy(currentUser);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategoryId(request.getCategoryId());
        ticket.setRequesterId(request.getRequesterId());
        ticket.setAssigneeId(request.getAssigneeId());
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setDueBy(request.getDueBy());
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public TicketDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<TicketDto.Response> list(String tenantId, Pageable pageable) {
        return ticketRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TicketDto.Response> listAll(String tenantId) {
        return ticketRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TicketDto.Response> listByRequester(String tenantId, UUID requesterId, Pageable pageable) {
        return ticketRepository
                .findByTenantIdAndRequesterIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, requesterId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TicketDto.Response> listByAssignee(String tenantId, UUID assigneeId, Pageable pageable) {
        return ticketRepository
                .findByTenantIdAndAssigneeIdAndDeletedFalse(tenantId, assigneeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TicketDto.Response> listByStatus(String tenantId, TicketStatus status, Pageable pageable) {
        return ticketRepository
                .findByTenantIdAndStatusAndDeletedFalse(tenantId, status, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public TicketDto.Response update(String tenantId, UUID id, String currentUser,
                                     TicketDto.UpdateRequest request) {
        Ticket ticket = findOrThrow(tenantId, id);
        if (request.getTitle() != null) ticket.setTitle(request.getTitle());
        if (request.getDescription() != null) ticket.setDescription(request.getDescription());
        if (request.getCategoryId() != null) ticket.setCategoryId(request.getCategoryId());
        if (request.getAssigneeId() != null) ticket.setAssigneeId(request.getAssigneeId());
        if (request.getPriority() != null) ticket.setPriority(request.getPriority());
        if (request.getStatus() != null) ticket.setStatus(request.getStatus());
        if (request.getSatisfactionRating() != null) ticket.setSatisfactionRating(request.getSatisfactionRating());
        if (request.getResolvedAt() != null) ticket.setResolvedAt(request.getResolvedAt());
        if (request.getClosedAt() != null) ticket.setClosedAt(request.getClosedAt());
        if (request.getDueBy() != null) ticket.setDueBy(request.getDueBy());
        ticket.setUpdatedBy(currentUser);
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto.Response resolve(String tenantId, UUID id, String currentUser) {
        Ticket ticket = findOrThrow(tenantId, id);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolvedAt(Instant.now());
        ticket.setUpdatedBy(currentUser);
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto.Response close(String tenantId, UUID id, String currentUser) {
        Ticket ticket = findOrThrow(tenantId, id);
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        ticket.setUpdatedBy(currentUser);
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Ticket ticket = findOrThrow(tenantId, id);
        ticket.setDeleted(true);
        ticket.setUpdatedBy(currentUser);
        ticketRepository.save(ticket);
    }

    private Ticket findOrThrow(String tenantId, UUID id) {
        return ticketRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
    }

    private TicketDto.Response toResponse(Ticket t) {
        return TicketDto.Response.builder()
                .id(t.getId())
                .tenantId(t.getTenantId())
                .title(t.getTitle())
                .description(t.getDescription())
                .categoryId(t.getCategoryId())
                .requesterId(t.getRequesterId())
                .assigneeId(t.getAssigneeId())
                .priority(t.getPriority())
                .status(t.getStatus())
                .resolvedAt(t.getResolvedAt())
                .closedAt(t.getClosedAt())
                .dueBy(t.getDueBy())
                .satisfactionRating(t.getSatisfactionRating())
                .createdBy(t.getCreatedBy())
                .updatedBy(t.getUpdatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
