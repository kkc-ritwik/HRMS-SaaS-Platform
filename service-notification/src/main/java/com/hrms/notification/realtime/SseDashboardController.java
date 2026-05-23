package com.hrms.notification.realtime;

import com.hrms.security.model.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Server-Sent Events for live dashboards (HR ops monitoring, payroll progress, attendance,
 * helpdesk SLA, recruitment funnel).
 *
 *   GET /api/notifications/sse/dashboard?channel=payroll-run-{runId}
 *
 * Clients connect with EventSource(); the server pushes JSON events on the same channel.
 * Producers call {@link #publish(String, String, Object)} from anywhere in the JVM —
 * typically from Kafka consumers that route by channel.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/sse")
public class SseDashboardController {

    private final ConcurrentMap<String, ConcurrentMap<UUID, SseEmitter>> channels = new ConcurrentHashMap<>();

    @GetMapping(value = "/dashboard", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestParam String channel,
                                @RequestParam(required = false) Long lastEventId) {
        String key = TenantContext.get() + ":" + channel;
        SseEmitter emitter = new SseEmitter(0L);           // no timeout
        UUID id = UUID.randomUUID();

        channels.computeIfAbsent(key, k -> new ConcurrentHashMap<>()).put(id, emitter);

        emitter.onCompletion(() -> remove(key, id));
        emitter.onTimeout(()    -> remove(key, id));
        emitter.onError(t       -> remove(key, id));

        // Send a hello frame so the client knows it's connected
        try {
            emitter.send(SseEmitter.event()
                    .name("hello")
                    .data(Map.of("channel", channel, "subscriberId", id.toString(),
                            "serverTime", OffsetDateTime.now().toString())));
        } catch (IOException ignored) {}

        return emitter;
    }

    /** Push an event to every subscriber on the given (tenant-scoped) channel. */
    public void publish(String channel, String eventName, Object data) {
        String key = TenantContext.get() + ":" + channel;
        ConcurrentMap<UUID, SseEmitter> subs = channels.get(key);
        if (subs == null || subs.isEmpty()) return;
        subs.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.debug("SSE subscriber {} dropped: {}", id, e.getMessage());
                remove(key, id);
            }
        });
    }

    private void remove(String key, UUID id) {
        ConcurrentMap<UUID, SseEmitter> subs = channels.get(key);
        if (subs != null) {
            subs.remove(id);
            if (subs.isEmpty()) channels.remove(key);
        }
    }

    /** Heartbeat every 30s to keep proxies / load balancers from closing idle connections. */
    @Scheduled(fixedDelay = 30_000L)
    public void heartbeat() {
        channels.forEach((key, subs) -> subs.forEach((id, emitter) -> {
            try { emitter.send(SseEmitter.event().name("ping").data("")); }
            catch (IOException e) { remove(key, id); }
        }));
    }
}
