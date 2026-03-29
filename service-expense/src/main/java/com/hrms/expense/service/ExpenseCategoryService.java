package com.hrms.expense.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.expense.dto.ExpenseCategoryDto;
import com.hrms.expense.entity.ExpenseCategory;
import com.hrms.expense.repository.ExpenseCategoryRepository;
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
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;

    @Transactional
    public ExpenseCategoryDto.Response create(String tenantId, ExpenseCategoryDto.CreateRequest request, String currentUser) {
        ExpenseCategory entity = new ExpenseCategory();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setMaxAmount(request.getMaxAmount());
        entity.setRequiresReceipt(request.isRequiresReceipt());
        entity.setActive(request.isActive());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExpenseCategoryDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseCategoryDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategoryDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExpenseCategoryDto.Response update(String tenantId, UUID id, ExpenseCategoryDto.UpdateRequest request, String currentUser) {
        ExpenseCategory entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCode() != null) entity.setCode(request.getCode());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getMaxAmount() != null) entity.setMaxAmount(request.getMaxAmount());
        if (request.getRequiresReceipt() != null) entity.setRequiresReceipt(request.getRequiresReceipt());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ExpenseCategory entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    private ExpenseCategory findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseCategory", "id", id));
    }

    private ExpenseCategoryDto.Response toResponse(ExpenseCategory e) {
        return ExpenseCategoryDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .maxAmount(e.getMaxAmount())
                .requiresReceipt(e.isRequiresReceipt())
                .active(e.isActive())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
