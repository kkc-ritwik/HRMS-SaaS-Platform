package com.hrms.workflow.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.workflow.dto.DelegationRuleDto;
import com.hrms.workflow.entity.DelegationRule;
import com.hrms.workflow.entity.DelegationRule.DelegationScope;
import com.hrms.workflow.repository.DelegationRuleRepository;
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
public class DelegationRuleService {

    private final DelegationRuleRepository repository;

    @Transactional
    public DelegationRuleDto.Response create(String tenantId, DelegationRuleDto.CreateRequest request, String currentUser) {
        DelegationRule entity = new DelegationRule();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setDelegatorId(request.getDelegatorId());
        entity.setDelegateId(request.getDelegateId());
        entity.setScope(request.getScope() != null ? request.getScope() : DelegationScope.ALL);
        entity.setEntityTypes(request.getEntityTypes());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setReason(request.getReason());
        entity.setActive(request.isActive());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public DelegationRuleDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<DelegationRuleDto.Response> listAll(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DelegationRuleDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DelegationRuleDto.Response> listByDelegator(String tenantId, UUID delegatorId) {
        return repository.findByTenantIdAndDelegatorIdAndActiveAndDeletedFalse(tenantId, delegatorId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DelegationRuleDto.Response> listByDelegate(String tenantId, UUID delegateId) {
        return repository.findByTenantIdAndDelegateIdAndActiveAndDeletedFalse(tenantId, delegateId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public DelegationRuleDto.Response update(String tenantId, UUID id, DelegationRuleDto.UpdateRequest request, String currentUser) {
        DelegationRule entity = findOrThrow(tenantId, id);
        if (request.getDelegatorId() != null) entity.setDelegatorId(request.getDelegatorId());
        if (request.getDelegateId() != null) entity.setDelegateId(request.getDelegateId());
        if (request.getScope() != null) entity.setScope(request.getScope());
        if (request.getEntityTypes() != null) entity.setEntityTypes(request.getEntityTypes());
        if (request.getStartDate() != null) entity.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) entity.setEndDate(request.getEndDate());
        if (request.getReason() != null) entity.setReason(request.getReason());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        DelegationRule entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public DelegationRuleDto.Response deactivate(String tenantId, UUID id, String currentUser) {
        DelegationRule entity = findOrThrow(tenantId, id);
        entity.setActive(false);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private DelegationRule findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DelegationRule", "id", id));
    }

    private DelegationRuleDto.Response toResponse(DelegationRule entity) {
        return DelegationRuleDto.Response.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .delegatorId(entity.getDelegatorId())
                .delegateId(entity.getDelegateId())
                .scope(entity.getScope())
                .entityTypes(entity.getEntityTypes())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .active(entity.isActive())
                .reason(entity.getReason())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
