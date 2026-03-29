package com.hrms.asset.service;

import com.hrms.asset.dto.AssetDto;
import com.hrms.asset.entity.Asset;
import com.hrms.asset.entity.Asset.AssetStatus;
import com.hrms.asset.repository.AssetRepository;
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
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public AssetDto.Response create(String tenantId, AssetDto.CreateRequest req, String currentUser) {
        Asset asset = new Asset();
        asset.setTenantId(tenantId);
        asset.setCategoryId(req.getCategoryId());
        asset.setName(req.getName());
        asset.setCode(req.getCode());
        asset.setSerialNumber(req.getSerialNumber());
        asset.setMake(req.getMake());
        asset.setModel(req.getModel());
        asset.setPurchaseDate(req.getPurchaseDate());
        asset.setPurchasePrice(req.getPurchasePrice());
        asset.setCurrentValue(req.getCurrentValue());
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setLocation(req.getLocation());
        asset.setWarrantyExpiry(req.getWarrantyExpiry());
        asset.setNotes(req.getNotes());
        asset.setCreatedBy(currentUser);
        asset.setUpdatedBy(currentUser);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public Page<AssetDto.Response> list(String tenantId, Pageable pageable) {
        return assetRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetDto.Response> listAll(String tenantId) {
        return assetRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<AssetDto.Response> listByStatus(String tenantId, AssetStatus status) {
        return assetRepository.findByTenantIdAndStatusAndDeletedFalse(tenantId, status)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssetDto.Response> listByCategory(String tenantId, UUID categoryId) {
        return assetRepository.findByTenantIdAndCategoryIdAndDeletedFalse(tenantId, categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public AssetDto.Response update(String tenantId, UUID id, AssetDto.UpdateRequest req,
                                     String currentUser) {
        Asset asset = findOrThrow(tenantId, id);
        if (req.getCategoryId() != null)    asset.setCategoryId(req.getCategoryId());
        if (req.getName() != null)          asset.setName(req.getName());
        if (req.getCode() != null)          asset.setCode(req.getCode());
        if (req.getSerialNumber() != null)  asset.setSerialNumber(req.getSerialNumber());
        if (req.getMake() != null)          asset.setMake(req.getMake());
        if (req.getModel() != null)         asset.setModel(req.getModel());
        if (req.getPurchaseDate() != null)  asset.setPurchaseDate(req.getPurchaseDate());
        if (req.getPurchasePrice() != null) asset.setPurchasePrice(req.getPurchasePrice());
        if (req.getCurrentValue() != null)  asset.setCurrentValue(req.getCurrentValue());
        if (req.getStatus() != null)        asset.setStatus(req.getStatus());
        if (req.getLocation() != null)      asset.setLocation(req.getLocation());
        if (req.getWarrantyExpiry() != null) asset.setWarrantyExpiry(req.getWarrantyExpiry());
        if (req.getNotes() != null)         asset.setNotes(req.getNotes());
        asset.setUpdatedBy(currentUser);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Asset asset = findOrThrow(tenantId, id);
        asset.setDeleted(true);
        asset.setUpdatedBy(currentUser);
        assetRepository.save(asset);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Asset findOrThrow(String tenantId, UUID id) {
        return assetRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
    }

    AssetDto.Response toResponse(Asset a) {
        return AssetDto.Response.builder()
                .id(a.getId())
                .tenantId(a.getTenantId())
                .categoryId(a.getCategoryId())
                .name(a.getName())
                .code(a.getCode())
                .serialNumber(a.getSerialNumber())
                .make(a.getMake())
                .model(a.getModel())
                .purchaseDate(a.getPurchaseDate())
                .purchasePrice(a.getPurchasePrice())
                .currentValue(a.getCurrentValue())
                .status(a.getStatus())
                .location(a.getLocation())
                .warrantyExpiry(a.getWarrantyExpiry())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
