package com.hrms.security.sso;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSO provider configuration. Drives auto-configuration of Spring Security's
 * OAuth2 client (Google / Microsoft / Okta) and SAML2 relying-party registrations.
 * All values read from .env — see CONFIGURATION.md.
 */
@Getter @Setter
@Component
@ConfigurationProperties(prefix = "hrms.sso")
public class SsoProviderConfig {

    /** Master switch — when false, only username/password + JWT used. */
    private boolean enabled = false;

    /** Auto-create users on first SSO login if not present. */
    private boolean justInTimeProvisioning = true;

    /** Default role granted to new users created via SSO/SCIM. */
    private String defaultRoleCode = "USER";

    /** Default tenant assigned to new users when the IdP does not pass tenant claim. */
    private String defaultTenantId = "default";

    /** SAML signing key + cert (PEM, base64-encoded contents in env). */
    private String samlPrivateKeyPem;
    private String samlCertificatePem;

    /** SAML IdP metadata URL (per-tenant — switch to a map<tenant, url> for true multi-tenancy). */
    private String samlIdpMetadataUrl;

    /** OIDC provider issuer URIs (auto-discovered via .well-known/openid-configuration). */
    private String oidcGoogleIssuer = "https://accounts.google.com";
    private String oidcMicrosoftIssuer = "https://login.microsoftonline.com/common/v2.0";
    private String oidcOktaIssuer;

    /** SCIM bearer token — clients (Okta / Azure AD) authenticate with this when provisioning. */
    private String scimBearerToken;
}
