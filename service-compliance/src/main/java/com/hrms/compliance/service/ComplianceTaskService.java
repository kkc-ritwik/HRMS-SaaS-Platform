package com.hrms.compliance.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compliance.dto.ComplianceTaskDto;
import com.hrms.compliance.entity.ComplianceTask;
import com.hrms.compliance.entity.ComplianceTask.TaskStatus;
import com.hrms.compliance.repository.ComplianceTaskRepository;
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
public class ComplianceTaskService {

    private final ComplianceTaskRepository repository;

    @Transactional
    public ComplianceTaskDto.Response create(String tenantId, ComplianceTaskDto.CreateRequest request, String currentUser) {
        ComplianceTask entity = new ComplianceTask();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setComplianceItemId(request.getComplianceItemId());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setAssigneeId(request.getAssigneeId());
        entity.setDueDate(request.getDueDate());
        entity.setNotes(request.getNotes());
        entity.setStatus(TaskStatus.PENDING);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ComplianceTaskDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ComplianceTaskDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ComplianceTaskDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplianceTaskDto.Response> listByItem(String tenantId, UUID complianceItemId) {
        return repository.findByTenantIdAndComplianceItemIdAndDeletedFalse(tenantId, complianceItemId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ComplianceTaskDto.Response> listByAssignee(String tenantId, UUID assigneeId, Pageable pageable) {
        return repository.findByTenantIdAndAssigneeIdAndDeletedFalse(tenantId, assigneeId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ComplianceTaskDto.Response update(String tenantId, UUID id, ComplianceTaskDto.UpdateRequest request, String currentUser) {
        ComplianceTask entity = findOrThrow(tenantId, id);
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getAssigneeId() != null) entity.setAssigneeId(request.getAssigneeId());
        if (request.getDueDate() != null) entity.setDueDate(request.getDueDate());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getCompletedAt() != null) entity.setCompletedAt(request.getCompletedAt());
        if (request.getEvidenceUrl() != null) entity.setEvidenceUrl(request.getEvidenceUrl());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ComplianceTask entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public ComplianceTaskDto.Response complete(String tenantId, UUID id, String evidenceUrl, String currentUser) {
        ComplianceTask entity = findOrThrow(tenantId, id);
        entity.setStatus(TaskStatus.COMPLETED);
        entity.setCompletedAt(Instant.now());
        if (evidenceUrl != null) entity.setEvidenceUrl(evidenceUrl);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private ComplianceTask findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ComplianceTask", "id", id));
    }

    private ComplianceTaskDto.Response toResponse(ComplianceTask e) {
        return ComplianceTaskDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .complianceItemId(e.getComplianceItemId())
                .title(e.getTitle())
                .description(e.getDescription())
                .assigneeId(e.getAssigneeId())
                .dueDate(e.getDueDate())
                .status(e.getStatus())
                .completedAt(e.getCompletedAt())
                .evidenceUrl(e.getEvidenceUrl())
                .notes(e.getNotes())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
