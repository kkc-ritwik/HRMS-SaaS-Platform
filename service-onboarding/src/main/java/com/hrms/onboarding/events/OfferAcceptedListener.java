package com.hrms.onboarding.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.onboarding.preboard.PreOnboardingService;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Bridges recruitment → onboarding: when a candidate accepts an offer, automatically:
 *   1. Create their pre-onboarding portal + email them an invitation
 *   2. (Later) seed onboarding tasks from the template matching their role/department
 *   3. (Later) emit asset-request stubs (laptop, badge, etc.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfferAcceptedListener {

    private final PreOnboardingService preboardSvc;
    private final ObjectMapper mapper;

    @KafkaListener(topics = Topics.RECRUITMENT, groupId = "${spring.kafka.consumer.group-id:service-onboarding}")
    public void onRecruitmentEvent(String payload) {
        try {
            DomainEvent ev = mapper.readValue(payload, DomainEvent.class);
            if (!"recruitment.offer.accepted".equals(ev.getEventType())) return;

            Map<String, Object> p = ev.getPayload() == null ? Map.of() : ev.getPayload();
            UUID candidateId = UUID.fromString(str(p, "candidateId"));
            UUID offerId = UUID.fromString(ev.getAggregateId());
            String name = str(p, "candidateName");
            String email = str(p, "candidateEmail");
            String joinDate = str(p, "proposedJoinDate");
            LocalDate join = (joinDate == null || joinDate.isBlank()) ? null : LocalDate.parse(joinDate);

            // The Kafka listener runs without an HTTP request → set the tenant manually
            TenantContext.set(ev.getTenantId());
            try {
                preboardSvc.createForCandidate(candidateId, offerId, name, email, join);
                log.info("Pre-onboarding portal auto-created for candidate {}", candidateId);
            } finally {
                TenantContext.clear();
            }
        } catch (Exception e) {
            log.error("Failed to handle recruitment event: {}", e.getMessage(), e);
        }
    }

    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v == null ? null : v.toString();
    }
}
