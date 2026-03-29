package com.hrms.compensation.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compensation.dto.CompensationPlanDto;
import com.hrms.compensation.entity.CompensationPlan;
import com.hrms.compensation.entity.CompensationPlan.PlanStatus;
import com.hrms.compensation.repository.CompensationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompensationPlanService {

    private final CompensationPlanRepository compensationPlanRepository;

    @Transactional
    public CompensationPlanDto.Response create(String tenantId, CompensationPlanDto.CreateRequest request, String currentUser) {
        CompensationPlan entity = new CompensationPlan();
        entity.setTenantId(tenantId);
        entity.setEmployeeId(request.getEmployeeId());
        entity.setPayGradeId(request.getPayGradeId());
        entity.setBaseSalary(request.getBaseSalary());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setEffectiveDate(request.getEffectiveDate());
        entity.setEndDate(request.getEndDate());
        entity.setAllowances(request.getAllowances() != null ? request.getAllowances() : BigDecimal.ZERO);
        entity.setBonusPercentage(request.getBonusPercentage() != null ? request.getBonusPercentage() : BigDecimal.ZERO);
        entity.setNotes(request.getNotes());
        entity.setStatus(PlanStatus.DRAFT);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(compensationPlanRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public CompensationPlanDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<CompensationPlanDto.Response> list(String tenantId, Pageable pageable) {
        return compensationPlanRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<CompensationPlanDto.Response> listAll(String tenantId) {
        return compensationPlanRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompensationPlanDto.Response> listByEmployee(String tenantId, UUID employeeId) {
        return compensationPlanRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalseOrderByEffectiveDateDesc(tenantId, employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CompensationPlanDto.Response update(String tenantId, UUID id, CompensationPlanDto.UpdateRequest request, String currentUser) {
        CompensationPlan entity = findOrThrow(tenantId, id);
        if (request.getPayGradeId() != null) entity.setPayGradeId(request.getPayGradeId());
        if (request.getBaseSalary() != null) entity.setBaseSalary(request.getBaseSalary());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getEffectiveDate() != null) entity.setEffectiveDate(request.getEffectiveDate());
        if (request.getEndDate() != null) entity.setEndDate(request.getEndDate());
        if (request.getAllowances() != null) entity.setAllowances(request.getAllowances());
        if (request.getBonusPercentage() != null) entity.setBonusPercentage(request.getBonusPercentage());
        if (request.getNotes() != null) entity.setNotes(request.getNotes());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        entity.setUpdatedBy(currentUser);
        return toResponse(compensationPlanRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        CompensationPlan entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        compensationPlanRepository.save(entity);
    }

    @Transactional
    public CompensationPlanDto.Response activate(String tenantId, UUID id, String currentUser) {
        CompensationPlan entity = findOrThrow(tenantId, id);

        // Supersede any existing ACTIVE plan for the same employee
        compensationPlanRepository
                .findByTenantIdAndEmployeeIdAndStatusAndDeletedFalse(tenantId, entity.getEmployeeId(), PlanStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(PlanStatus.SUPERSEDED);
                    existing.setUpdatedBy(currentUser);
                    compensationPlanRepository.save(existing);
                });

        entity.setStatus(PlanStatus.ACTIVE);
        entity.setUpdatedBy(currentUser);
        return toResponse(compensationPlanRepository.save(entity));
    }

    private CompensationPlan findOrThrow(String tenantId, UUID id) {
        return compensationPlanRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CompensationPlan", "id", id));
    }

    private CompensationPlanDto.Response toResponse(CompensationPlan e) {
        return CompensationPlanDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .employeeId(e.getEmployeeId())
                .payGradeId(e.getPayGradeId())
                .baseSalary(e.getBaseSalary())
                .currency(e.getCurrency())
                .effectiveDate(e.getEffectiveDate())
                .endDate(e.getEndDate())
                .allowances(e.getAllowances())
                .bonusPercentage(e.getBonusPercentage())
                .notes(e.getNotes())
                .status(e.getStatus())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
