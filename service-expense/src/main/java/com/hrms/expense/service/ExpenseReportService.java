package com.hrms.expense.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.expense.dto.ExpenseReportDto;
import com.hrms.expense.entity.ExpenseReport;
import com.hrms.expense.entity.ExpenseReport.ReportStatus;
import com.hrms.expense.repository.ExpenseReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseReportService {

    private final ExpenseReportRepository repository;

    @Transactional
    public ExpenseReportDto.Response create(String tenantId, ExpenseReportDto.CreateRequest request, String currentUser) {
        ExpenseReport entity = new ExpenseReport();
        entity.setTenantId(tenantId);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setPeriodStart(request.getPeriodStart());
        entity.setPeriodEnd(request.getPeriodEnd());
        entity.setTotalAmount(BigDecimal.ZERO);
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setStatus(ReportStatus.DRAFT);
        return toResponse(repository.save(entity));
    }

    @Transactional(readOnly = true)
    public ExpenseReportDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseReportDto.Response> list(String tenantId, Pageable pageable) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ExpenseReportDto.Response> listAll(String tenantId) {
        return repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ExpenseReportDto.Response> listByEmployee(String tenantId, UUID employeeId, Pageable pageable) {
        return repository.findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ExpenseReportDto.Response update(String tenantId, UUID id, ExpenseReportDto.UpdateRequest request, String currentUser) {
        ExpenseReport entity = findOrThrow(tenantId, id);
        if (request.getEmployeeId() != null) entity.setEmployeeId(request.getEmployeeId());
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getPeriodStart() != null) entity.setPeriodStart(request.getPeriodStart());
        if (request.getPeriodEnd() != null) entity.setPeriodEnd(request.getPeriodEnd());
        if (request.getTotalAmount() != null) entity.setTotalAmount(request.getTotalAmount());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ExpenseReport entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        repository.save(entity);
    }

    @Transactional
    public ExpenseReportDto.Response submit(String tenantId, UUID id, String currentUser) {
        ExpenseReport entity = findOrThrow(tenantId, id);
        entity.setStatus(ReportStatus.SUBMITTED);
        entity.setSubmittedAt(Instant.now());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ExpenseReportDto.Response approve(String tenantId, UUID id, UUID approvedBy, String currentUser) {
        ExpenseReport entity = findOrThrow(tenantId, id);
        entity.setStatus(ReportStatus.APPROVED);
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(Instant.now());
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    @Transactional
    public ExpenseReportDto.Response reject(String tenantId, UUID id, String reason, String currentUser) {
        ExpenseReport entity = findOrThrow(tenantId, id);
        entity.setStatus(ReportStatus.REJECTED);
        entity.setRejectedReason(reason);
        entity.setUpdatedBy(currentUser);
        return toResponse(repository.save(entity));
    }

    private ExpenseReport findOrThrow(String tenantId, UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseReport", "id", id));
    }

    private ExpenseReportDto.Response toResponse(ExpenseReport e) {
        return ExpenseReportDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .employeeId(e.getEmployeeId())
                .title(e.getTitle())
                .description(e.getDescription())
                .periodStart(e.getPeriodStart())
                .periodEnd(e.getPeriodEnd())
                .totalAmount(e.getTotalAmount())
                .currency(e.getCurrency())
                .status(e.getStatus())
                .submittedAt(e.getSubmittedAt())
                .approvedBy(e.getApprovedBy())
                .approvedAt(e.getApprovedAt())
                .rejectedReason(e.getRejectedReason())
                .paidAt(e.getPaidAt())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
