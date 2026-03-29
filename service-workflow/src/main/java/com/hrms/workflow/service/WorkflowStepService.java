package com.hrms.workflow.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.workflow.dto.WorkflowStepDto;
import com.hrms.workflow.entity.WorkflowStep;
import com.hrms.workflow.repository.WorkflowStepRepository;
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
public class WorkflowStepService {

    private final WorkflowStepRepository repository;

    @Transactional
    public WorkflowStepDto.Response create(String tenantId, WorkflowStepDto.CreateRequest request, String currentUser) {
        WorkflowStep entity = new WorkflowStep();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setWorkflowId(request.getWorkflowId());
        entity.setStepOrder(request.getStepOrder());
        entity.setStepName(request.getStepName());
        entity.setStepType(request.getStepType());
        entity.setApproverType(request.getApproverType());
        entity.setApproverId(request.getApproverId());
        entity.setApproverRole(request.getApproverRole());
        entity.setCanDelegate(request.isCanDelegate());
        entity.setSlaHours(request.getSlaHours());
        entity.setEscalationTo(request.getEscalationTo());
        entity.setRequired(request.isRequired());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public WorkflowStepDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<WorkflowStepDto.Response> listAll(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkflowStepDto.Response> listByWorkflow(String tenantId, UUID workflowId) {
        return repository.findByTenantIdAndWorkflowIdAndDeletedFalseOrderByStepOrderAsc(tenantId, workflowId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public WorkflowStepDto.Response update(String tenantId, UUID id, WorkflowStepDto.UpdateRequest request, String currentUser) {
        WorkflowStep entity = findOrThrow(tenantId, id);
        if (request.getWorkflowId() != null) entity.setWorkflowId(request.getWorkflowId());
        if (request.getStepOrder() != null) entity.setStepOrder(request.getStepOrder());
        if (request.getStepName() != null) entity.setStepName(request.getStepName());
        if (request.getStepType() != null) entity.setStepType(request.getStepType());
        if (request.getApproverType() != null) entity.setApproverType(request.getApproverType());
        if (request.getApproverId() != null) entity.setApproverId(request.getApproverId());
        if (request.getApproverRole() != null) entity.setApproverRole(request.getApproverRole());
        if (request.getCanDelegate() != null) entity.setCanDelegate(request.getCanDelegate());
        if (request.getSlaHours() != null) entity.setSlaHours(request.getSlaHours());
        if (request.getEscalationTo() != null) entity.setEscalationTo(request.getEscalationTo());
        if (request.getRequired() != null) entity.setRequired(request.getRequired());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        WorkflowStep entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    private WorkflowStep findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowStep", "id", id));
    }

    private WorkflowStepDto.Response toResponse(WorkflowStep entity) {
        return WorkflowStepDto.Response.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .workflowId(entity.getWorkflowId())
                .stepOrder(entity.getStepOrder())
                .stepName(entity.getStepName())
                .stepType(entity.getStepType())
                .approverType(entity.getApproverType())
                .approverId(entity.getApproverId())
                .approverRole(entity.getApproverRole())
                .canDelegate(entity.isCanDelegate())
                .slaHours(entity.getSlaHours())
                .escalationTo(entity.getEscalationTo())
                .required(entity.isRequired())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
