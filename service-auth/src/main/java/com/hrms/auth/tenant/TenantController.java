package com.hrms.auth.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantOnboardingService svc;

    @PostMapping
    public TenantOnboardingService.TenantCreated provision(@RequestBody TenantOnboardingService.NewTenantRequest req) {
        return svc.provisionTenant(req);
    }
}
