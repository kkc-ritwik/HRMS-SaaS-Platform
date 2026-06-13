package com.hrms.asset.vendor;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Vendor master CRUD — external parties HR/Finance pay (asset suppliers, AMC
 * providers, recruitment/BGV agencies, training partners, etc.). The {@link Vendor}
 * entity + {@code vendors} table already existed; this exposes the REST layer.
 */
@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
@Tag(name = "Vendors", description = "Vendor master management")
public class VendorController {

    private final VendorRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List vendors")
    public ResponseEntity<ApiResponse<List<Vendor>>> list(
            @RequestParam(required = false) Vendor.Category category) {
        String tenantId = TenantContext.get();
        List<Vendor> vendors = category != null
                ? repository.findByTenantIdAndCategoryAndDeletedFalseOrderByCreatedAtDesc(tenantId, category)
                : repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(vendors));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get a vendor by ID")
    public ResponseEntity<ApiResponse<Vendor>> get(@PathVariable UUID id) {
        Vendor vendor = repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(vendor));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create a vendor")
    public ResponseEntity<ApiResponse<Vendor>> create(@Valid @RequestBody Vendor body) {
        body.setTenantId(TenantContext.get());
        body.setCreatedBy(currentUserId());
        if (body.getActive() == null) body.setActive(true);
        Vendor saved = repository.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update a vendor")
    public ResponseEntity<ApiResponse<Vendor>> update(@PathVariable UUID id, @RequestBody Vendor body) {
        Vendor existing = repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + id));
        body.setId(existing.getId());
        body.setTenantId(existing.getTenantId());
        body.setCreatedBy(existing.getCreatedBy());
        body.setCreatedAt(existing.getCreatedAt());
        body.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(repository.save(body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete a vendor")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Vendor existing = repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + id));
        existing.setDeleted(true);
        existing.setUpdatedBy(currentUserId());
        repository.save(existing);
        return ResponseEntity.noContent().build();
    }

    private String currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof UserPrincipal up ? up.getId() : null;
    }
}
