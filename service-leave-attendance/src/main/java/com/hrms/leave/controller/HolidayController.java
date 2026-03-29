package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.leave.dto.HolidayDto;
import com.hrms.leave.service.HolidayService;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/holidays")
@RequiredArgsConstructor
@Tag(name = "Holidays", description = "Manage public and restricted holidays")
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Create a holiday")
    public ResponseEntity<ApiResponse<HolidayDto.Response>> create(
            @Valid @RequestBody HolidayDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(holidayService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @Operation(summary = "List holidays for a given year")
    public ResponseEntity<ApiResponse<List<HolidayDto.Response>>> list(
            @RequestParam(required = false) Integer year,
            @PageableDefault(size = 50) Pageable pageable) {
        int y = year != null ? year : LocalDate.now().getYear();
        Page<HolidayDto.Response> page = holidayService.list(tenantId(), y, pageable);
        PaginationMeta meta = holidayService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/by-location")
    @Operation(summary = "Get holidays for a specific location and year")
    public ResponseEntity<ApiResponse<List<HolidayDto.Response>>> byLocation(
            @RequestParam UUID locationId,
            @RequestParam(required = false) Integer year) {
        int y = year != null ? year : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponse.ok(
                holidayService.getByLocationAndYear(tenantId(), locationId, y)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a holiday by ID")
    public ResponseEntity<ApiResponse<HolidayDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(holidayService.getById(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Update a holiday")
    public ResponseEntity<ApiResponse<HolidayDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody HolidayDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                holidayService.update(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('LEAVE:WRITE')")
    @Operation(summary = "Delete a holiday")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        holidayService.delete(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
