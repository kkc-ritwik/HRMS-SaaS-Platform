package com.hrms.reports.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.reports.dto.DashboardWidgetDto;
import com.hrms.reports.entity.DashboardWidget;
import com.hrms.reports.repository.DashboardWidgetRepository;
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
public class DashboardWidgetService {

    private final DashboardWidgetRepository dashboardWidgetRepository;

    @Transactional
    public DashboardWidgetDto.Response create(String tenantId, DashboardWidgetDto.CreateRequest request, String currentUser) {
        DashboardWidget entity = new DashboardWidget();
        entity.setTenantId(tenantId);
        entity.setDashboardId(request.getDashboardId());
        entity.setWidgetType(request.getWidgetType());
        entity.setTitle(request.getTitle());
        entity.setDataSource(request.getDataSource());
        entity.setQueryConfig(request.getQueryConfig());
        entity.setDisplayConfig(request.getDisplayConfig());
        entity.setPositionX(request.getPositionX());
        entity.setPositionY(request.getPositionY());
        entity.setWidth(request.getWidth());
        entity.setHeight(request.getHeight());
        entity.setRefreshSeconds(request.getRefreshSeconds());
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(dashboardWidgetRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public DashboardWidgetDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<DashboardWidgetDto.Response> list(String tenantId, Pageable pageable) {
        return dashboardWidgetRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DashboardWidgetDto.Response> listAll(String tenantId) {
        return dashboardWidgetRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DashboardWidgetDto.Response> listByDashboard(String tenantId, UUID dashboardId) {
        return dashboardWidgetRepository
                .findByTenantIdAndDashboardIdAndDeletedFalseOrderByPositionYAscPositionXAsc(tenantId, dashboardId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DashboardWidgetDto.Response update(String tenantId, UUID id, DashboardWidgetDto.UpdateRequest request, String currentUser) {
        DashboardWidget entity = findOrThrow(tenantId, id);
        if (request.getWidgetType() != null) entity.setWidgetType(request.getWidgetType());
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getDataSource() != null) entity.setDataSource(request.getDataSource());
        if (request.getQueryConfig() != null) entity.setQueryConfig(request.getQueryConfig());
        if (request.getDisplayConfig() != null) entity.setDisplayConfig(request.getDisplayConfig());
        if (request.getPositionX() != null) entity.setPositionX(request.getPositionX());
        if (request.getPositionY() != null) entity.setPositionY(request.getPositionY());
        if (request.getWidth() != null) entity.setWidth(request.getWidth());
        if (request.getHeight() != null) entity.setHeight(request.getHeight());
        if (request.getRefreshSeconds() != null) entity.setRefreshSeconds(request.getRefreshSeconds());
        entity.setUpdatedBy(currentUser);
        return toResponse(dashboardWidgetRepository.save(entity));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        DashboardWidget entity = findOrThrow(tenantId, id);
        entity.setDeleted(true);
        entity.setUpdatedBy(currentUser);
        dashboardWidgetRepository.save(entity);
    }

    private DashboardWidget findOrThrow(String tenantId, UUID id) {
        return dashboardWidgetRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("DashboardWidget", "id", id));
    }

    private DashboardWidgetDto.Response toResponse(DashboardWidget e) {
        return DashboardWidgetDto.Response.builder()
                .id(e.getId())
                .tenantId(e.getTenantId())
                .dashboardId(e.getDashboardId())
                .widgetType(e.getWidgetType())
                .title(e.getTitle())
                .dataSource(e.getDataSource())
                .queryConfig(e.getQueryConfig())
                .displayConfig(e.getDisplayConfig())
                .positionX(e.getPositionX())
                .positionY(e.getPositionY())
                .width(e.getWidth())
                .height(e.getHeight())
                .refreshSeconds(e.getRefreshSeconds())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
