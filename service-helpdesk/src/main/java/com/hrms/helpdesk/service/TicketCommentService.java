package com.hrms.helpdesk.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.helpdesk.dto.TicketCommentDto;
import com.hrms.helpdesk.entity.TicketComment;
import com.hrms.helpdesk.repository.TicketCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCommentService {

    private final TicketCommentRepository ticketCommentRepository;

    @Transactional
    public TicketCommentDto.Response create(String tenantId, String currentUser,
                                            TicketCommentDto.CreateRequest request) {
        TicketComment comment = new TicketComment();
        comment.setTenantId(tenantId);
        comment.setCreatedBy(currentUser);
        comment.setUpdatedBy(currentUser);
        comment.setTicketId(request.getTicketId());
        comment.setAuthorId(request.getAuthorId());
        comment.setComment(request.getComment());
        comment.setInternal(request.isInternal());
        comment.setAttachmentUrl(request.getAttachmentUrl());
        return toResponse(ticketCommentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public TicketCommentDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<TicketCommentDto.Response> list(String tenantId, Pageable pageable) {
        return ticketCommentRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentDto.Response> listByTicket(String tenantId, UUID ticketId) {
        return ticketCommentRepository
                .findByTenantIdAndTicketIdAndDeletedFalseOrderByCreatedAtAsc(tenantId, ticketId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketCommentDto.Response update(String tenantId, UUID id, String currentUser,
                                            TicketCommentDto.UpdateRequest request) {
        TicketComment comment = findOrThrow(tenantId, id);
        if (request.getComment() != null) comment.setComment(request.getComment());
        if (request.getAttachmentUrl() != null) comment.setAttachmentUrl(request.getAttachmentUrl());
        comment.setUpdatedBy(currentUser);
        return toResponse(ticketCommentRepository.save(comment));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        TicketComment comment = findOrThrow(tenantId, id);
        comment.setDeleted(true);
        comment.setUpdatedBy(currentUser);
        ticketCommentRepository.save(comment);
    }

    private TicketComment findOrThrow(String tenantId, UUID id) {
        return ticketCommentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketComment", "id", id));
    }

    private TicketCommentDto.Response toResponse(TicketComment c) {
        return TicketCommentDto.Response.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .ticketId(c.getTicketId())
                .authorId(c.getAuthorId())
                .comment(c.getComment())
                .internal(c.isInternal())
                .attachmentUrl(c.getAttachmentUrl())
                .createdBy(c.getCreatedBy())
                .updatedBy(c.getUpdatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
