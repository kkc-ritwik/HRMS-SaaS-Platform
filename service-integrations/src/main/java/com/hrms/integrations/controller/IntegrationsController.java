package com.hrms.integrations.controller;

import com.hrms.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Top-level integrations catalog for the Settings UI. Webhook subscriptions are managed under
 * {@code /api/v1/integrations/webhooks}; this endpoint exposes the available third-party
 * connectors and their connection state.
 */
@RestController
@RequestMapping("/api/v1/integrations")
@RequiredArgsConstructor
public class IntegrationsController {

    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> list() {
        List<Map<String, Object>> integrations = List.of(
                Map.of("key", "slack", "name", "Slack", "category", "Communication", "connected", false),
                Map.of("key", "teams", "name", "Microsoft Teams", "category", "Communication", "connected", false),
                Map.of("key", "webhooks", "name", "Outgoing Webhooks", "category", "Developer", "connected", true),
                Map.of("key", "google", "name", "Google Workspace", "category", "Identity", "connected", false),
                Map.of("key", "azuread", "name", "Azure AD", "category", "Identity", "connected", false)
        );
        return ResponseEntity.ok(ApiResponse.ok(integrations));
    }
}
