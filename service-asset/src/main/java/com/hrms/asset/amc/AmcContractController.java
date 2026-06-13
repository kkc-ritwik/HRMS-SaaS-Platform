package com.hrms.asset.amc;

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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Annual Maintenance Contract CRUD. The {@link AmcContract} entity + {@code amc_contracts}
 * table already existed; this exposes the missing REST layer used by the frontend
 * AMC Contracts page.
 */
@RestController
@RequestMapping("/api/v1/assets/amc-contracts")
@RequiredArgsConstructor
@Tag(name = "AMC Contracts", description = "Annual maintenance / warranty / support contracts")
public class AmcContractController {

    private final AmcContractRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List AMC contracts")
    public ResponseEntity<ApiResponse<List<AmcContract>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
                repository.findByTenantIdAndDeletedFalseOrderByEndDateAsc(TenantContext.get())));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "List contracts expiring within N days")
    public ResponseEntity<ApiResponse<List<AmcContract>>> expiring(
            @RequestParam(defaultValue = "90") int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return ResponseEntity.ok(ApiResponse.ok(
                repository.findByTenantIdAndDeletedFalseAndEndDateBeforeOrderByEndDateAsc(TenantContext.get(), cutoff)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:READ')")
    @Operation(summary = "Get an AMC contract by ID")
    public ResponseEntity<ApiResponse<AmcContract>> get(@PathVariable UUID id) {
        AmcContract c = repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("AMC contract not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(c));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Create an AMC contract")
    public ResponseEntity<ApiResponse<AmcContract>> create(@Valid @RequestBody AmcContract body) {
        body.setTenantId(TenantContext.get());
        body.setCreatedBy(currentUserId());
        if (body.getStatus() == null) body.setStatus(AmcContract.Status.ACTIVE);
        AmcContract saved = repository.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Update an AMC contract")
    public ResponseEntity<ApiResponse<AmcContract>> update(@PathVariable UUID id, @RequestBody AmcContract body) {
        AmcContract existing = repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("AMC contract not found: " + id));
        body.setId(existing.getId());
        body.setTenantId(existing.getTenantId());
        body.setCreatedBy(existing.getCreatedBy());
        body.setCreatedAt(existing.getCreatedAt());
        body.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(repository.save(body)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET:WRITE')")
    @Operation(summary = "Soft-delete an AMC contract")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        AmcContract existing = repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("AMC contract not found: " + id));
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
