package com.hrms.expense.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.expense.dto.ExpenseItemDto;
import com.hrms.expense.entity.ExpenseItem;
import com.hrms.expense.repository.ExpenseItemRepository;
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
public class ExpenseItemService {

    private final ExpenseItemRepository repository;

    @Transactional
    public ExpenseItemDto.Response create(String tenantId, ExpenseItemDto.CreateRequest request, String currentUser) {
        ExpenseItem entity = new ExpenseItem();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setReportId(request.getReportId());
        entity.setCategoryId(request.getCategoryId());
        entity.setDescription(request.getDescription());
        entity.setAmount(request.getAmount());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setExpenseDate(request.getExpenseDate());
        entity.setReceiptUrl(request.getReceiptUrl());
        entity.setMerchant(request.getMerchant());
        entity.setNotes(request.getNotes());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExpenseItemDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseItemDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ExpenseItemDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseItemDto.Response> listByReport(String tenantId, UUID reportId) {
        return repository.findByTenantIdAndReportIdAndDeletedFalse(tenantId, reportId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ExpenseItemDto.Response update(String tenantId, UUID id, ExpenseItemDto.UpdateRequest request, String currentUser) {
        ExpenseItem entity = findOrThrow(tenantId, id);
        if (request.getReportId() != null) entity.setReportId(request.getReportId());
        if (request.getCategoryId() != null) entity.setCategoryId(request.getCategoryId());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getAmount() != null) entity.setAmount(request.getAmount());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getExpenseDate() != null) entity.setExpenseDate(request.getExpenseDate());
        if (request.getReceiptUrl() != null) entity.setReceiptUrl(request.getReceiptUrl());
        if (request.getMerchant() != null) entity.setMerchant(request.getMerchant());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ExpenseItem entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    private ExpenseItem findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseItem", "id", id));
    }

    private ExpenseItemDto.Response toResponse(ExpenseItem e) {
        return ExpenseItemDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .reportId(e.getReportId())
                .categoryId(e.getCategoryId())
                .description(e.getDescription())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .expenseDate(e.getExpenseDate())
                .receiptUrl(e.getReceiptUrl())
                .merchant(e.getMerchant())
                .notes(e.getNotes())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
