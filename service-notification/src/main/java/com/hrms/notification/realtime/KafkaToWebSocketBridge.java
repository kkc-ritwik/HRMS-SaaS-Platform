package com.hrms.notification.realtime;

import com.hrms.events.model.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fan-out: subscribes to the {@code hrms.notification} Kafka topic and pushes each event
 * to the targeted STOMP destination.
 *
 *  · Per-user toast       → /user/{userId}/queue/notifications
 *  · Per-tenant broadcast → /topic/tenant/{tenantId}/announcements
 *  · Manager-of dashboard → /user/{managerId}/queue/team-events
 *
 * Topic naming convention is read from {@code event.payload.target}; absence routes to
 * /topic/tenant/{tenantId}/general by default.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaToWebSocketBridge {

    private final SimpMessagingTemplate stomp;

    @KafkaListener(topics = {"hrms.notification", "hrms.engagement", "hrms.payroll"},
            groupId = "notification-ws-bridge",
            containerFactory = "kafkaListenerContainerFactory")
    public void bridge(DomainEvent event) {
        try {
            Map<String, Object> payload = event.getPayload();
            String target = payload == null ? null : (String) payload.get("targetUserId");
            String tenant = event.getTenantId() == null ? "_" : event.getTenantId();

            Map<String, Object> envelope = Map.of(
                    "type", event.getEventType(),
                    "tenantId", tenant,
                    "occurredAt", event.getOccurredAt() == null ? "" : event.getOccurredAt().toString(),
                    "payload", payload == null ? Map.of() : payload
            );

            if (target != null) {
                stomp.convertAndSendToUser(target, "/queue/notifications", envelope);
                log.debug("WS push to user {} ← {}", target, event.getEventType());
            } else {
                stomp.convertAndSend("/topic/tenant/" + tenant + "/general", envelope);
            }
        } catch (Exception e) {
            log.warn("Failed to bridge event {} to WebSocket: {}", event.getEventType(), e.getMessage());
        }
    }

}
