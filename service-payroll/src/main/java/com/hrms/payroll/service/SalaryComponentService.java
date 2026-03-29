package com.hrms.payroll.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.SalaryComponentDto;
import com.hrms.payroll.entity.SalaryComponent;
import com.hrms.payroll.repository.SalaryComponentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryComponentService {

    private final SalaryComponentRepository componentRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public SalaryComponentDto.Response create(String tenantId, SalaryComponentDto.CreateRequest req,
                                               String currentUser) {
        if (componentRepository.existsByCodeAndTenantIdAndDeletedFalse(req.getCode(), tenantId)) {
            throw new DuplicateResourceException("SalaryComponent", "code", req.getCode());
        }
        validatePercentageRef(req.getCalculationType(), req.getPercentageOfComponentId(),
                req.getPercentageValue(), tenantId);

        SalaryComponent comp = new SalaryComponent();
        comp.setTenantId(tenantId);
        comp.setName(req.getName());
        comp.setCode(req.getCode().toUpperCase());
        comp.setType(req.getType());
        comp.setCalculationType(req.getCalculationType());
        comp.setPercentageOfComponentId(req.getPercentageOfComponentId());
        comp.setPercentageValue(req.getPercentageValue());
        comp.setFormulaExpression(req.getFormulaExpression());
        comp.setTaxable(req.isTaxable());
        comp.setPartOfCtc(req.isPartOfCtc());
        comp.setPartOfGross(req.isPartOfGross());
        comp.setProRata(req.isProRata());
        comp.setDisplayOrder(req.getDisplayOrder());
        comp.setCreatedBy(currentUser);
        return toResponse(componentRepository.save(comp), Map.of());
    }

    @Transactional
    public SalaryComponentDto.Response update(String tenantId, UUID id,
                                               SalaryComponentDto.UpdateRequest req,
                                               String currentUser) {
        SalaryComponent comp = getEntity(tenantId, id);
        if (req.getName() != null)                   comp.setName(req.getName());
        if (req.getType() != null)                   comp.setType(req.getType());
        if (req.getCalculationType() != null)        comp.setCalculationType(req.getCalculationType());
        if (req.getPercentageOfComponentId() != null) comp.setPercentageOfComponentId(req.getPercentageOfComponentId());
        if (req.getPercentageValue() != null)        comp.setPercentageValue(req.getPercentageValue());
        if (req.getFormulaExpression() != null)      comp.setFormulaExpression(req.getFormulaExpression());
        if (req.getTaxable() != null)                comp.setTaxable(req.getTaxable());
        if (req.getPartOfCtc() != null)              comp.setPartOfCtc(req.getPartOfCtc());
        if (req.getPartOfGross() != null)            comp.setPartOfGross(req.getPartOfGross());
        if (req.getProRata() != null)                comp.setProRata(req.getProRata());
        if (req.getDisplayOrder() != null)           comp.setDisplayOrder(req.getDisplayOrder());
        if (req.getActive() != null)                 comp.setActive(req.getActive());
        comp.setUpdatedBy(currentUser);
        return toResponse(componentRepository.save(comp), resolveBaseNames(tenantId));
    }

    @Transactional(readOnly = true)
    public SalaryComponentDto.Response getById(String tenantId, UUID id) {
        SalaryComponent comp = getEntity(tenantId, id);
        return toResponse(comp, resolveBaseNames(tenantId));
    }

    @Transactional(readOnly = true)
    public Page<SalaryComponentDto.Response> list(String tenantId, Pageable pageable) {
        Map<UUID, String> nameMap = resolveBaseNames(tenantId);
        return componentRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(c -> toResponse(c, nameMap));
    }

    @Transactional(readOnly = true)
    public List<SalaryComponentDto.Response> listActive(String tenantId) {
        Map<UUID, String> nameMap = resolveBaseNames(tenantId);
        return componentRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(c -> toResponse(c, nameMap)).collect(Collectors.toList());
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        SalaryComponent comp = getEntity(tenantId, id);
        comp.setDeleted(true);
        comp.setUpdatedBy(currentUser);
        componentRepository.save(comp);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    public SalaryComponent getEntity(String tenantId, UUID id) {
        return componentRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent", "id", id));
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    SalaryComponentDto.Response toResponse(SalaryComponent c, Map<UUID, String> baseNameMap) {
        String baseCompName = c.getPercentageOfComponentId() != null
                ? baseNameMap.get(c.getPercentageOfComponentId()) : null;
        return SalaryComponentDto.Response.builder()
                .id(c.getId())
                .name(c.getName())
                .code(c.getCode())
                .type(c.getType())
                .calculationType(c.getCalculationType())
                .percentageOfComponentId(c.getPercentageOfComponentId())
                .percentageOfComponentName(baseCompName)
                .percentageValue(c.getPercentageValue())
                .formulaExpression(c.getFormulaExpression())
                .taxable(c.isTaxable())
                .partOfCtc(c.isPartOfCtc())
                .partOfGross(c.isPartOfGross())
                .proRata(c.isProRata())
                .displayOrder(c.getDisplayOrder())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    /** Build a UUID→name map for all active components (used to resolve percentage base names). */
    private Map<UUID, String> resolveBaseNames(String tenantId) {
        return componentRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().collect(Collectors.toMap(SalaryComponent::getId, SalaryComponent::getName));
    }

    private void validatePercentageRef(SalaryComponent.CalculationType calcType,
                                        UUID refId, java.math.BigDecimal value, String tenantId) {
        if (calcType == SalaryComponent.CalculationType.PERCENTAGE) {
            if (refId != null) {
                componentRepository.findByIdAndTenantIdAndDeletedFalse(refId, tenantId)
                        .orElseThrow(() -> new ResourceNotFoundException("SalaryComponent",
                                "percentageOfComponentId", refId));
            }
        }
    }
}
