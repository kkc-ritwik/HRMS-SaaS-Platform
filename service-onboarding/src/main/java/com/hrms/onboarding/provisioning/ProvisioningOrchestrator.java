package com.hrms.onboarding.provisioning;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Picks the right IdentityProvisioner for the configured IT platform and orchestrates
 * day-0 provisioning. Called from OfferAcceptedListener after the pre-onboarding portal
 * is created (so we have employee details to use).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProvisioningOrchestrator {

    @Value("${hrms.provisioning.default-platform:GOOGLE_WORKSPACE}") private String defaultPlatform;

    private final List<IdentityProvisioner> provisioners;

    public String provisionForEmployee(IdentityProvisioner.NewIdentity identity) {
        return provision(identity, defaultPlatform);
    }

    public String provision(IdentityProvisioner.NewIdentity identity, String platform) {
        IdentityProvisioner p = pick(platform);
        String externalId = p.provision(identity);
        log.info("Provisioned identity for {} on {} → {}", identity.workEmail(), platform, externalId);
        return externalId;
    }

    public void deactivateForOffboarding(String platform, String externalId) {
        pick(platform).deactivate(externalId);
        log.info("Deactivated {} on {}", externalId, platform);
    }

    public String resetPassword(String platform, String externalId) {
        return pick(platform).resetPassword(externalId);
    }

    private IdentityProvisioner pick(String platform) {
        String target = platform == null ? defaultPlatform : platform;
        return provisioners.stream().filter(p -> p.supports(target)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No IdentityProvisioner for platform " + target));
    }

    /** Helper to derive provisioning input from an employee record (used by listeners). */
    public IdentityProvisioner.NewIdentity newIdentityFor(UUID employeeId, String first, String last,
                                                          String workEmail, String dept, String designation,
                                                          String managerEmail) {
        String tempPassword = UUID.randomUUID().toString().substring(0, 12) + "!Aa";
        return new IdentityProvisioner.NewIdentity(first, last, workEmail, tempPassword,
                dept, designation, managerEmail, "/");
    }
}
