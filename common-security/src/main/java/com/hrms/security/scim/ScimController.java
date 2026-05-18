package com.hrms.security.scim;

import com.hrms.security.sso.SsoProviderConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * SCIM 2.0 (RFC 7644) provisioning endpoint — lets Okta / Azure AD / Google Workspace push
 * user lifecycle (create / update / suspend / delete) into HRMS.
 *
 * Authentication: Bearer token (hrms.sso.scim-bearer-token). Path: /scim/v2/Users.
 * Backed by ScimUserAdapter implemented in service-auth (so the User table stays there).
 */
@RestController
@RequestMapping("/scim/v2")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "hrms.sso.scim-bearer-token")
public class ScimController {

    private final SsoProviderConfig cfg;
    private final ObjectProvider<ScimUserAdapter> adapterProvider;

    private ScimUserAdapter adapter() {
        ScimUserAdapter a = adapterProvider.getIfAvailable();
        if (a == null) throw new IllegalStateException("ScimUserAdapter not configured");
        return a;
    }

    private void auth(String authHeader) {
        String expected = "Bearer " + cfg.getScimBearerToken();
        if (authHeader == null || !authHeader.equals(expected)) {
            throw new ScimException(401, "Invalid bearer token");
        }
    }

    @GetMapping("/ServiceProviderConfig")
    public Map<String, Object> serviceProviderConfig() {
        return Map.of(
                "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
                "patch", Map.of("supported", true),
                "bulk", Map.of("supported", false),
                "filter", Map.of("supported", true, "maxResults", 200),
                "changePassword", Map.of("supported", false),
                "sort", Map.of("supported", true),
                "etag", Map.of("supported", false),
                "authenticationSchemes", List.of(
                        Map.of("type", "oauthbearertoken", "name", "OAuth Bearer Token", "primary", true)));
    }

    @GetMapping("/Users")
    public Map<String, Object> listUsers(@RequestHeader(value = "Authorization", required = false) String authz,
                                          @RequestParam(required = false) String filter,
                                          @RequestParam(defaultValue = "1") int startIndex,
                                          @RequestParam(defaultValue = "100") int count) {
        auth(authz);
        List<Map<String, Object>> resources = adapter().listUsers(filter, startIndex, count);
        return Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
                "totalResults", resources.size(),
                "startIndex", startIndex,
                "itemsPerPage", resources.size(),
                "Resources", resources);
    }

    @GetMapping("/Users/{id}")
    public Map<String, Object> getUser(@RequestHeader("Authorization") String authz, @PathVariable String id) {
        auth(authz);
        Map<String, Object> u = adapter().getUser(id);
        if (u == null) throw new ScimException(404, "User not found");
        return u;
    }

    @PostMapping("/Users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestHeader("Authorization") String authz,
                                                           @RequestBody Map<String, Object> body) {
        auth(authz);
        Map<String, Object> created = adapter().createUser(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/Users/{id}")
    public Map<String, Object> replaceUser(@RequestHeader("Authorization") String authz,
                                            @PathVariable String id,
                                            @RequestBody Map<String, Object> body) {
        auth(authz); return adapter().replaceUser(id, body);
    }

    @PatchMapping("/Users/{id}")
    public Map<String, Object> patchUser(@RequestHeader("Authorization") String authz,
                                          @PathVariable String id,
                                          @RequestBody Map<String, Object> body) {
        auth(authz); return adapter().patchUser(id, body);
    }

    @DeleteMapping("/Users/{id}")
    public ResponseEntity<Void> deleteUser(@RequestHeader("Authorization") String authz, @PathVariable String id) {
        auth(authz); adapter().deactivateUser(id); return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ScimException.class)
    public ResponseEntity<Map<String, Object>> handle(ScimException e) {
        return ResponseEntity.status(e.status).body(Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:Error"),
                "status", String.valueOf(e.status),
                "detail", e.getMessage(),
                "_meta", Map.of("at", OffsetDateTime.now().toString())));
    }

    public static class ScimException extends RuntimeException {
        final int status;
        public ScimException(int status, String message) { super(message); this.status = status; }
    }
}
