package com.hrms.expense.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.expense.dto.AdvanceDto;
import com.hrms.expense.entity.Advance;
import com.hrms.expense.entity.Advance.AdvanceStatus;
import com.hrms.expense.repository.AdvanceRepository;
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
public class AdvanceService {

    private final AdvanceRepository repository;

    @Transactional
    public AdvanceDto.Response create(String tenantId, AdvanceDto.CreateRequest request, String currentUser) {
        Advance entity = new Advance();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setAmount(request.getAmount());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setPurpose(request.getPurpose());
        entity.setStatus(AdvanceStatus.REQUESTED);
        entity.setRequestedAt(Instant.now());
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public AdvanceDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<AdvanceDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AdvanceDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdvanceDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return repository.findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AdvanceDto.Response update(String tenantId, UUID id, AdvanceDto.UpdateRequest request, String currentUser) {
        Advance entity = findOrThrow(tenantId, id);
        if (request.getEmployeeId() != null) entity.setEmployeeId(request.getEmployeeId());
        if (request.getAmount() != null) entity.setAmount(request.getAmount());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getPurpose() != null) entity.setPurpose(request.getPurpose());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getApprovedBy() != null) entity.setApprovedBy(request.getApprovedBy());
        if (request.getApprovedAt() != null) entity.setApprovedAt(request.getApprovedAt());
        if (request.getDisbursedAt() != null) entity.setDisbursedAt(request.getDisbursedAt());
        if (request.getDueDate() != null) entity.setDueDate(request.getDueDate());
        if (request.getSettledAmount() != null) entity.setSettledAmount(request.getSettledAmount());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Advance entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public AdvanceDto.Response approve(String tenantId, UUID id, UUID approvedBy, String currentUser) {
        Advance entity = findOrThrow(tenantId, id);
        entity.setStatus(AdvanceStatus.APPROVED);
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(Instant.now());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private Advance findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Advance", "id", id));
    }

    private AdvanceDto.Response toResponse(Advance e) {
        return AdvanceDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .employeeId(e.getEmployeeId())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .purpose(e.getPurpose())
                .status(e.getStatus())
                .requestedAt(e.getRequestedAt())
                .approvedBy(e.getApprovedBy())
                .approvedAt(e.getApprovedAt())
                .disbursedAt(e.getDisbursedAt())
                .dueDate(e.getDueDate())
                .settledAmount(e.getSettledAmount())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
