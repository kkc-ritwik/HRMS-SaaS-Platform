package com.hrms.leave.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.common.dto.PaginationMeta;
import com.hrms.leave.dto.ShiftDto;
import com.hrms.leave.service.ShiftService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shifts")
@RequiredArgsConstructor
@Tag(name = "Shifts", description = "Manage work shifts and employee shift assignments")
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasAuthority('ATTENDANCE:WRITE')")
    @Operation(summary = "Create a shift")
    public ResponseEntity<ApiResponse<ShiftDto.Response>> create(
            @Valid @RequestBody ShiftDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(shiftService.createShift(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @Operation(summary = "List all shifts (paginated)")
    public ResponseEntity<ApiResponse<List<ShiftDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ShiftDto.Response> page = shiftService.listShifts(tenantId(), pageable);
        PaginationMeta meta = shiftService.buildMeta(page);
        return ResponseEntity.ok(ApiResponse.ok(page.getContent(), meta));
    }

    @GetMapping("/active")
    @Operation(summary = "List active shifts (for dropdowns)")
    public ResponseEntity<ApiResponse<List<ShiftDto.Response>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.listActiveShifts(tenantId())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shift by ID")
    public ResponseEntity<ApiResponse<ShiftDto.Response>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(shiftService.getShift(tenantId(), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE:WRITE')")
    @Operation(summary = "Update a shift")
    public ResponseEntity<ApiResponse<ShiftDto.Response>> update(
            @PathVariable UUID id,
            @RequestBody ShiftDto.UpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                shiftService.updateShift(tenantId(), id, req, currentUserId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ATTENDANCE:WRITE')")
    @Operation(summary = "Delete a shift")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        shiftService.deleteShift(tenantId(), id, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{shiftId}/assign")
    @PreAuthorize("hasAuthority('ATTENDANCE:WRITE')")
    @Operation(summary = "Assign a shift to an employee for a date range")
    public ResponseEntity<ApiResponse<Void>> assign(
            @PathVariable UUID shiftId,
            @Valid @RequestBody ShiftDto.AssignRequest req) {
        shiftService.assignShiftToEmployee(tenantId(), shiftId, req, currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
