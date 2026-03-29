package com.hrms.workflow.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.workflow.dto.WorkflowInstanceDto;
import com.hrms.workflow.entity.WorkflowInstance;
import com.hrms.workflow.entity.WorkflowInstance.InstanceStatus;
import com.hrms.workflow.entity.WorkflowStep;
import com.hrms.workflow.repository.WorkflowInstanceRepository;
import com.hrms.workflow.repository.WorkflowStepRepository;
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
public class WorkflowInstanceService {

    private final WorkflowInstanceRepository repository;
    private final WorkflowStepRepository workflowStepRepository;

    @Transactional
    public WorkflowInstanceDto.Response create(String tenantId, WorkflowInstanceDto.CreateRequest request, String currentUser) {
        WorkflowInstance entity = new WorkflowInstance();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setWorkflowId(request.getWorkflowId());
        entity.setEntityType(request.getEntityType());
        entity.setEntityId(request.getEntityId());
        entity.setInitiatedBy(request.getInitiatedBy());
        entity.setCurrentStepOrder(0);
        entity.setStatus(InstanceStatus.PENDING);
        entity.setContextData(request.getContextData());
        entity.setNotes(request.getNotes());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public WorkflowInstanceDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<WorkflowInstanceDto.Response> listAll(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowInstanceDto.Response> listByEntity(String tenantId, UUID entityId) {
        return repository.findByTenantIdAndEntityIdAndDeletedFalse(tenantId, entityId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<WorkflowInstanceDto.Response> listByInitiator(String tenantId, UUID initiatedBy, Pageable pageable) {
        return repository.findByTenantIdAndInitiatedByAndDeletedFalse(tenantId, initiatedBy, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<WorkflowInstanceDto.Response> listByStatus(String tenantId, InstanceStatus status, Pageable pageable) {
        return repository.findByTenantIdAndStatusAndDeletedFalse(tenantId, status, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public WorkflowInstanceDto.Response update(String tenantId, UUID id, WorkflowInstanceDto.UpdateRequest request, String currentUser) {
        WorkflowInstance entity = findOrThrow(tenantId, id);
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getCurrentStepOrder() != null) entity.setCurrentStepOrder(request.getCurrentStepOrder());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (request.getCompletedAt() != null) entity.setCompletedAt(request.getCompletedAt());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        WorkflowInstance entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public WorkflowInstanceDto.Response approve(String tenantId, UUID id, String currentUser) {
        WorkflowInstance entity = findOrThrow(tenantId, id);
        List<WorkflowStep> steps = workflowStepRepository
                .findByTenantIdAndWorkflowIdAndDeletedFalseOrderByStepOrderAsc(tenantId, entity.getWorkflowId());
        int currentOrder = entity.getCurrentStepOrder();
        boolean hasNextStep = steps.stream().anyMatch(s -> s.getStepOrder() > currentOrder);
        if (hasNextStep) {
            int nextOrder = steps.stream()
                    .filter(s -> s.getStepOrder() > currentOrder)
                    .mapToInt(WorkflowStep::getStepOrder)
                    .min()
                    .orElse(currentOrder);
            entity.setCurrentStepOrder(nextOrder);
            entity.setStatus(InstanceStatus.IN_PROGRESS);
        } else {
            entity.setStatus(InstanceStatus.APPROVED);
            entity.setCompletedAt(Instant.now());
        }
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public WorkflowInstanceDto.Response reject(String tenantId, UUID id, String notes, String currentUser) {
        WorkflowInstance entity = findOrThrow(tenantId, id);
        entity.setStatus(InstanceStatus.REJECTED);
        entity.setCompletedAt(Instant.now());
        entity.setNotes(notes);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public WorkflowInstanceDto.Response cancel(String tenantId, UUID id, String currentUser) {
        WorkflowInstance entity = findOrThrow(tenantId, id);
        entity.setStatus(InstanceStatus.CANCELLED);
        entity.setCompletedAt(Instant.now());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private WorkflowInstance findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowInstance", "id", id));
    }

    private WorkflowInstanceDto.Response toResponse(WorkflowInstance entity) {
        return WorkflowInstanceDto.Response.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workflowId(entity.getWorkflowId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .initiatedBy(entity.getInitiatedBy())
                .currentStepOrder(entity.getCurrentStepOrder())
                .status(entity.getStatus())
                .contextData(entity.getContextData())
                .completedAt(entity.getCompletedAt())
                .notes(entity.getNotes())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
