package com.hrms.helpdesk.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.helpdesk.dto.TicketCategoryDto;
import com.hrms.helpdesk.entity.TicketCategory;
import com.hrms.helpdesk.repository.TicketCategoryRepository;
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
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;

    @Transactional
    public TicketCategoryDto.Response create(String tenantId, String currentUser,
                                              TicketCategoryDto.CreateRequest request) {
        TicketCategory category = new TicketCategory();
        category.setTenantId(tenantId);
        category.setCreatedBy(currentUser);
        category.setUpdatedBy(currentUser);
        category.setName(request.getName());
        category.setCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setSlaHours(request.getSlaHours());
        category.setAutoAssignTo(request.getAutoAssignTo());
        category.setActive(request.isActive());
        return toResponse(ticketCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public TicketCategoryDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<TicketCategoryDto.Response> list(String tenantId, Pageable pageable) {
        return ticketCategoryRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TicketCategoryDto.Response> listAll(String tenantId) {
        return ticketCategoryRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketCategoryDto.Response update(String tenantId, UUID id, String currentUser,
                                              TicketCategoryDto.UpdateRequest request) {
        TicketCategory category = findOrThrow(tenantId, id);
        if (request.getName() != null) category.setName(request.getName());
        if (request.getCode() != null) category.setCode(request.getCode());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getSlaHours() != null) category.setSlaHours(request.getSlaHours());
        if (request.getAutoAssignTo() != null) category.setAutoAssignTo(request.getAutoAssignTo());
        if (request.getActive() != null) category.setActive(request.getActive());
        category.setUpdatedBy(currentUser);
        return toResponse(ticketCategoryRepository.save(category));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        TicketCategory category = findOrThrow(tenantId, id);
        category.setDeleted(true);
        category.setUpdatedBy(currentUser);
        ticketCategoryRepository.save(category);
    }

    private TicketCategory findOrThrow(String tenantId, UUID id) {
        return ticketCategoryRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketCategory", "id", id));
    }

    private TicketCategoryDto.Response toResponse(TicketCategory c) {
        return TicketCategoryDto.Response.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .name(c.getName())
                .code(c.getCode())
                .description(c.getDescription())
                .slaHours(c.getSlaHours())
                .autoAssignTo(c.getAutoAssignTo())
                .active(c.isActive())
                .createdBy(c.getCreatedBy())
                .updatedBy(c.getUpdatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
