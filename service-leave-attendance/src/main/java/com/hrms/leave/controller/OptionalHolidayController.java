package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.leave.entity.Holiday;
import com.hrms.leave.entity.OptionalHolidaySelection;
import com.hrms.leave.repository.HolidayRepository;
import com.hrms.leave.repository.OptionalHolidaySelectionRepository;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Optional (restricted) holiday selection — employees pick up to an annual quota of
 * optional holidays from the published list.
 */
@RestController
@RequestMapping("/api/v1/holidays/optional")
@RequiredArgsConstructor
@Tag(name = "Optional Holidays", description = "Restricted-holiday quota and selection")
public class OptionalHolidayController {

    private final HolidayRepository holidayRepository;
    private final OptionalHolidaySelectionRepository selectionRepository;

    @Value("${hrms.holidays.optional-quota:2}")
    private int optionalQuota;

    @GetMapping("/quota")
    @PreAuthorize("hasAuthority('LEAVE:READ')")
    @Operation(summary = "Optional-holiday quota, usage and available picks for an employee")
    public ResponseEntity<ApiResponse<Map<String, Object>>> quota(
            @RequestParam UUID employeeId,
            @RequestParam(required = false) Integer year) {
        String tenantId = TenantContext.get();
        int yr = year != null ? year : LocalDate.now().getYear();

        List<Holiday> optional = holidayRepository
                .findByTenantIdAndYearAndTypeAndActiveAndDeletedFalse(tenantId, yr, Holiday.HolidayType.OPTIONAL, true);
        List<OptionalHolidaySelection> selected = selectionRepository
                .findByTenantIdAndEmployeeIdAndYearAndDeletedFalse(tenantId, employeeId, yr);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("year", yr);
        out.put("quota", optionalQuota);
        out.put("used", selected.size());
        out.put("remaining", Math.max(0, optionalQuota - selected.size()));
        out.put("selectedHolidayIds", selected.stream().map(OptionalHolidaySelection::getHolidayId).toList());
        out.put("availableHolidays", optional);
        return ResponseEntity.ok(ApiResponse.ok(out));
    }

    @PostMapping("/select")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Transactional
    @Operation(summary = "Select optional holidays for an employee (replaces existing picks for the year)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> select(@RequestBody SelectRequest req) {
        String tenantId = TenantContext.get();
        int yr = req.year() != null ? req.year() : LocalDate.now().getYear();
        List<UUID> holidayIds = req.holidayIds() == null ? List.of() : req.holidayIds();

        if (holidayIds.size() > optionalQuota) {
            throw new IllegalArgumentException("Selection exceeds the optional-holiday quota of " + optionalQuota);
        }

        selectionRepository.deleteByTenantIdAndEmployeeIdAndYear(tenantId, req.employeeId(), yr);
        String actor = currentUserId();
        for (UUID hid : holidayIds) {
            OptionalHolidaySelection s = new OptionalHolidaySelection();
            s.setTenantId(tenantId);
            s.setEmployeeId(req.employeeId());
            s.setHolidayId(hid);
            s.setYear(yr);
            s.setCreatedBy(actor);
            selectionRepository.save(s);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("year", yr);
        out.put("selected", holidayIds.size());
        out.put("remaining", Math.max(0, optionalQuota - holidayIds.size()));
        return ResponseEntity.ok(ApiResponse.ok(out));
    }

    private String currentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof UserPrincipal up ? up.getId() : null;
    }

    public record SelectRequest(UUID employeeId, Integer year, List<UUID> holidayIds) {}
}
