package com.hrms.engagement.celebrations;

import com.hrms.clients.corehr.EmployeeClient;
import com.hrms.clients.corehr.dto.EmployeeDto;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Daily celebration crawler — emits engagement.celebration events for birthdays and
 * work anniversaries falling today. Listeners (notification service, social feed) post
 * the announcement / send personal notes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CelebrationScheduler {

    private final ObjectProvider<EmployeeClient> employeeClientProvider;
    private final EventPublisher events;

    /** Runs every day at 07:00 local server time. */
    @Scheduled(cron = "${hrms.engagement.celebrations.cron:0 0 7 * * *}")
    public void scanForCelebrations() {
        EmployeeClient ec = employeeClientProvider.getIfAvailable();
        if (ec == null) { log.debug("EmployeeClient unavailable — celebration scan skipped"); return; }

        LocalDate today = LocalDate.now();
        MonthDay todayMD = MonthDay.from(today);

        try {
            List<EmployeeDto> all = ec.search(null, null, "ACTIVE");
            int birthdays = 0, anniversaries = 0;
            for (EmployeeDto e : all) {
                if (e.getDateOfBirth() != null && MonthDay.from(e.getDateOfBirth()).equals(todayMD)) {
                    events.publishDirect(Topics.ENGAGEMENT, DomainEvent.of(
                            "engagement.celebration.birthday", "engagement",
                            e.getTenantId(), e.getId().toString(), "Employee",
                            Map.of("employeeId", e.getId(), "displayName",
                                    e.getDisplayName() == null ? e.getFirstName() : e.getDisplayName())));
                    birthdays++;
                }
                if (e.getJoinDate() != null && MonthDay.from(e.getJoinDate()).equals(todayMD)
                        && !e.getJoinDate().equals(today)) {
                    long years = ChronoUnit.YEARS.between(e.getJoinDate(), today);
                    if (years >= 1) {
                        events.publishDirect(Topics.ENGAGEMENT, DomainEvent.of(
                                "engagement.celebration.workAnniversary", "engagement",
                                e.getTenantId(), e.getId().toString(), "Employee",
                                Map.of("employeeId", e.getId(), "yearsOfService", years,
                                        "displayName", e.getDisplayName() == null ? e.getFirstName() : e.getDisplayName())));
                        anniversaries++;
                    }
                }
            }
            log.info("Celebration scan complete: {} birthdays, {} work anniversaries", birthdays, anniversaries);
        } catch (Exception e) {
            log.warn("Celebration scan failed: {}", e.getMessage());
        }
    }
}
