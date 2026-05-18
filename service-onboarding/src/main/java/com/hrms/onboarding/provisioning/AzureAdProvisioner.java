package com.hrms.onboarding.provisioning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/** Azure AD / Entra ID user provisioning via Microsoft Graph. */
@Slf4j
@Component
public class AzureAdProvisioner implements IdentityProvisioner {

    @Value("${hrms.provisioning.azure.tenant-id:}") private String azureTenantId;
    @Value("${hrms.provisioning.azure.client-id:}") private String azureClientId;
    @Value("${hrms.provisioning.azure.client-secret:}") private String azureClientSecret;
    @Value("${hrms.provisioning.azure.domain:}") private String userPrincipalDomain;

    private final RestTemplate rest = new RestTemplate();

    @Override public boolean supports(String platform) { return "AZURE_AD".equalsIgnoreCase(platform); }

    @Override
    public String provision(NewIdentity id) {
        if (notConfigured()) {
            log.warn("Azure AD not configured — logging request: {}", id.workEmail());
            return "dev-azure-" + UUID.randomUUID();
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "accountEnabled", true,
                    "displayName", id.firstName() + " " + id.lastName(),
                    "mailNickname", id.workEmail().split("@")[0],
                    "userPrincipalName", id.workEmail(),
                    "passwordProfile", Map.of(
                            "forceChangePasswordNextSignIn", true,
                            "password", id.temporaryPassword()),
                    "givenName", id.firstName(),
                    "surname", id.lastName(),
                    "department", id.department() == null ? "" : id.department(),
                    "jobTitle", id.designation() == null ? "" : id.designation());
            ResponseEntity<Map> resp = rest.exchange(
                    "https://graph.microsoft.com/v1.0/users",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Object oid = resp.getBody() == null ? null : resp.getBody().get("id");
            return oid == null ? null : oid.toString();
        } catch (Exception e) {
            log.error("Azure AD provision failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void deactivate(String externalId) {
        if (notConfigured() || externalId == null) return;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange("https://graph.microsoft.com/v1.0/users/" + externalId,
                    HttpMethod.PATCH, new HttpEntity<>(Map.of("accountEnabled", false), h), String.class);
        } catch (Exception e) {
            log.warn("Azure AD deactivate failed: {}", e.getMessage());
        }
    }

    @Override
    public void addToGroup(String externalId, String groupId) {
        if (notConfigured() || externalId == null) return;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange("https://graph.microsoft.com/v1.0/groups/" + groupId + "/members/$ref",
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of("@odata.id",
                            "https://graph.microsoft.com/v1.0/directoryObjects/" + externalId), h),
                    String.class);
        } catch (Exception e) {
            log.warn("Azure addToGroup failed: {}", e.getMessage());
        }
    }

    @Override
    public String resetPassword(String externalId) {
        String temp = UUID.randomUUID().toString().substring(0, 12) + "!Aa";
        if (notConfigured() || externalId == null) return temp;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange("https://graph.microsoft.com/v1.0/users/" + externalId,
                    HttpMethod.PATCH,
                    new HttpEntity<>(Map.of("passwordProfile",
                            Map.of("forceChangePasswordNextSignIn", true, "password", temp)), h),
                    String.class);
        } catch (Exception e) {
            log.warn("Azure resetPassword failed: {}", e.getMessage());
        }
        return temp;
    }

    private boolean notConfigured() {
        return azureTenantId == null || azureTenantId.isBlank() || azureClientId == null || azureClientId.isBlank();
    }

    /** Real impl: client credentials grant at https://login.microsoftonline.com/{tenant}/oauth2/v2.0/token */
    private String obtainAccessToken() { return "stub-azure-token"; }
}
