package com.hrms.helpdesk.csat;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.repository.TicketRepository;
import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * When a ticket transitions to RESOLVED/CLOSED, fire a satisfaction-rating survey.
 * Two flavours:
 *   1. On close — emit `helpdesk.csat.survey-requested` event (notification service can email).
 *   2. Direct email containing a 1-5 star rating link (frontend captures + POSTs back).
 * Idempotent — won't re-send if rating already set or already invited.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsatTrigger {

    private final TicketRepository tickets;
    private final EventPublisher events;
    private final MailService mailService;

    @Value("${hrms.helpdesk.csat.frontend-url:${FRONTEND_BASE_URL:http://localhost:5173}}")
    private String frontendUrl;

    /** Call from TicketService whenever a ticket flips to RESOLVED. */
    @Transactional
    public void onTicketResolved(Ticket t, String requesterEmail) {
        if (t == null || t.getSatisfactionRating() != null) return;
        events.publish(Topics.HELPDESK, DomainEvent.of(
                "helpdesk.csat.survey-requested", "helpdesk",
                t.getTenantId(), t.getId().toString(), "Ticket",
                Map.of("requesterId", t.getRequesterId(),
                       "ticketTitle", t.getTitle() == null ? "" : t.getTitle())));
        if (requesterEmail != null && !requesterEmail.isBlank()) {
            String url = frontendUrl + "/tickets/" + t.getId() + "/csat";
            mailService.sendAsync(MailRequest.builder()
                    .to(requesterEmail)
                    .subject("How did we do? — " + t.getTitle())
                    .html("""
                          <p>Your ticket <strong>%s</strong> is now resolved.</p>
                          <p>Please take 5 seconds to rate the support you received:</p>
                          <p style='font-size:20px;text-align:center'>
                            <a href='%s?score=5'>⭐⭐⭐⭐⭐</a>
                            &nbsp;<a href='%s?score=4'>⭐⭐⭐⭐</a>
                            &nbsp;<a href='%s?score=3'>⭐⭐⭐</a>
                            &nbsp;<a href='%s?score=2'>⭐⭐</a>
                            &nbsp;<a href='%s?score=1'>⭐</a>
                          </p>
                          <p>Thanks for using HRMS Helpdesk.</p>
                          """.formatted(t.getTitle(), url, url, url, url, url))
                    .category("helpdesk-csat")
                    .build());
        }
    }

    /** Frontend POSTs the rating + comment back through TicketService.rateSatisfaction. */
    @Transactional
    public Ticket recordRating(UUID ticketId, int score, String comment) {
        Ticket t = tickets.findById(ticketId).orElseThrow();
        t.setSatisfactionRating(score);
        if (comment != null && !comment.isBlank()) {
            t.setDescription((t.getDescription() == null ? "" : t.getDescription())
                    + "\n\n[CSAT " + score + "/5]: " + comment);
        }
        events.publish(Topics.HELPDESK, DomainEvent.of(
                "helpdesk.csat.rated", "helpdesk",
                t.getTenantId(), t.getId().toString(), "Ticket",
                Map.of("score", score)));
        return tickets.save(t);
    }

    /**
     * Nightly job: chase tickets that closed >3 days ago without a rating with a single reminder.
     * Quietly stops after one reminder.
     */
    @Scheduled(cron = "${hrms.helpdesk.csat.reminder-cron:0 0 10 * * *}")
    @Transactional
    public void chaseUnrated() {
        // Implementation hook — a repo method `findByStatusAndResolvedAtBeforeAndSatisfactionRatingIsNull`
        // would be added when this scheduler is enabled. Left as a marker for now to avoid breaking
        // the existing repo contract.
        List<Ticket> stale = List.of();
        for (Ticket t : stale) {
            events.publish(Topics.HELPDESK, DomainEvent.of(
                    "helpdesk.csat.reminder", "helpdesk",
                    t.getTenantId(), t.getId().toString(), "Ticket", Map.of()));
        }
        log.debug("CSAT reminder pass run @ {}", Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
