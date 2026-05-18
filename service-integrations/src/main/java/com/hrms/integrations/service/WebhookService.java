package com.hrms.integrations.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.events.model.DomainEvent;
import com.hrms.integrations.entity.WebhookDelivery;
import com.hrms.integrations.entity.WebhookSubscription;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    public interface SubRepo extends JpaRepository<WebhookSubscription, UUID> {
        List<WebhookSubscription> findByTenantIdAndIsActiveTrue(String tenantId);
    }
    public interface DeliveryRepo extends JpaRepository<WebhookDelivery, UUID> {
        List<WebhookDelivery> findTop100ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(
                WebhookDelivery.Status s, OffsetDateTime cutoff);
    }

    private final SubRepo subs;
    private final DeliveryRepo deliveries;
    private final ObjectMapper mapper;
    private final RestTemplate rest = new RestTemplate();

    // ── Subscribe ────────────────────────────────────────────────────────────
    @Transactional
    public WebhookSubscription subscribe(WebhookSubscription s) {
        s.setTenantId(TenantContext.get());
        if (s.getSecret() == null || s.getSecret().isBlank())
            s.setSecret(UUID.randomUUID().toString().replace("-", ""));
        return subs.save(s);
    }

    public List<WebhookSubscription> list() { return subs.findByTenantIdAndIsActiveTrue(TenantContext.get()); }

    // ── Fan-out on every domain event from any topic ─────────────────────────
    @KafkaListener(topicPattern = "hrms\\..*",
            groupId = "${spring.kafka.consumer.group-id:service-integrations}")
    public void onEvent(String payload) {
        try {
            DomainEvent ev = mapper.readValue(payload, DomainEvent.class);
            List<WebhookSubscription> matches = subs.findByTenantIdAndIsActiveTrue(
                    ev.getTenantId() == null ? "" : ev.getTenantId());
            for (WebhookSubscription s : matches) {
                if (s.getEventTypes() == null
                        || s.getEventTypes().contains("*")
                        || s.getEventTypes().contains(ev.getEventType())) {
                    deliveries.save(WebhookDelivery.builder()
                            .tenantId(s.getTenantId())
                            .subscriptionId(s.getId())
                            .eventId(ev.getEventId())
                            .eventType(ev.getEventType())
                            .payload(payload)
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Webhook fan-out failed: {}", e.getMessage());
        }
    }

    // ── Delivery worker with exponential backoff ─────────────────────────────
    @Scheduled(fixedDelayString = "${hrms.integrations.delivery.poll-ms:5000}")
    @Transactional
    public void deliverPending() {
        for (WebhookDelivery d : deliveries.findTop100ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(
                WebhookDelivery.Status.PENDING, OffsetDateTime.now())) {
            WebhookSubscription sub = subs.findById(d.getSubscriptionId()).orElse(null);
            if (sub == null || !Boolean.TRUE.equals(sub.getIsActive())) {
                d.setStatus(WebhookDelivery.Status.ABANDONED);
                deliveries.save(d);
                continue;
            }
            try {
                HttpHeaders h = new HttpHeaders();
                h.setContentType(MediaType.APPLICATION_JSON);
                h.set("X-Hrms-Event-Id", d.getEventId());
                h.set("X-Hrms-Event-Type", d.getEventType());
                h.set("X-Hrms-Signature", hmac(sub.getSecret(), d.getPayload()));
                ResponseEntity<String> resp = rest.postForEntity(sub.getTargetUrl(),
                        new HttpEntity<>(d.getPayload(), h), String.class);
                d.setLastResponseCode(resp.getStatusCode().value());
                if (resp.getStatusCode().is2xxSuccessful()) {
                    d.setStatus(WebhookDelivery.Status.DELIVERED);
                    d.setDeliveredAt(OffsetDateTime.now());
                } else {
                    bumpRetry(d, sub, "HTTP " + resp.getStatusCode().value());
                }
            } catch (Exception e) {
                bumpRetry(d, sub, e.getMessage());
            }
            deliveries.save(d);
        }
    }

    private void bumpRetry(WebhookDelivery d, WebhookSubscription sub, String error) {
        d.setAttempts(d.getAttempts() + 1);
        d.setLastError(error);
        if (d.getAttempts() >= sub.getRetryMax()) {
            d.setStatus(WebhookDelivery.Status.ABANDONED);
        } else {
            long backoffSec = (long) Math.pow(2, d.getAttempts()) * 10L; // 20, 40, 80, 160…
            d.setNextAttemptAt(OffsetDateTime.now().plusSeconds(backoffSec));
        }
    }

    private String hmac(String secret, String body) {
        try {
            Mac m = Mac.getInstance("HmacSHA256");
            m.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + HexFormat.of().formatHex(m.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { return ""; }
    }
}
