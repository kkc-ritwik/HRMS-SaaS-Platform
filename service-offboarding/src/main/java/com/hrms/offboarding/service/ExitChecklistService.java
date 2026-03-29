package com.hrms.offboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.offboarding.dto.ExitChecklistDto;
import com.hrms.offboarding.entity.ExitChecklist;
import com.hrms.offboarding.repository.ExitChecklistRepository;
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
public class ExitChecklistService {

    private final ExitChecklistRepository exitChecklistRepository;

    @Transactional
    public ExitChecklistDto.Response create(String tenantId, ExitChecklistDto.CreateRequest req, String currentUser) {
        ExitChecklist checklist = new ExitChecklist();
        checklist.setTenantId(tenantId);
        checklist.setSeparationId(req.getSeparationId());
        checklist.setTaskTitle(req.getTaskTitle());
        checklist.setTaskCategory(req.getTaskCategory());
        checklist.setAssignedTo(req.getAssignedTo());
        checklist.setDueDate(req.getDueDate());
        checklist.setNotes(req.getNotes());
        checklist.setCompleted(false);
        checklist.setCreatedBy(currentUser);
        checklist.setUpdatedBy(currentUser);
        return toResponse(exitChecklistRepository.save(checklist));
    }

    @Transactional(readOnly = true)
    public ExitChecklistDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ExitChecklistDto.Response> list(String tenantId, Pageable pageable) {
        return exitChecklistRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ExitChecklistDto.Response update(String tenantId, UUID id, ExitChecklistDto.UpdateRequest req, String currentUser) {
        ExitChecklist checklist = getEntity(tenantId, id);
        if (req.getTaskTitle() != null) {
            checklist.setTaskTitle(req.getTaskTitle());
        }
        if (req.getTaskCategory() != null) {
            checklist.setTaskCategory(req.getTaskCategory());
        }
        if (req.getAssignedTo() != null) {
            checklist.setAssignedTo(req.getAssignedTo());
        }
        if (req.getDueDate() != null) {
            checklist.setDueDate(req.getDueDate());
        }
        if (req.getNotes() != null) {
            checklist.setNotes(req.getNotes());
        }
        if (req.getCompleted() != null) {
            checklist.setCompleted(req.getCompleted());
        }
        if (req.getCompletedAt() != null) {
            checklist.setCompletedAt(req.getCompletedAt());
        }
        checklist.setUpdatedBy(currentUser);
        return toResponse(exitChecklistRepository.save(checklist));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ExitChecklist checklist = getEntity(tenantId, id);
        checklist.setDeleted(true);
        checklist.setUpdatedBy(currentUser);
        exitChecklistRepository.save(checklist);
    }

    @Transactional(readOnly = true)
    public List<ExitChecklistDto.Response> listBySeparation(String tenantId, UUID separationId) {
        return exitChecklistRepository
                .findByTenantIdAndSeparationIdAndDeletedFalse(tenantId, separationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExitChecklistDto.Response> listByAssignee(String tenantId, UUID assignedTo) {
        return exitChecklistRepository
                .findByTenantIdAndAssignedToAndDeletedFalse(tenantId, assignedTo)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExitChecklistDto.Response markComplete(String tenantId, UUID id, String currentUser) {
        ExitChecklist checklist = getEntity(tenantId, id);
        checklist.setCompleted(true);
        checklist.setCompletedAt(Instant.now());
        checklist.setUpdatedBy(currentUser);
        return toResponse(exitChecklistRepository.save(checklist));
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private ExitChecklist getEntity(String tenantId, UUID id) {
        return exitChecklistRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ExitChecklist", "id", id));
    }

    private ExitChecklistDto.Response toResponse(ExitChecklist c) {
        return ExitChecklistDto.Response.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .separationId(c.getSeparationId())
                .taskTitle(c.getTaskTitle())
                .taskCategory(c.getTaskCategory())
                .assignedTo(c.getAssignedTo())
                .dueDate(c.getDueDate())
                .completed(c.isCompleted())
                .completedAt(c.getCompletedAt())
                .notes(c.getNotes())
                .createdBy(c.getCreatedBy())
                .updatedBy(c.getUpdatedBy())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
