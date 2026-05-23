package com.hrms.lms.renewal;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.lms.entity.Certification;
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
 * Daily scan of certifications nearing expiry. Fires
 * hrms.training/certification.expiring at 180, 90, 60, 30, 14, 7, 1 days ahead.
 * Listeners (service-notification) turn events into emails / Slack DMs and add
 * a "Renew your {{cert}}" item to the employee's learning queue.
 *
 * Also auto-flips ACTIVE → EXPIRED on the day the expiry falls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CertificationRenewalScheduler {

    public interface Repo extends JpaRepository<Certification, java.util.UUID> {
        @Query("SELECT c FROM Certification c WHERE c.status = 'ACTIVE' AND c.deleted = false " +
                "AND c.expiryDate = :target")
        List<Certification> dueOn(@Param("target") LocalDate target);

        @Query("SELECT c FROM Certification c WHERE c.status = 'ACTIVE' AND c.deleted = false " +
                "AND c.expiryDate < :today")
        List<Certification> alreadyExpired(@Param("today") LocalDate today);
    }

    private static final int[] ALERT_DAYS = {180, 90, 60, 30, 14, 7, 1};

    private final Repo repo;
    private final ObjectProvider<EventPublisher> eventsProvider;

    @Scheduled(cron = "${hrms.lms.cert-renewal.cron:0 30 7 * * *}")
    public void scan() {
        EventPublisher publisher = eventsProvider.getIfAvailable();
        if (publisher == null) {
            log.debug("EventPublisher unavailable — cert renewal scan skipped");
            return;
        }

        LocalDate today = LocalDate.now();
        int alertsFired = 0;
        for (int days : ALERT_DAYS) {
            LocalDate target = today.plusDays(days);
            List<Certification> due = repo.dueOn(target);
            for (Certification c : due) {
                publisher.publishDirect(Topics.LMS, DomainEvent.of(
                        "certification.expiring", "lms",
                        c.getTenantId(), c.getId().toString(), "Certification",
                        Map.of(
                                "employeeId", c.getEmployeeId(),
                                "certificateName", c.getCertificateName(),
                                "issuedBy", c.getIssuedBy() == null ? "" : c.getIssuedBy(),
                                "expiryDate", c.getExpiryDate().toString(),
                                "daysRemaining", days)));
                alertsFired++;
            }
        }

        // Hard-expire any active certifications whose date has passed
        List<Certification> expired = repo.alreadyExpired(today);
        for (Certification c : expired) {
            c.setStatus(Certification.CertificationStatus.EXPIRED);
            repo.save(c);
            publisher.publishDirect(Topics.LMS, DomainEvent.of(
                    "certification.expired", "lms",
                    c.getTenantId(), c.getId().toString(), "Certification",
                    Map.of(
                            "employeeId", c.getEmployeeId(),
                            "certificateName", c.getCertificateName(),
                            "expiryDate", c.getExpiryDate().toString())));
        }

        if (alertsFired + expired.size() > 0) {
            log.info("Certification renewal scan — {} alerts, {} auto-expired",
                    alertsFired, expired.size());
        }
    }
}
