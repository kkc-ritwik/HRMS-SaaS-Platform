package com.hrms.notification.dispatch.channels;

import com.hrms.notification.dispatch.DispatchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * WebSocket-based in-app push (STOMP via Spring). Frontend subscribes to
 * /topic/user/{userId}/notifications and receives JSON payloads in real time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppChannel {

    private final SimpMessagingTemplate ws;

    public void send(DispatchRequest req) {
        if (req.getToUserIds() == null) return;
        for (String userId : req.getToUserIds()) {
            try {
                ws.convertAndSend("/topic/user/" + userId + "/notifications", Map.of(
                        "subject", req.getSubject() == null ? "" : req.getSubject(),
                        "body", req.getBody() == null ? "" : req.getBody(),
                        "category", req.getCategory() == null ? "" : req.getCategory(),
                        "referenceType", req.getReferenceType() == null ? "" : req.getReferenceType(),
                        "referenceId", req.getReferenceId() == null ? "" : req.getReferenceId(),
                        "priority", req.getPriority() == null ? "NORMAL" : req.getPriority()));
            } catch (Exception e) {
                log.error("WebSocket push to user {} failed: {}", userId, e.getMessage());
            }
        }
    }
}
