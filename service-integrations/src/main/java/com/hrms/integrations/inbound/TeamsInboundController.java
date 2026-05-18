package com.hrms.integrations.inbound;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Inbound endpoint for Microsoft Teams bot messages / Adaptive Card actions.
 * Teams POSTs an Activity JSON; we acknowledge immediately and publish an event for
 * downstream services to handle (leave apply, balance check, etc.).
 *
 * Bot Framework signature validation is best done via Bot Framework SDK — for now we
 * trust the bot's caller is in the same VNet / API-Manager.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/integrations/teams/inbound")
@RequiredArgsConstructor
public class TeamsInboundController {

    private final EventPublisher events;

    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> message(@RequestBody Map<String, Object> activity) {
        String type = String.valueOf(activity.getOrDefault("type", "message"));
        Object id = activity.get("id");
        events.publishDirect(Topics.SOCIAL, DomainEvent.of(
                "teams.activity", "integrations",
                String.valueOf(activity.getOrDefault("conversation", Map.of()).toString()),
                id == null ? null : id.toString(), "TeamsActivity",
                Map.of("type", type, "raw", activity)));
        return ResponseEntity.ok(Map.of(
                "type", "message",
                "text", "Received: " + type));
    }
}
