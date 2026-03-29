package com.hrms.corehr.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.corehr.dto.LocationDto;
import com.hrms.corehr.entity.Location;
import com.hrms.corehr.mapper.LocationMapper;
import com.hrms.corehr.repository.LocationRepository;
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
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper     mapper;

    @Transactional
    public LocationDto.Response create(String tenantId, LocationDto.CreateRequest req,
                                        String currentUser) {
        if (locationRepository.existsByCodeAndTenantIdAndDeletedFalse(req.getCode(), tenantId)) {
            throw new DuplicateResourceException("Location", "code", req.getCode());
        }
        Location location = mapper.toEntity(req);
        location.setTenantId(tenantId);
        location.setCreatedBy(currentUser);
        location.setUpdatedBy(currentUser);
        return mapper.toResponse(locationRepository.save(location));
    }

    @Transactional(readOnly = true)
    public Page<LocationDto.Response> list(String tenantId, String search, Pageable pageable) {
        Page<Location> page = (search == null || search.isBlank())
                ? locationRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                : locationRepository.searchByTenant(tenantId, search, pageable);
        return page.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public LocationDto.Response getById(String tenantId, UUID id) {
        return mapper.toResponse(findOrThrow(tenantId, id));
    }

    @Transactional
    public LocationDto.Response update(String tenantId, UUID id,
                                        LocationDto.UpdateRequest req, String currentUser) {
        Location location = findOrThrow(tenantId, id);
        mapper.updateEntity(req, location);
        location.setUpdatedBy(currentUser);
        return mapper.toResponse(locationRepository.save(location));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Location location = findOrThrow(tenantId, id);
        location.setDeleted(true);
        location.setUpdatedBy(currentUser);
        locationRepository.save(location);
        log.info("[CoreHR] Location {} soft-deleted by {}", id, currentUser);
    }

    @Transactional(readOnly = true)
    public List<LocationDto.Response> listActive(String tenantId) {
        return locationRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(mapper::toResponse).toList();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Location findOrThrow(String tenantId, UUID id) {
        return locationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", "id", id));
    }
}
