package com.hrms.helpdesk.engine;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.helpdesk.entity.Ticket;
import com.hrms.helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Periodically scans OPEN/IN_PROGRESS tickets whose dueBy has passed and emits
 * helpdesk.ticket.escalated. Listeners (notification service) alert the assignee + manager.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketSlaEscalator {

    private final TicketRepository tickets;
    private final EventPublisher events;

    @Scheduled(fixedDelayString = "${hrms.helpdesk.sla.poll-ms:60000}")
    @Transactional
    public void scan() {
        List<Ticket> overdue = tickets.findOverdue(Instant.now());
        for (Ticket t : overdue) {
            events.publish(Topics.HELPDESK, DomainEvent.of(
                    "helpdesk.ticket.escalated", "helpdesk",
                    t.getTenantId(), t.getId().toString(), "Ticket",
                    Map.of("priority", t.getPriority(), "assigneeId",
                           t.getAssigneeId() == null ? "" : t.getAssigneeId().toString())));
            // Bump priority one level if not already CRITICAL
            if (t.getPriority() != Ticket.Priority.CRITICAL) {
                t.setPriority(switch (t.getPriority()) {
                    case LOW -> Ticket.Priority.MEDIUM;
                    case MEDIUM -> Ticket.Priority.HIGH;
                    case HIGH, CRITICAL -> Ticket.Priority.CRITICAL;
                });
                tickets.save(t);
            }
            log.warn("Ticket {} SLA breached — escalated to {}", t.getId(), t.getPriority());
        }
    }
}
