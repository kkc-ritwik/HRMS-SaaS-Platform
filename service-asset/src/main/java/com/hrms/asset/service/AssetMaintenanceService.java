package com.hrms.asset.service;

import com.hrms.asset.dto.AssetMaintenanceDto;
import com.hrms.asset.entity.AssetMaintenance;
import com.hrms.asset.repository.AssetMaintenanceRepository;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetMaintenanceService {

    private final AssetMaintenanceRepository assetMaintenanceRepository;

    @Transactional
    public AssetMaintenanceDto.Response create(String tenantId, AssetMaintenanceDto.CreateRequest req,
                                                String currentUser) {
        AssetMaintenance maintenance = new AssetMaintenance();
        maintenance.setTenantId(tenantId);
        maintenance.setAssetId(req.getAssetId());
        maintenance.setMaintenanceType(req.getMaintenanceType());
        maintenance.setDescription(req.getDescription());
        maintenance.setScheduledDate(req.getScheduledDate());
        maintenance.setCost(req.getCost());
        maintenance.setVendor(req.getVendor());
        maintenance.setStatus(AssetMaintenance.MaintenanceStatus.SCHEDULED);
        maintenance.setNotes(req.getNotes());
        maintenance.setCreatedBy(currentUser);
        maintenance.setUpdatedBy(currentUser);
        return toResponse(assetMaintenanceRepository.save(maintenance));
    }

    @Transactional(readOnly = true)
    public Page<AssetMaintenanceDto.Response> list(String tenantId, Pageable pageable) {
        return assetMaintenanceRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetMaintenanceDto.Response> listAll(String tenantId) {
        return assetMaintenanceRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetMaintenanceDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AssetMaintenanceDto.Response> listByAsset(String tenantId, UUID assetId) {
        return assetMaintenanceRepository.findByTenantIdAndAssetIdAndDeletedFalse(tenantId, assetId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AssetMaintenanceDto.Response update(String tenantId, UUID id,
                                                AssetMaintenanceDto.UpdateRequest req,
                                                String currentUser) {
        AssetMaintenance maintenance = findOrThrow(tenantId, id);
        if (req.getMaintenanceType() != null)  maintenance.setMaintenanceType(req.getMaintenanceType());
        if (req.getDescription() != null)      maintenance.setDescription(req.getDescription());
        if (req.getScheduledDate() != null)    maintenance.setScheduledDate(req.getScheduledDate());
        if (req.getCompletedDate() != null)    maintenance.setCompletedDate(req.getCompletedDate());
        if (req.getCost() != null)             maintenance.setCost(req.getCost());
        if (req.getVendor() != null)           maintenance.setVendor(req.getVendor());
        if (req.getStatus() != null)           maintenance.setStatus(req.getStatus());
        if (req.getNotes() != null)            maintenance.setNotes(req.getNotes());
        maintenance.setUpdatedBy(currentUser);
        return toResponse(assetMaintenanceRepository.save(maintenance));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        AssetMaintenance maintenance = findOrThrow(tenantId, id);
        maintenance.setDeleted(true);
        maintenance.setUpdatedBy(currentUser);
        assetMaintenanceRepository.save(maintenance);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AssetMaintenance findOrThrow(String tenantId, UUID id) {
        return assetMaintenanceRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AssetMaintenance", "id", id));
    }

    AssetMaintenanceDto.Response toResponse(AssetMaintenance m) {
        return AssetMaintenanceDto.Response.builder()
                .id(m.getId())
                .tenantId(m.getTenantId())
                .assetId(m.getAssetId())
                .maintenanceType(m.getMaintenanceType())
                .description(m.getDescription())
                .scheduledDate(m.getScheduledDate())
                .completedDate(m.getCompletedDate())
                .cost(m.getCost())
                .vendor(m.getVendor())
                .status(m.getStatus())
                .notes(m.getNotes())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
