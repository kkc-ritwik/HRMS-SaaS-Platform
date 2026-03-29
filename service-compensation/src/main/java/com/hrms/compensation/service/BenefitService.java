package com.hrms.compensation.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.compensation.dto.BenefitDto;
import com.hrms.compensation.entity.Benefit;
import com.hrms.compensation.entity.Benefit.BenefitType;
import com.hrms.compensation.repository.BenefitRepository;
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
public class BenefitService {

    private final BenefitRepository benefitRepository;

    @Transactional
    public BenefitDto.Response create(String tenantId, BenefitDto.CreateRequest request, String currentUser) {
        Benefit entity = new Benefit();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setBenefitType(request.getBenefitType() != null ? request.getBenefitType() : BenefitType.OTHER);
        entity.setProvider(request.getProvider());
        entity.setCoverageAmount(request.getCoverageAmount());
        entity.setEmployeeContribution(request.getEmployeeContribution());
        entity.setEmployerContribution(request.getEmployerContribution());
        entity.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        entity.setActive(request.isActive());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(benefitRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public BenefitDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<BenefitDto.Response> list(String tenantId, Pageable pageable) {
        return benefitRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<BenefitDto.Response> listAll(String tenantId) {
        return benefitRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BenefitDto.Response> listActive(String tenantId) {
        return benefitRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BenefitDto.Response> listByType(String tenantId, BenefitType type) {
        return benefitRepository.findByTenantIdAndBenefitTypeAndDeletedFalse(tenantId, type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public BenefitDto.Response update(String tenantId, UUID id, BenefitDto.UpdateRequest request, String currentUser) {
        Benefit entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCode() != null) entity.setCode(request.getCode());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getBenefitType() != null) entity.setBenefitType(request.getBenefitType());
        if (request.getProvider() != null) entity.setProvider(request.getProvider());
        if (request.getCoverageAmount() != null) entity.setCoverageAmount(request.getCoverageAmount());
        if (request.getEmployeeContribution() != null) entity.setEmployeeContribution(request.getEmployeeContribution());
        if (request.getEmployerContribution() != null) entity.setEmployerContribution(request.getEmployerContribution());
        if (request.getCurrency() != null) entity.setCurrency(request.getCurrency());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(benefitRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Benefit entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        benefitRepository.save(entity);
    }

    private Benefit findOrThrow(String tenantId, UUID id) {
        return benefitRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Benefit", "id", id));
    }

    private BenefitDto.Response toResponse(Benefit e) {
        return BenefitDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .benefitType(e.getBenefitType())
                .provider(e.getProvider())
                .coverageAmount(e.getCoverageAmount())
                .employeeContribution(e.getEmployeeContribution())
                .employerContribution(e.getEmployerContribution())
                .currency(e.getCurrency())
                .active(e.isActive())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
