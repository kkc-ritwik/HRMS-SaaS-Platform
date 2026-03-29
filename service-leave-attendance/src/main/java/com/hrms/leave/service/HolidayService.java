package com.hrms.leave.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.HolidayDto;
import com.hrms.leave.entity.Holiday;
import com.hrms.leave.repository.HolidayRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;

    @Transactional
    public HolidayDto.Response create(String tenantId, HolidayDto.CreateRequest req,
                                       String currentUser) {
        Holiday holiday = new Holiday();
        holiday.setTenantId(tenantId);
        holiday.setName(req.getName());
        holiday.setDate(req.getDate());
        holiday.setType(req.getType() != null ? req.getType() : Holiday.HolidayType.NATIONAL);
        holiday.setLocationIds(req.getLocationIds());
        holiday.setActive(true);
        holiday.setYear(req.getDate().getYear());
        holiday.setCreatedBy(currentUser);
        holiday.setUpdatedBy(currentUser);
        return toResponse(holidayRepository.save(holiday));
    }

    @Transactional(readOnly = true)
    public Page<HolidayDto.Response> list(String tenantId, int year, Pageable pageable) {
        return holidayRepository.findByTenantIdAndYearAndDeletedFalse(tenantId, year, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public HolidayDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    @Transactional(readOnly = true)
    public List<HolidayDto.Response> getByLocationAndYear(String tenantId, UUID locationId,
                                                           int year) {
        return holidayRepository.findByLocationAndYear(tenantId, locationId, year)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public HolidayDto.Response update(String tenantId, UUID id,
                                       HolidayDto.UpdateRequest req, String currentUser) {
        Holiday holiday = findOrThrow(tenantId, id);
        if (req.getName() != null)    holiday.setName(req.getName());
        if (req.getDate() != null)    { holiday.setDate(req.getDate()); holiday.setYear(req.getDate().getYear()); }
        if (req.getType() != null)    holiday.setType(req.getType());
        if (req.getLocationIds() != null) holiday.setLocationIds(req.getLocationIds());
        if (req.getActive() != null)  holiday.setActive(req.getActive());
        holiday.setUpdatedBy(currentUser);
        return toResponse(holidayRepository.save(holiday));
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        Holiday holiday = findOrThrow(tenantId, id);
        holiday.setDeleted(true);
        holiday.setUpdatedBy(currentUser);
        holidayRepository.save(holiday);
    }

    /** Returns the set of holiday dates for a tenant in a given year — used by leave calculation. */
    @Transactional(readOnly = true)
    public Set<LocalDate> getHolidayDates(String tenantId, int year) {
        return holidayRepository
                .findByTenantIdAndYearAndActiveAndDeletedFalse(tenantId, year, true)
                .stream().map(Holiday::getDate).collect(Collectors.toSet());
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── private ───────────────────────────────────────────────────────────────

    private Holiday findOrThrow(String tenantId, UUID id) {
        return holidayRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday", "id", id));
    }

    HolidayDto.Response toResponse(Holiday h) {
        return HolidayDto.Response.builder()
                .id(h.getId()).name(h.getName()).date(h.getDate())
                .type(h.getType()).locationIds(h.getLocationIds())
                .active(h.isActive()).year(h.getYear()).createdAt(h.getCreatedAt())
                .build();
    }
}
