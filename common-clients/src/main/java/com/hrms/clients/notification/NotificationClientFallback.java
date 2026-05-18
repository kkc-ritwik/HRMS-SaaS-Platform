package com.hrms.clients.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallback implements FallbackFactory<NotificationClient> {
    @Override
    public NotificationClient create(Throwable cause) {
        log.warn("NotificationClient fallback: {}", cause.getMessage());
        return req -> log.warn("Notification dropped due to fallback: tmpl={} recipients={}",
                req.templateCode(), req.recipientUserIds());
    }
}
