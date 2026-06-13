package com.hrms.auth.tenant;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Self-service tenant settings — branding (logo/colours) and feature-flag overrides
 * for the current tenant.
 */
@RestController
@RequestMapping("/api/v1/tenants/current")
@RequiredArgsConstructor
@Tag(name = "Tenant Settings", description = "Branding and feature-flag management for the current tenant")
public class TenantSettingsController {

    private final TenantSettingsRepository repository;

    @GetMapping("/branding")
    @Operation(summary = "Get the current tenant's branding")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBranding() {
        return ResponseEntity.ok(ApiResponse.ok(settings().getBranding()));
    }

    @PostMapping("/branding")
    @PreAuthorize("hasAuthority('TENANT:WRITE')")
    @Operation(summary = "Update the current tenant's branding")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateBranding(@RequestBody Map<String, Object> branding) {
        TenantSettings s = settings();
        Map<String, Object> b = s.getBranding() == null ? new HashMap<>() : new HashMap<>(s.getBranding());
        b.putAll(branding);
        s.setBranding(b);
        s.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(repository.save(s).getBranding()));
    }

    @PostMapping("/branding/logo")
    @PreAuthorize("hasAuthority('TENANT:WRITE')")
    @Operation(summary = "Set the current tenant's logo URL")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> setLogo(@RequestBody Map<String, String> body) {
        TenantSettings s = settings();
        Map<String, Object> b = s.getBranding() == null ? new HashMap<>() : new HashMap<>(s.getBranding());
        b.put("logoUrl", body.get("logoUrl"));
        s.setBranding(b);
        s.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(repository.save(s).getBranding()));
    }

    @PostMapping("/flags/{key}")
    @PreAuthorize("hasAuthority('TENANT:WRITE')")
    @Operation(summary = "Toggle a feature flag for the current tenant")
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> setFlag(
            @PathVariable String key, @RequestBody(required = false) Map<String, Object> body) {
        TenantSettings s = settings();
        Map<String, Object> flags = s.getFeatureFlags() == null ? new HashMap<>() : new HashMap<>(s.getFeatureFlags());
        Object enabled = body == null ? Boolean.TRUE : body.getOrDefault("enabled", Boolean.TRUE);
        flags.put(key, enabled);
        s.setFeatureFlags(flags);
        s.setUpdatedBy(currentUserId());
        return ResponseEntity.ok(ApiResponse.ok(repository.save(s).getFeatureFlags()));
    }

    @Transactional
    protected TenantSettings settings() {
        String tenantId = TenantContext.get();
        return repository.findByTenantId(tenantId).orElseGet(() -> {
            TenantSettings s = new TenantSettings();
            s.setTenantId(tenantId);
            s.setBranding(new HashMap<>());
            s.setFeatureFlags(new HashMap<>());
            s.setCreatedBy(currentUserId());
            return repository.save(s);
        });
    }

    private String currentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication() == null ? null
                : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof UserPrincipal up ? up.getId() : null;
    }
}
