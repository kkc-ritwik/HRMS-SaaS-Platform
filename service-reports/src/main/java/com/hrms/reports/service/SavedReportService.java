package com.hrms.reports.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.reports.dto.SavedReportDto;
import com.hrms.reports.entity.SavedReport;
import com.hrms.reports.entity.SavedReport.ReportStatus;
import com.hrms.reports.repository.SavedReportRepository;
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
public class SavedReportService {

    private final SavedReportRepository savedReportRepository;

    @Transactional
    public SavedReportDto.Response create(String tenantId, SavedReportDto.CreateRequest request, String currentUser) {
        SavedReport entity = new SavedReport();
        entity.setTenantId(tenantId);
        entity.setDefinitionId(request.getDefinitionId());
        entity.setName(request.getName());
        entity.setFilters(request.getFilters());
        entity.setGeneratedBy(request.getGeneratedBy());
        entity.setGeneratedAt(Instant.now());
        entity.setFileFormat(request.getFileFormat());
        entity.setStatus(ReportStatus.PENDING);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(savedReportRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public SavedReportDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<SavedReportDto.Response> list(String tenantId, Pageable pageable) {
        return savedReportRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<SavedReportDto.Response> listAll(String tenantId) {
        return savedReportRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SavedReportDto.Response> listByDefinition(String tenantId, UUID definitionId, Pageable pageable) {
        return savedReportRepository
                .findByTenantIdAndDefinitionIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, definitionId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SavedReportDto.Response> listByUser(String tenantId, UUID generatedBy, Pageable pageable) {
        return savedReportRepository
                .findByTenantIdAndGeneratedByAndDeletedFalseOrderByCreatedAtDesc(tenantId, generatedBy, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SavedReportDto.Response update(String tenantId, UUID id, SavedReportDto.UpdateRequest request, String currentUser) {
        SavedReport entity = findOrThrow(tenantId, id);
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getFileUrl() != null) entity.setFileUrl(request.getFileUrl());
        if (request.getErrorMessage() != null) entity.setErrorMessage(request.getErrorMessage());
        if (request.getGeneratedAt() != null) entity.setGeneratedAt(request.getGeneratedAt());
        entity.setUpdatedBy(currentUser);
        return toResponse(savedReportRepository.save(entity));
    }

    @Transactional
    public SavedReportDto.Response markCompleted(String tenantId, UUID id, String fileUrl, String currentUser) {
        SavedReport entity = findOrThrow(tenantId, id);
        entity.setStatus(ReportStatus.COMPLETED);
        entity.setFileUrl(fileUrl);
        entity.setUpdatedBy(currentUser);
        return toResponse(savedReportRepository.save(entity));
    }

    @Transactional
    public SavedReportDto.Response markFailed(String tenantId, UUID id, String error, String currentUser) {
        SavedReport entity = findOrThrow(tenantId, id);
        entity.setStatus(ReportStatus.FAILED);
        entity.setErrorMessage(error);
        entity.setUpdatedBy(currentUser);
        return toResponse(savedReportRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        SavedReport entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        savedReportRepository.save(entity);
    }

    private SavedReport findOrThrow(String tenantId, UUID id) {
        return savedReportRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("SavedReport", "id", id));
    }

    private SavedReportDto.Response toResponse(SavedReport e) {
        return SavedReportDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .definitionId(e.getDefinitionId())
                .name(e.getName())
                .filters(e.getFilters())
                .generatedBy(e.getGeneratedBy())
                .generatedAt(e.getGeneratedAt())
                .fileUrl(e.getFileUrl())
                .fileFormat(e.getFileFormat())
                .status(e.getStatus())
                .errorMessage(e.getErrorMessage())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
