package com.hrms.compliance.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compliance.dto.ComplianceItemDto;
import com.hrms.compliance.entity.ComplianceItem;
import com.hrms.compliance.entity.ComplianceItem.ComplianceStatus;
import com.hrms.compliance.repository.ComplianceItemRepository;
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
public class ComplianceItemService {

    private final ComplianceItemRepository repository;

    @Transactional
    public ComplianceItemDto.Response create(String tenantId, ComplianceItemDto.CreateRequest request, String currentUser) {
        ComplianceItem entity = new ComplianceItem();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setRegulation(request.getRegulation());
        entity.setJurisdiction(request.getJurisdiction());
        entity.setDueDate(request.getDueDate());
        entity.setRecurring(request.isRecurring());
        entity.setRecurrencePeriod(request.getRecurrencePeriod());
        entity.setOwnerId(request.getOwnerId());
        entity.setNotes(request.getNotes());
        entity.setStatus(ComplianceStatus.PENDING);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ComplianceItemDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ComplianceItemDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ComplianceItemDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ComplianceItemDto.Response> listByStatus(String tenantId, ComplianceStatus status, Pageable pageable) {
        return repository.findByTenantIdAndStatusAndDeletedFalse(tenantId, status, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ComplianceItemDto.Response> listByOwner(String tenantId, UUID ownerId) {
        return repository.findByTenantIdAndOwnerIdAndDeletedFalse(tenantId, ownerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ComplianceItemDto.Response update(String tenantId, UUID id, ComplianceItemDto.UpdateRequest request, String currentUser) {
        ComplianceItem entity = findOrThrow(tenantId, id);
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getCategory() != null) entity.setCategory(request.getCategory());
        if (request.getRegulation() != null) entity.setRegulation(request.getRegulation());
        if (request.getJurisdiction() != null) entity.setJurisdiction(request.getJurisdiction());
        if (request.getDueDate() != null) entity.setDueDate(request.getDueDate());
        if (request.getRecurring() != null) entity.setRecurring(request.getRecurring());
        if (request.getRecurrencePeriod() != null) entity.setRecurrencePeriod(request.getRecurrencePeriod());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getOwnerId() != null) entity.setOwnerId(request.getOwnerId());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ComplianceItem entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public ComplianceItemDto.Response markCompliant(String tenantId, UUID id, String currentUser) {
        ComplianceItem entity = findOrThrow(tenantId, id);
        entity.setStatus(ComplianceStatus.COMPLIANT);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private ComplianceItem findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceItem", "id", id));
    }

    private ComplianceItemDto.Response toResponse(ComplianceItem e) {
        return ComplianceItemDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .title(e.getTitle())
                .description(e.getDescription())
                .category(e.getCategory())
                .regulation(e.getRegulation())
                .jurisdiction(e.getJurisdiction())
                .dueDate(e.getDueDate())
                .recurring(e.isRecurring())
                .recurrencePeriod(e.getRecurrencePeriod())
                .status(e.getStatus())
                .ownerId(e.getOwnerId())
                .notes(e.getNotes())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
