package com.hrms.payroll.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.EmployeeSalaryDto;
import com.hrms.payroll.entity.*;
import com.hrms.payroll.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeSalaryService {

    private final EmployeeSalaryRepository          salaryRepository;
    private final EmployeeSalaryComponentRepository salaryComponentRepository;
    private final SalaryComponentRepository         componentRepository;
    private final SalaryStructureRepository         structureRepository;

    // ── Assign / revise salary ────────────────────────────────────────────────

    @Transactional
    public EmployeeSalaryDto.Response assign(String tenantId, UUID employeeId,
                                              EmployeeSalaryDto.AssignRequest req,
                                              String currentUser) {
        // Close any existing ACTIVE salary one day before the new effective date
        salaryRepository.closeActiveSalary(
                employeeId, tenantId,
                req.getEffectiveFrom().minusDays(1),
                currentUser);

        // Resolve components (either from the request or fall back to the structure defaults)
        List<EmployeeSalaryDto.ComponentItem> items = resolveComponents(tenantId, req);
        if (items.isEmpty()) {
            throw new BusinessException("NO_COMPONENTS",
                    "At least one salary component must be provided");
        }

        // Build a map for quick lookup of component metadata
        List<UUID> compIds = items.stream().map(EmployeeSalaryDto.ComponentItem::getComponentId)
                .collect(Collectors.toList());
        Map<UUID, SalaryComponent> compMap = componentRepository
                .findAllByIdInAndTenantId(compIds, tenantId)
                .stream().collect(Collectors.toMap(SalaryComponent::getId, c -> c));

        // Calculate gross (sum of EARNINGs) and deductions
        BigDecimal grossMonthly = items.stream()
                .filter(i -> {
                    SalaryComponent c = compMap.get(i.getComponentId());
                    return c != null && c.getType() == SalaryComponent.ComponentType.EARNING
                            && c.isPartOfGross();
                })
                .map(EmployeeSalaryDto.ComponentItem::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductions = items.stream()
                .filter(i -> {
                    SalaryComponent c = compMap.get(i.getComponentId());
                    return c != null && c.getType() == SalaryComponent.ComponentType.DEDUCTION;
                })
                .map(EmployeeSalaryDto.ComponentItem::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netMonthly = grossMonthly.subtract(totalDeductions);

        // Create EmployeeSalary record
        EmployeeSalary salary = new EmployeeSalary();
        salary.setTenantId(tenantId);
        salary.setEmployeeId(employeeId);
        salary.setSalaryStructureId(req.getSalaryStructureId());
        salary.setCtcAnnual(req.getCtcAnnual());
        salary.setGrossMonthly(grossMonthly);
        salary.setNetMonthly(netMonthly);
        salary.setEffectiveFrom(req.getEffectiveFrom());
        salary.setRevisionLetterUrl(req.getRevisionLetterUrl());
        salary.setApprovedBy(req.getApprovedBy());
        salary.setStatus(EmployeeSalary.SalaryStatus.ACTIVE);
        salary.setCreatedBy(currentUser);
        salaryRepository.save(salary);

        // Create component breakdown rows
        List<EmployeeSalaryComponent> savedComps = new ArrayList<>();
        for (EmployeeSalaryDto.ComponentItem item : items) {
            EmployeeSalaryComponent esc = new EmployeeSalaryComponent();
            esc.setEmployeeSalaryId(salary.getId());
            esc.setComponentId(item.getComponentId());
            esc.setMonthlyAmount(item.getMonthlyAmount());
            esc.setAnnualAmount(item.getMonthlyAmount().multiply(BigDecimal.valueOf(12)));
            esc.setEmployerContribution(item.getEmployerContribution() != null
                    ? item.getEmployerContribution() : BigDecimal.ZERO);
            savedComps.add(salaryComponentRepository.save(esc));
        }

        log.info("Salary assigned: employee={} effectiveFrom={} ctcAnnual={}",
                employeeId, req.getEffectiveFrom(), req.getCtcAnnual());
        return toResponse(salary, savedComps, compMap);
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public EmployeeSalaryDto.Response getCurrentSalary(String tenantId, UUID employeeId) {
        EmployeeSalary salary = salaryRepository
                .findFirstByEmployeeIdAndTenantIdAndStatusAndDeletedFalseOrderByEffectiveFromDesc(
                        employeeId, tenantId, EmployeeSalary.SalaryStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalary", "employeeId", employeeId));
        return buildFullResponse(salary, tenantId);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSalaryDto.Response> getSalaryHistory(String tenantId, UUID employeeId) {
        return salaryRepository
                .findByEmployeeIdAndTenantIdAndDeletedFalseOrderByEffectiveFromDesc(employeeId, tenantId)
                .stream().map(s -> buildFullResponse(s, tenantId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<EmployeeSalaryDto.Response> getSalaryPage(String tenantId, UUID employeeId,
                                                           Pageable pageable) {
        return salaryRepository.findByEmployeeIdAndTenantIdAndDeletedFalse(employeeId, tenantId, pageable)
                .map(s -> buildFullResponse(s, tenantId));
    }

    @Transactional(readOnly = true)
    public EmployeeSalaryDto.Response getSalaryById(String tenantId, UUID salaryId) {
        EmployeeSalary salary = salaryRepository.findByIdAndTenantIdAndDeletedFalse(salaryId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EmployeeSalary", "id", salaryId));
        return buildFullResponse(salary, tenantId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * If the request has explicit component items, use them.
     * Otherwise fall back to the salary structure's default components.
     */
    private List<EmployeeSalaryDto.ComponentItem> resolveComponents(
            String tenantId, EmployeeSalaryDto.AssignRequest req) {
        if (req.getComponents() != null && !req.getComponents().isEmpty()) {
            return req.getComponents();
        }
        // No explicit breakdown → cannot auto-split without more domain rules;
        // return empty so the caller throws a meaningful error.
        return List.of();
    }

    private EmployeeSalaryDto.Response buildFullResponse(EmployeeSalary salary, String tenantId) {
        List<EmployeeSalaryComponent> comps = salaryComponentRepository
                .findByEmployeeSalaryId(salary.getId());
        List<UUID> compIds = comps.stream().map(EmployeeSalaryComponent::getComponentId)
                .collect(Collectors.toList());
        Map<UUID, SalaryComponent> compMap = compIds.isEmpty() ? Map.of()
                : componentRepository.findAllByIdInAndTenantId(compIds, tenantId)
                        .stream().collect(Collectors.toMap(SalaryComponent::getId, c -> c));
        return toResponse(salary, comps, compMap);
    }

    private EmployeeSalaryDto.Response toResponse(EmployeeSalary salary,
                                                   List<EmployeeSalaryComponent> comps,
                                                   Map<UUID, SalaryComponent> compMap) {
        // Resolve structure name
        UUID structId = salary.getSalaryStructureId();
        String structureName = structId != null
                ? structureRepository.findById(structId).map(SalaryStructure::getName).orElse(null)
                : null;

        List<EmployeeSalaryDto.ComponentBreakdown> breakdown = comps.stream().map(esc -> {
            SalaryComponent c = compMap.get(esc.getComponentId());
            return EmployeeSalaryDto.ComponentBreakdown.builder()
                    .componentId(esc.getComponentId())
                    .componentName(c != null ? c.getName() : null)
                    .componentCode(c != null ? c.getCode() : null)
                    .componentType(c != null ? c.getType() : null)
                    .monthlyAmount(esc.getMonthlyAmount())
                    .annualAmount(esc.getAnnualAmount())
                    .employerContribution(esc.getEmployerContribution())
                    .taxable(c != null && c.isTaxable())
                    .partOfGross(c != null && c.isPartOfGross())
                    .build();
        }).collect(Collectors.toList());

        return EmployeeSalaryDto.Response.builder()
                .id(salary.getId())
                .employeeId(salary.getEmployeeId())
                .salaryStructureId(salary.getSalaryStructureId())
                .salaryStructureName(structureName)
                .ctcAnnual(salary.getCtcAnnual())
                .grossMonthly(salary.getGrossMonthly())
                .netMonthly(salary.getNetMonthly())
                .effectiveFrom(salary.getEffectiveFrom())
                .effectiveTo(salary.getEffectiveTo())
                .revisionLetterUrl(salary.getRevisionLetterUrl())
                .approvedBy(salary.getApprovedBy())
                .status(salary.getStatus())
                .components(breakdown)
                .createdAt(salary.getCreatedAt())
                .updatedAt(salary.getUpdatedAt())
                .build();
    }
}
