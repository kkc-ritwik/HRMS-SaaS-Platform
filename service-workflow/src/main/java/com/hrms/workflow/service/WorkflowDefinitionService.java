package com.hrms.workflow.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.workflow.dto.WorkflowDefinitionDto;
import com.hrms.workflow.entity.WorkflowDefinition;
import com.hrms.workflow.repository.WorkflowDefinitionRepository;
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
public class WorkflowDefinitionService {

    private final WorkflowDefinitionRepository repository;

    @Transactional
    public WorkflowDefinitionDto.Response create(String tenantId, WorkflowDefinitionDto.CreateRequest request, String currentUser) {
        WorkflowDefinition entity = new WorkflowDefinition();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setEntityType(request.getEntityType());
        entity.setTriggerEvent(request.getTriggerEvent());
        entity.setStepsConfig(request.getStepsConfig());
        entity.setActive(request.isActive());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public WorkflowDefinitionDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<WorkflowDefinitionDto.Response> listAll(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinitionDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowDefinitionDto.Response> listByEntityType(String tenantId, String entityType) {
        return repository.findByTenantIdAndEntityTypeAndActiveAndDeletedFalse(tenantId, entityType, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public WorkflowDefinitionDto.Response update(String tenantId, UUID id, WorkflowDefinitionDto.UpdateRequest request, String currentUser) {
        WorkflowDefinition entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCode() != null) entity.setCode(request.getCode());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getEntityType() != null) entity.setEntityType(request.getEntityType());
        if (request.getTriggerEvent() != null) entity.setTriggerEvent(request.getTriggerEvent());
        if (request.getStepsConfig() != null) entity.setStepsConfig(request.getStepsConfig());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        WorkflowDefinition entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public WorkflowDefinitionDto.Response activate(String tenantId, UUID id, String currentUser) {
        WorkflowDefinition entity = findOrThrow(tenantId, id);
        entity.setActive(true);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public WorkflowDefinitionDto.Response deactivate(String tenantId, UUID id, String currentUser) {
        WorkflowDefinition entity = findOrThrow(tenantId, id);
        entity.setActive(false);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private WorkflowDefinition findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowDefinition", "id", id));
    }

    private WorkflowDefinitionDto.Response toResponse(WorkflowDefinition entity) {
        return WorkflowDefinitionDto.Response.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .code(entity.getCode())
                .description(entity.getDescription())
                .entityType(entity.getEntityType())
                .triggerEvent(entity.getTriggerEvent())
                .stepsConfig(entity.getStepsConfig())
                .active(entity.isActive())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
