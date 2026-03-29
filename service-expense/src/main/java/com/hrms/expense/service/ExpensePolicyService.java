package com.hrms.expense.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.expense.dto.ExpensePolicyDto;
import com.hrms.expense.entity.ExpensePolicy;
import com.hrms.expense.repository.ExpensePolicyRepository;
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
public class ExpensePolicyService {

    private final ExpensePolicyRepository repository;

    @Transactional
    public ExpensePolicyDto.Response create(String tenantId, ExpensePolicyDto.CreateRequest request, String currentUser) {
        ExpensePolicy entity = new ExpensePolicy();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setCategoryId(request.getCategoryId());
        entity.setEmployeeLevel(request.getEmployeeLevel());
        entity.setMaxAmount(request.getMaxAmount());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setApprovalRequired(request.isApprovalRequired());
        entity.setActive(request.isActive());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExpensePolicyDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ExpensePolicyDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ExpensePolicyDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExpensePolicyDto.Response update(String tenantId, UUID id, ExpensePolicyDto.UpdateRequest request, String currentUser) {
        ExpensePolicy entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getCategoryId() != null) entity.setCategoryId(request.getCategoryId());
        if (request.getEmployeeLevel() != null) entity.setEmployeeLevel(request.getEmployeeLevel());
        if (request.getMaxAmount() != null) entity.setMaxAmount(request.getMaxAmount());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getApprovalRequired() != null) entity.setApprovalRequired(request.getApprovalRequired());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ExpensePolicy entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    private ExpensePolicy findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpensePolicy", "id", id));
    }

    private ExpensePolicyDto.Response toResponse(ExpensePolicy e) {
        return ExpensePolicyDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .description(e.getDescription())
                .categoryId(e.getCategoryId())
                .employeeLevel(e.getEmployeeLevel())
                .maxAmount(e.getMaxAmount())
                .currency(e.getCurrency())
                .approvalRequired(e.isApprovalRequired())
                .active(e.isActive())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
