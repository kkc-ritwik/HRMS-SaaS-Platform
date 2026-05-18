package com.hrms.security.sso;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Handles successful SSO authentication callbacks (OIDC code-flow returns or SAML
 * AuthnResponse processed). Resolves/creates the local User record (just-in-time
 * provisioning) and issues a JWT.
 *
 * Backed by a SsoUserResolver contract — service-auth provides the concrete implementation
 * that hits the users table; other services don't need it.
 */
@Slf4j
@Service
public class SsoAuthenticationCallbackHandler {

    private final SsoProviderConfig config;
    private final ObjectProvider<SsoUserResolver> resolverProvider;

    public SsoAuthenticationCallbackHandler(SsoProviderConfig config,
                                             ObjectProvider<SsoUserResolver> resolverProvider) {
        this.config = config;
        this.resolverProvider = resolverProvider;
    }

    private SsoUserResolver resolver() {
        SsoUserResolver r = resolverProvider.getIfAvailable();
        if (r == null) throw new IllegalStateException(
                "SsoUserResolver bean not found — service-auth (or another service that owns the User table) must provide it");
        return r;
    }

    public SsoOutcome handleSuccessfulSso(SsoAssertion assertion) {
        if (!config.isEnabled()) {
            throw new IllegalStateException("SSO not enabled");
        }
        String tenant = assertion.getTenantId() != null
                ? assertion.getTenantId() : config.getDefaultTenantId();

        SsoUserResolver r = resolver();
        Object user = r.findByEmail(tenant, assertion.getEmail());
        if (user == null) {
            if (!config.isJustInTimeProvisioning()) {
                throw new IllegalStateException("User not found and JIT provisioning disabled");
            }
            user = r.create(tenant, assertion, Set.of(config.getDefaultRoleCode()));
            log.info("JIT-created user via SSO: {} (tenant={})", assertion.getEmail(), tenant);
        } else {
            r.updateFromAssertion(user, assertion);
        }
        String jwt = r.issueJwt(user);
        return new SsoOutcome(jwt, assertion.getEmail(), tenant);
    }

    public record SsoOutcome(String jwt, String email, String tenantId) {}

    /**
     * Implemented by service-auth (the only service with access to users + JwtService).
     * Other services that include common-security but don't need SSO won't instantiate this.
     */
    public interface SsoUserResolver {
        Object findByEmail(String tenantId, String email);
        Object create(String tenantId, SsoAssertion assertion, Set<String> roles);
        void updateFromAssertion(Object user, SsoAssertion assertion);
        String issueJwt(Object user);
    }

    @Data
    public static class SsoAssertion {
        private String email;
        private String firstName;
        private String lastName;
        private String tenantId;
        private String externalId;         // sub / NameID
        private String provider;           // GOOGLE / MICROSOFT / OKTA / SAML
        private Set<String> groupMemberships;
    }
}
