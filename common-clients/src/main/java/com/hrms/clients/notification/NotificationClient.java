package com.hrms.clients.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "service-notification", path = "/api/v1/notifications",
        fallbackFactory = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/dispatch")
    void dispatch(@RequestBody NotificationDispatchRequest req);

    record NotificationDispatchRequest(
            String tenantId,
            List<String> recipientUserIds,
            String templateCode,
            String channel,            // EMAIL, SMS, PUSH, IN_APP, SLACK, TEAMS, WHATSAPP
            String subject,
            Map<String, Object> variables,
            String referenceType,
            String referenceId,
            String priority            // LOW, NORMAL, HIGH, URGENT
    ) {}
}
