package com.hrms.reports.engine;

import com.hrms.reports.entity.ReportSchedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DB-backed implementation of {@link ScheduledReportRunner.ScheduleRunSource}.
 * Looks up rows from report_schedules whose next_fire_at <= now, hands them to the runner,
 * updates last_fired_at + computes next fire time post-execution.
 *
 * NOTE: cron parsing uses Spring's CronExpression — far simpler than embedding Quartz.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaScheduleRunSource implements ScheduledReportRunner.ScheduleRunSource {

    public interface Repo extends JpaRepository<ReportSchedule, UUID> {
        @Query("SELECT s FROM ReportSchedule s WHERE s.active = true AND s.deleted = false " +
                "AND (s.nextFireAt IS NULL OR s.nextFireAt <= :now)")
        List<ReportSchedule> findDue(@Param("now") OffsetDateTime now);
    }

    private final Repo repo;

    @Override
    public List<ScheduledReportRunner.DueSchedule> fetchDue(OffsetDateTime now) {
        return repo.findDue(now).stream()
                .map(s -> new ScheduledReportRunner.DueSchedule(
                        s.getId(), s.getName(), s.getSqlQuery(),
                        s.getBindings(), s.getMaxRows(),
                        s.getFormat(), s.getRecipients()))
                .toList();
    }

    @Override
    public void markRan(UUID scheduleId) {
        repo.findById(scheduleId).ifPresent(s -> {
            s.setLastFiredAt(OffsetDateTime.now());
            s.setLastError(null);
            s.setConsecutiveFailures(0);
            s.setNextFireAt(computeNextFire(s.getCronExpression(), s.getTimeZone()));
            repo.save(s);
        });
    }

    @Override
    public void markFailed(UUID scheduleId, String error) {
        repo.findById(scheduleId).ifPresent(s -> {
            s.setLastError(error == null ? "Unknown error" : error.substring(0, Math.min(1900, error.length())));
            s.setConsecutiveFailures((s.getConsecutiveFailures() == null ? 0 : s.getConsecutiveFailures()) + 1);
            // Back off — try again in 30 minutes after a failure
            s.setNextFireAt(OffsetDateTime.now().plusMinutes(30));
            // Disable after 5 consecutive failures to stop noise
            if (s.getConsecutiveFailures() >= 5) {
                s.setActive(false);
                log.error("Report schedule {} disabled after 5 consecutive failures", scheduleId);
            }
            repo.save(s);
        });
    }

    private OffsetDateTime computeNextFire(String cron, String tz) {
        try {
            org.springframework.scheduling.support.CronExpression expr =
                    org.springframework.scheduling.support.CronExpression.parse(cron);
            java.time.ZoneId zone = (tz == null || tz.isBlank())
                    ? java.time.ZoneOffset.UTC : java.time.ZoneId.of(tz);
            java.time.LocalDateTime nextLocal = expr.next(java.time.LocalDateTime.now(zone));
            return nextLocal == null ? null : nextLocal.atZone(zone).toOffsetDateTime();
        } catch (Exception e) {
            log.warn("Bad cron expression '{}': {}", cron, e.getMessage());
            return OffsetDateTime.now().plusHours(1);
        }
    }
}
