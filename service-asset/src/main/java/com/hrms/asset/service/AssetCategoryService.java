package com.hrms.asset.service;

import com.hrms.asset.dto.AssetCategoryDto;
import com.hrms.asset.entity.AssetCategory;
import com.hrms.asset.repository.AssetCategoryRepository;
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
public class AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;

    @Transactional
    public AssetCategoryDto.Response create(String tenantId, AssetCategoryDto.CreateRequest req,
                                             String currentUser) {
        AssetCategory category = new AssetCategory();
        category.setTenantId(tenantId);
        category.setName(req.getName());
        category.setCode(req.getCode());
        category.setDescription(req.getDescription());
        category.setDepreciationRate(req.getDepreciationRate());
        category.setLifespanYears(req.getLifespanYears());
        category.setActive(req.isActive());
        category.setCreatedBy(currentUser);
        category.setUpdatedBy(currentUser);
        return toResponse(assetCategoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public Page<AssetCategoryDto.Response> list(String tenantId, Pageable pageable) {
        return assetCategoryRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryDto.Response> listAll(String tenantId) {
        return assetCategoryRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetCategoryDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional
    public AssetCategoryDto.Response update(String tenantId, UUID id,
                                             AssetCategoryDto.UpdateRequest req,
                                             String currentUser) {
        AssetCategory category = findOrThrow(tenantId, id);
        if (req.getName() != null)             category.setName(req.getName());
        if (req.getCode() != null)             category.setCode(req.getCode());
        if (req.getDescription() != null)      category.setDescription(req.getDescription());
        if (req.getDepreciationRate() != null) category.setDepreciationRate(req.getDepreciationRate());
        if (req.getLifespanYears() != null)    category.setLifespanYears(req.getLifespanYears());
        if (req.getActive() != null)           category.setActive(req.getActive());
        category.setUpdatedBy(currentUser);
        return toResponse(assetCategoryRepository.save(category));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        AssetCategory category = findOrThrow(tenantId, id);
        category.setDeleted(true);
        category.setUpdatedBy(currentUser);
        assetCategoryRepository.save(category);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private AssetCategory findOrThrow(String tenantId, UUID id) {
        return assetCategoryRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("AssetCategory", "id", id));
    }

    AssetCategoryDto.Response toResponse(AssetCategory c) {
        return AssetCategoryDto.Response.builder()
                .id(c.getId())
                .tenantId(c.getTenantId())
                .name(c.getName())
                .code(c.getCode())
                .description(c.getDescription())
                .depreciationRate(c.getDepreciationRate())
                .lifespanYears(c.getLifespanYears())
                .active(c.isActive())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
