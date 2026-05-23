package com.hrms.corehr.immigration;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Nightly scan of travel_documents whose expiry falls inside the alert windows.
 * Publishes hrms.employee/document.expiring events. Notification + manager-chain handled
 * by listeners (service-notification turns events into emails / Slack DMs).
 *
 * Default alert windows: 90, 60, 30, 14, 7, 1 days before expiry — each fires once
 * (de-duped via the events.publish outbox).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiryAlertScheduler {

    public interface DocRepo extends JpaRepository<TravelDocument, java.util.UUID> {
        @Query("SELECT d FROM TravelDocument d WHERE d.active = true AND d.deleted = false " +
                "AND d.expiryDate BETWEEN :from AND :to")
        List<TravelDocument> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
    }

    private static final int[] ALERT_DAYS = {90, 60, 30, 14, 7, 1};

    private final DocRepo docs;
    private final ObjectProvider<EventPublisher> eventsProvider;

    /** Runs daily at 08:00. */
    @Scheduled(cron = "${hrms.corehr.expiry-alerts.cron:0 0 8 * * *}")
    public void scan() {
        EventPublisher publisher = eventsProvider.getIfAvailable();
        if (publisher == null) { log.debug("EventPublisher unavailable — expiry scan skipped"); return; }

        LocalDate today = LocalDate.now();
        int total = 0;
        for (int days : ALERT_DAYS) {
            LocalDate target = today.plusDays(days);
            List<TravelDocument> due = docs.findExpiringBetween(target, target);
            for (TravelDocument d : due) {
                publisher.publishDirect(Topics.EMPLOYEE, DomainEvent.of(
                        "document.expiring", "core-hr",
                        d.getTenantId(), d.getId().toString(), "TravelDocument",
                        Map.of(
                            "documentType", d.getDocumentType(),
                            "employeeId", d.getEmployeeId(),
                            "expiryDate", d.getExpiryDate().toString(),
                            "daysRemaining", days,
                            "destinationCountry", d.getDestinationCountry() == null ? "" : d.getDestinationCountry())));
                total++;
            }
        }
        if (total > 0) log.info("Expiry alerts fired: {} documents", total);
    }
}
