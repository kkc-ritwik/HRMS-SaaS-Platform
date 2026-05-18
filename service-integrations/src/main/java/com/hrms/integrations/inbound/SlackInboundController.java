package com.hrms.integrations.inbound;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

/**
 * Inbound endpoint for Slack slash commands + interactivity / events. Slack POSTs here
 * with a signed payload; we verify HMAC and dispatch.
 *
 * Supported slash commands:
 *   /leave apply 2024-06-01..2024-06-03 sick — quick apply
 *   /leave balance — show my balance
 *   /attendance in / out — quick punch
 *   /kudos @userId great job on shipping X — quick kudos
 * Implementation detail handled by listeners of the published events.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/integrations/slack/inbound")
@RequiredArgsConstructor
public class SlackInboundController {

    @Value("${hrms.notification.slack.signing-secret:}") private String signingSecret;

    private final EventPublisher events;

    @PostMapping(value = "/commands", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Map<String, Object>> slashCommand(
            @RequestHeader(value = "X-Slack-Request-Timestamp", required = false) String ts,
            @RequestHeader(value = "X-Slack-Signature", required = false) String sig,
            @RequestBody String rawBody,
            @RequestParam Map<String, String> form) {

        if (!verify(ts, sig, rawBody)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("text", "Invalid request signature"));
        }
        String command = form.getOrDefault("command", "").replace("/", "");
        String text = form.getOrDefault("text", "");
        String userId = form.getOrDefault("user_id", "");
        String teamId = form.getOrDefault("team_id", "");

        events.publishDirect(Topics.SOCIAL, DomainEvent.of(
                "slack.slash_command", "integrations", teamId, userId, "SlackSlashCommand",
                Map.of("command", command, "text", text, "slackUserId", userId, "teamId", teamId)));

        return ResponseEntity.ok(Map.of(
                "response_type", "ephemeral",
                "text", "Your request was received: `/" + command + " " + text + "`"));
    }

    /** Slack event subscriptions (URL verification + events). */
    @PostMapping("/events")
    public ResponseEntity<Map<String, Object>> events(
            @RequestHeader(value = "X-Slack-Request-Timestamp", required = false) String ts,
            @RequestHeader(value = "X-Slack-Signature", required = false) String sig,
            @RequestBody String rawBody,
            @RequestBody(required = false) Map<String, Object> body) {

        if (body != null && "url_verification".equals(body.get("type"))) {
            return ResponseEntity.ok(Map.of("challenge", body.get("challenge")));
        }
        if (!verify(ts, sig, rawBody)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        events.publishDirect(Topics.SOCIAL, DomainEvent.of(
                "slack.event", "integrations", null,
                body == null ? null : String.valueOf(body.get("event_id")),
                "SlackEvent", body == null ? Map.of() : body));
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private boolean verify(String ts, String sig, String body) {
        if (signingSecret == null || signingSecret.isBlank() || signingSecret.startsWith("REPLACE")) {
            log.warn("Slack signing secret not configured — accepting unverified request (dev only)");
            return true;
        }
        if (ts == null || sig == null) return false;
        try {
            String baseString = "v0:" + ts + ":" + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = "v0=" + HexFormat.of().formatHex(mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8)));
            return computed.equalsIgnoreCase(sig);
        } catch (Exception e) {
            log.warn("Slack signature verification failed: {}", e.getMessage());
            return false;
        }
    }
}
