package com.hrms.recruitment.events;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes recruitment.offer.accepted on offer acceptance. Listened to by service-onboarding
 * (creates onboarding workflow), service-core-hr (creates Employee shell record), and
 * service-notification (sends welcome email).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OfferAcceptedPublisher {

    private final EventPublisher events;

    public void publish(String tenantId, UUID offerId, UUID candidateId,
                        UUID jobRequisitionId, String candidateName, String candidateEmail,
                        String offeredPosition, LocalDate proposedJoinDate) {
        events.publish(Topics.RECRUITMENT, DomainEvent.of(
                "recruitment.offer.accepted", "recruitment", tenantId,
                offerId.toString(), "OfferLetter",
                Map.of(
                        "candidateId", candidateId.toString(),
                        "jobRequisitionId", jobRequisitionId.toString(),
                        "candidateName", candidateName == null ? "" : candidateName,
                        "candidateEmail", candidateEmail == null ? "" : candidateEmail,
                        "offeredPosition", offeredPosition == null ? "" : offeredPosition,
                        "proposedJoinDate", proposedJoinDate == null ? "" : proposedJoinDate.toString())));
        log.info("Published offer.accepted for offer {}", offerId);
    }
}
