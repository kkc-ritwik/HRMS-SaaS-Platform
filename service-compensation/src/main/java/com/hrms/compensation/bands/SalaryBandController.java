package com.hrms.compensation.bands;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Salary bands per pay grade / country (min / mid / max with compa-ratio reference). */
@RestController
@RequestMapping("/api/v1/compensation/bands")
@RequiredArgsConstructor
public class SalaryBandController {

    private final SalaryBandRepository repo;

    @GetMapping
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<List<SalaryBand>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
                repo.findByTenantIdAndDeletedFalse(TenantContext.get())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('COMPENSATION:READ')")
    public ResponseEntity<ApiResponse<SalaryBand>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repo.findById(id).orElse(null)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COMPENSATION:WRITE')")
    public ResponseEntity<ApiResponse<SalaryBand>> create(@RequestBody SalaryBand band) {
        band.setTenantId(TenantContext.get());
        return ResponseEntity.ok(ApiResponse.ok(repo.save(band)));
    }
}
