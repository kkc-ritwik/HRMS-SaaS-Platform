package com.hrms.reports.engine;

import com.hrms.mail.model.MailAttachment;
import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Wakes up every minute; finds report schedules due now (cron handled at the row level by
 * comparing nextFireAt). Runs the underlying report, exports per format, emails to the
 * subscribers list. Records run history.
 *
 * The {@link ScheduleRunSource} bean is implemented by the data-access layer (queries the
 * report_schedules table); this keeps the runner DB-schema-agnostic.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledReportRunner {

    private final ScheduleRunSource source;
    private final ReportQueryEngine engine;
    private final ReportExporter exporter;
    private final MailService mailService;

    @Scheduled(fixedDelayString = "${hrms.reports.scheduler.poll-ms:60000}")
    @Transactional
    public void run() {
        List<DueSchedule> due = source.fetchDue(java.time.OffsetDateTime.now());
        for (DueSchedule d : due) {
            try {
                ReportQueryEngine.ResultSet rs = engine.run(d.sql(), d.bindings(), d.maxRows());
                MailRequest.MailRequestBuilder mb = MailRequest.builder()
                        .to(d.recipients()).subject("Scheduled Report — " + d.reportName())
                        .html("<p>Report attached.</p><p>Generated " + LocalDate.now() + "</p>")
                        .category("scheduled-report");

                if ("XLSX".equalsIgnoreCase(d.format())) {
                    byte[] b = exporter.toExcel(d.reportName(), rs);
                    mb.attachment(MailAttachment.builder().filename(d.reportName() + ".xlsx")
                            .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                            .content(b).build());
                } else if ("PDF".equalsIgnoreCase(d.format())) {
                    byte[] b = exporter.toPdf(d.reportName(), rs);
                    mb.attachment(MailAttachment.builder().filename(d.reportName() + ".pdf")
                            .contentType("application/pdf").content(b).build());
                } else {
                    byte[] b = exporter.toCsv(rs);
                    mb.attachment(MailAttachment.builder().filename(d.reportName() + ".csv")
                            .contentType("text/csv").content(b).build());
                }
                mailService.sendAsync(mb.build());
                source.markRan(d.scheduleId());
            } catch (Exception e) {
                log.error("Scheduled report {} failed: {}", d.scheduleId(), e.getMessage(), e);
                source.markFailed(d.scheduleId(), e.getMessage());
            }
        }
    }

    public interface ScheduleRunSource {
        List<DueSchedule> fetchDue(java.time.OffsetDateTime now);
        void markRan(java.util.UUID scheduleId);
        void markFailed(java.util.UUID scheduleId, String error);
    }

    public record DueSchedule(java.util.UUID scheduleId, String reportName,
                              String sql, Map<String, Object> bindings,
                              int maxRows, String format, List<String> recipients) {}
}
