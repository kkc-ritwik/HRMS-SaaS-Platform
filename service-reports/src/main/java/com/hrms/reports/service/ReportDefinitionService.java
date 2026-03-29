package com.hrms.reports.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.reports.dto.ReportDefinitionDto;
import com.hrms.reports.entity.ReportDefinition;
import com.hrms.reports.repository.ReportDefinitionRepository;
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
public class ReportDefinitionService {

    private final ReportDefinitionRepository reportDefinitionRepository;

    @Transactional
    public ReportDefinitionDto.Response create(String tenantId, ReportDefinitionDto.CreateRequest request, String currentUser) {
        ReportDefinition entity = new ReportDefinition();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setDescription(request.getDescription());
        entity.setCategory(request.getCategory());
        entity.setQueryConfig(request.getQueryConfig());
        entity.setParametersConfig(request.getParametersConfig());
        entity.setOutputFormats(request.getOutputFormats());
        entity.setScheduleCron(request.getScheduleCron());
        entity.setOwnerId(request.getOwnerId());
        entity.setActive(request.isActive());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(reportDefinitionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ReportDefinitionDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ReportDefinitionDto.Response> list(String tenantId, Pageable pageable) {
        return reportDefinitionRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReportDefinitionDto.Response> listAll(String tenantId) {
        return reportDefinitionRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDefinitionDto.Response> listActive(String tenantId) {
        return reportDefinitionRepository
                .findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReportDefinitionDto.Response> listByCategory(String tenantId, String category, Pageable pageable) {
        return reportDefinitionRepository
                .findByTenantIdAndCategoryAndDeletedFalse(tenantId, category, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ReportDefinitionDto.Response update(String tenantId, UUID id, ReportDefinitionDto.UpdateRequest request, String currentUser) {
        ReportDefinition entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getCode() != null) entity.setCode(request.getCode());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getCategory() != null) entity.setCategory(request.getCategory());
        if (request.getQueryConfig() != null) entity.setQueryConfig(request.getQueryConfig());
        if (request.getParametersConfig() != null) entity.setParametersConfig(request.getParametersConfig());
        if (request.getOutputFormats() != null) entity.setOutputFormats(request.getOutputFormats());
        if (request.getScheduleCron() != null) entity.setScheduleCron(request.getScheduleCron());
        if (request.getOwnerId() != null) entity.setOwnerId(request.getOwnerId());
        if (request.getActive() != null) entity.setActive(request.getActive());
        entity.setUpdatedBy(currentUser);
        return toResponse(reportDefinitionRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        ReportDefinition entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        reportDefinitionRepository.save(entity);
    }

    private ReportDefinition findOrThrow(String tenantId, UUID id) {
        return reportDefinitionRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("ReportDefinition", "id", id));
    }

    private ReportDefinitionDto.Response toResponse(ReportDefinition e) {
        return ReportDefinitionDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .code(e.getCode())
                .description(e.getDescription())
                .category(e.getCategory())
                .queryConfig(e.getQueryConfig())
                .parametersConfig(e.getParametersConfig())
                .outputFormats(e.getOutputFormats())
                .scheduleCron(e.getScheduleCron())
                .ownerId(e.getOwnerId())
                .active(e.isActive())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
