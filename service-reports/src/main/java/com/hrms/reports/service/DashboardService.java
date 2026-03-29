package com.hrms.reports.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.reports.dto.DashboardDto;
import com.hrms.reports.entity.Dashboard;
import com.hrms.reports.repository.DashboardRepository;
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
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    @Transactional
    public DashboardDto.Response create(String tenantId, DashboardDto.CreateRequest request, String currentUser) {
        Dashboard entity = new Dashboard();
        entity.setTenantId(tenantId);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setOwnerId(request.getOwnerId());
        entity.setShared(request.isShared());
        entity.setSharedWith(request.getSharedWith());
        entity.setTheme(request.getTheme());
        entity.setDefaultDashboard(request.isDefaultDashboard());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(dashboardRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public DashboardDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<DashboardDto.Response> list(String tenantId, Pageable pageable) {
        return dashboardRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DashboardDto.Response> listAll(String tenantId) {
        return dashboardRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DashboardDto.Response> listByOwner(String tenantId, UUID ownerId) {
        return dashboardRepository
                .findByTenantIdAndOwnerIdAndDeletedFalse(tenantId, ownerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<DashboardDto.Response> listShared(String tenantId, Pageable pageable) {
        return dashboardRepository
                .findByTenantIdAndSharedAndDeletedFalse(tenantId, true, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public DashboardDto.Response update(String tenantId, UUID id, DashboardDto.UpdateRequest request, String currentUser) {
        Dashboard entity = findOrThrow(tenantId, id);
        if (request.getName() != null) entity.setName(request.getName());
        if (request.getDescription() != null) entity.setDescription(request.getDescription());
        if (request.getShared() != null) entity.setShared(request.getShared());
        if (request.getSharedWith() != null) entity.setSharedWith(request.getSharedWith());
        if (request.getLayoutConfig() != null) entity.setLayoutConfig(request.getLayoutConfig());
        if (request.getTheme() != null) entity.setTheme(request.getTheme());
        if (request.getDefaultDashboard() != null) entity.setDefaultDashboard(request.getDefaultDashboard());
        entity.setUpdatedBy(currentUser);
        return toResponse(dashboardRepository.save(entity));
    }

    @Transactional
    public DashboardDto.Response setDefault(String tenantId, UUID id, UUID ownerId, String currentUser) {
        // Clear existing default for this owner
        dashboardRepository.findByTenantIdAndOwnerIdAndDefaultDashboardAndDeletedFalse(tenantId, ownerId, true)
                .ifPresent(existing -> {
                    existing.setDefaultDashboard(false);
                    existing.setUpdatedBy(currentUser);
                    dashboardRepository.save(existing);
                });
        // Set the new default
        Dashboard entity = findOrThrow(tenantId, id);
        entity.setDefaultDashboard(true);
        entity.setUpdatedBy(currentUser);
        return toResponse(dashboardRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Dashboard entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        dashboardRepository.save(entity);
    }

    private Dashboard findOrThrow(String tenantId, UUID id) {
        return dashboardRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Dashboard", "id", id));
    }

    private DashboardDto.Response toResponse(Dashboard e) {
        return DashboardDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .name(e.getName())
                .description(e.getDescription())
                .ownerId(e.getOwnerId())
                .shared(e.isShared())
                .sharedWith(e.getSharedWith())
                .layoutConfig(e.getLayoutConfig())
                .theme(e.getTheme())
                .defaultDashboard(e.isDefaultDashboard())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
