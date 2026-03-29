package com.hrms.onboarding.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.onboarding.dto.OnboardingTemplateDto;
import com.hrms.onboarding.entity.OnboardingTemplate;
import com.hrms.onboarding.repository.OnboardingTemplateRepository;
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
public class OnboardingTemplateService {

    private final OnboardingTemplateRepository onboardingTemplateRepository;

    @Transactional
    public OnboardingTemplateDto.Response create(String tenantId, OnboardingTemplateDto.CreateRequest req, String currentUser) {
        OnboardingTemplate template = new OnboardingTemplate();
        template.setTenantId(tenantId);
        template.setName(req.getName());
        template.setDescription(req.getDescription());
        template.setRoleId(req.getRoleId());
        template.setDepartmentId(req.getDepartmentId());
        template.setActive(req.isActive());
        template.setCreatedBy(currentUser);
        template.setUpdatedBy(currentUser);
        return toResponse(onboardingTemplateRepository.save(template));
    }

    @Transactional
    public OnboardingTemplateDto.Response update(String tenantId, UUID id, OnboardingTemplateDto.UpdateRequest req, String currentUser) {
        OnboardingTemplate template = getEntity(tenantId, id);
        if (req.getName() != null) {
            template.setName(req.getName());
        }
        if (req.getDescription() != null) {
            template.setDescription(req.getDescription());
        }
        if (req.getRoleId() != null) {
            template.setRoleId(req.getRoleId());
        }
        if (req.getDepartmentId() != null) {
            template.setDepartmentId(req.getDepartmentId());
        }
        if (req.getActive() != null) {
            template.setActive(req.getActive());
        }
        template.setUpdatedBy(currentUser);
        return toResponse(onboardingTemplateRepository.save(template));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        OnboardingTemplate template = getEntity(tenantId, id);
        template.setDeleted(true);
        template.setUpdatedBy(currentUser);
        onboardingTemplateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public OnboardingTemplateDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<OnboardingTemplateDto.Response> list(String tenantId, Pageable pageable) {
        return onboardingTemplateRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private OnboardingTemplate getEntity(String tenantId, UUID id) {
        return onboardingTemplateRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OnboardingTemplate", "id", id));
    }

    private OnboardingTemplateDto.Response toResponse(OnboardingTemplate template) {
        return OnboardingTemplateDto.Response.builder()
                .id(template.getId())
                .tenantId(template.getTenantId())
                .name(template.getName())
                .description(template.getDescription())
                .roleId(template.getRoleId())
                .departmentId(template.getDepartmentId())
                .active(template.isActive())
                .createdBy(template.getCreatedBy())
                .updatedBy(template.getUpdatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
