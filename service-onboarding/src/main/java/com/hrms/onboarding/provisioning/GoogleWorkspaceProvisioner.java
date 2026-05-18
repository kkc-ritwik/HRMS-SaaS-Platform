package com.hrms.onboarding.provisioning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Google Workspace user provisioning via Admin SDK Directory API.
 * Service account with domain-wide delegation required (env: hrms.provisioning.google.*).
 */
@Slf4j
@Component
public class GoogleWorkspaceProvisioner implements IdentityProvisioner {

    @Value("${hrms.provisioning.google.service-account-path:}") private String serviceAccountPath;
    @Value("${hrms.provisioning.google.impersonate-admin:}") private String impersonateAdmin;
    @Value("${hrms.provisioning.google.domain:}") private String domain;

    private final RestTemplate rest = new RestTemplate();

    @Override public boolean supports(String platform) { return "GOOGLE_WORKSPACE".equalsIgnoreCase(platform); }

    @Override
    public String provision(NewIdentity id) {
        if (notConfigured()) {
            log.warn("Google Workspace not configured — logging request: {}", id.workEmail());
            return "dev-google-" + UUID.randomUUID();
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "primaryEmail", id.workEmail(),
                    "name", Map.of("givenName", id.firstName(), "familyName", id.lastName()),
                    "password", id.temporaryPassword(),
                    "changePasswordAtNextLogin", true,
                    "organizations", java.util.List.of(Map.of(
                            "title", id.designation() == null ? "" : id.designation(),
                            "department", id.department() == null ? "" : id.department(),
                            "primary", true)),
                    "orgUnitPath", id.orgUnit() == null ? "/" : id.orgUnit());
            ResponseEntity<Map> resp = rest.exchange(
                    "https://admin.googleapis.com/admin/directory/v1/users",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Object gid = resp.getBody() == null ? null : resp.getBody().get("id");
            return gid == null ? null : gid.toString();
        } catch (Exception e) {
            log.error("Google Workspace provision failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void deactivate(String externalId) {
        if (notConfigured() || externalId == null) return;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange("https://admin.googleapis.com/admin/directory/v1/users/" + externalId,
                    HttpMethod.PUT, new HttpEntity<>(Map.of("suspended", true), h), String.class);
        } catch (Exception e) {
            log.warn("Google Workspace deactivate failed: {}", e.getMessage());
        }
    }

    @Override
    public void addToGroup(String externalId, String groupId) {
        if (notConfigured() || externalId == null) return;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange("https://admin.googleapis.com/admin/directory/v1/groups/" + groupId + "/members",
                    HttpMethod.POST, new HttpEntity<>(Map.of("email", externalId, "role", "MEMBER"), h), String.class);
        } catch (Exception e) {
            log.warn("Google addToGroup failed: {}", e.getMessage());
        }
    }

    @Override
    public String resetPassword(String externalId) {
        String temp = UUID.randomUUID().toString().substring(0, 12) + "!Aa";
        if (notConfigured() || externalId == null) return temp;
        try {
            HttpHeaders h = new HttpHeaders(); h.setBearerAuth(obtainAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            rest.exchange("https://admin.googleapis.com/admin/directory/v1/users/" + externalId,
                    HttpMethod.PUT, new HttpEntity<>(Map.of(
                            "password", temp, "changePasswordAtNextLogin", true), h), String.class);
        } catch (Exception e) {
            log.warn("Google resetPassword failed: {}", e.getMessage());
        }
        return temp;
    }

    private boolean notConfigured() {
        return serviceAccountPath == null || serviceAccountPath.isBlank() || domain == null || domain.isBlank();
    }

    /** Real impl: load service account JSON, sign JWT, exchange for OAuth access token impersonating impersonateAdmin. */
    private String obtainAccessToken() { return "stub-google-token"; }
}
