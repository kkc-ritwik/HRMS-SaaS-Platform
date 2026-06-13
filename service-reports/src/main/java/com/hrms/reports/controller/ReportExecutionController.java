package com.hrms.reports.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.reports.engine.JpaScheduleRunSource;
import com.hrms.reports.engine.ReportExporter;
import com.hrms.reports.engine.ReportQueryEngine;
import com.hrms.reports.entity.ReportDefinition;
import com.hrms.reports.entity.ReportSchedule;
import com.hrms.reports.entity.SavedReport;
import com.hrms.reports.repository.ReportDefinitionRepository;
import com.hrms.reports.repository.SavedReportRepository;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Ad-hoc report execution — run a report definition's query on demand, export it
 * to CSV/XLSX/PDF, or register a recurring schedule. Reuses the existing
 * {@link ReportQueryEngine} + {@link ReportExporter} + {@code report_schedules} table.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report Execution", description = "Run, export and schedule report definitions")
public class ReportExecutionController {

    private final ReportDefinitionRepository definitionRepository;
    private final SavedReportRepository savedReportRepository;
    private final ReportQueryEngine engine;
    private final ReportExporter exporter;
    private final JpaScheduleRunSource.Repo scheduleRepo;

    @PostMapping("/{id}/run")
    @PreAuthorize("hasAuthority('REPORT:READ')")
    @Operation(summary = "Run a report definition and return the result set")
    public ResponseEntity<ApiResponse<Map<String, Object>>> run(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, Object> parameters) {
        ReportDefinition def = definition(id);
        ReportQueryEngine.ResultSet rs = engine.run(sqlOf(def), bindings(parameters), 10000);
        return ResponseEntity.ok(ApiResponse.ok(rs.toMap()));
    }

    @PostMapping("/{id}/export")
    @PreAuthorize("hasAuthority('REPORT:READ')")
    @Operation(summary = "Run a report definition and export it (csv | xlsx | pdf)")
    public ResponseEntity<byte[]> export(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "csv") String format,
            @RequestBody(required = false) Map<String, Object> parameters) {
        ReportDefinition def = definition(id);
        ReportQueryEngine.ResultSet rs = engine.run(sqlOf(def), bindings(parameters), 50000);
        String name = def.getName() == null ? "report" : def.getName().replaceAll("\\s+", "_");

        byte[] body;
        MediaType contentType;
        String ext;
        switch (format.toLowerCase()) {
            case "xlsx" -> {
                body = exporter.toExcel(def.getName(), rs);
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ext = "xlsx";
            }
            case "pdf" -> {
                body = exporter.toPdf(def.getName(), rs);
                contentType = MediaType.APPLICATION_PDF;
                ext = "pdf";
            }
            default -> {
                body = exporter.toCsv(rs);
                contentType = MediaType.parseMediaType("text/csv");
                ext = "csv";
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "." + ext + "\"")
                .contentType(contentType)
                .body(body);
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasAuthority('REPORT:WRITE')")
    @Operation(summary = "Schedule a report definition for recurring delivery")
    public ResponseEntity<ApiResponse<ReportSchedule>> schedule(
            @PathVariable UUID id, @RequestBody ScheduleRequest req) {
        ReportDefinition def = definition(id);
        ReportSchedule s = new ReportSchedule();
        s.setTenantId(TenantContext.get());
        s.setCreatedBy(currentUserId());
        s.setReportDefinitionId(def.getId());
        s.setName(req.name() != null ? req.name() : def.getName());
        s.setSqlQuery(sqlOf(def));
        s.setBindings(req.bindings());
        s.setMaxRows(req.maxRows() != null ? req.maxRows() : 10000);
        s.setFormat(req.format() != null ? req.format().toUpperCase() : "CSV");
        s.setRecipients(req.recipients() != null ? req.recipients() : List.of());
        s.setCronExpression(req.cronExpression());
        s.setTimeZone(req.timeZone());
        s.setActive(true);
        s.setNextFireAt(computeNextFire(req.cronExpression(), req.timeZone()));
        return ResponseEntity.ok(ApiResponse.ok(scheduleRepo.save(s)));
    }

    @PostMapping("/saved/{id}/share")
    @PreAuthorize("hasAuthority('REPORT:READ')")
    @Operation(summary = "Share a saved report with specific users")
    public ResponseEntity<ApiResponse<SavedReport>> share(
            @PathVariable UUID id, @RequestBody ShareRequest req) {
        SavedReport report = savedReportRepository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Saved report not found: " + id));
        List<String> shared = new java.util.ArrayList<>(
                report.getSharedWith() == null ? List.of() : report.getSharedWith());
        if (req.userIds() != null) {
            for (String u : req.userIds()) if (u != null && !shared.contains(u)) shared.add(u);
        }
        report.setSharedWith(shared);
        return ResponseEntity.ok(ApiResponse.ok(savedReportRepository.save(report)));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ReportDefinition definition(UUID id) {
        return definitionRepository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Report definition not found: " + id));
    }

    private String sqlOf(ReportDefinition def) {
        List<String> cfg = def.getQueryConfig();
        if (cfg == null || cfg.isEmpty()) {
            throw new IllegalStateException("Report definition has no query configured");
        }
        return String.join("\n", cfg);
    }

    private Map<String, Object> bindings(Map<String, Object> parameters) {
        return parameters == null ? Map.of() : parameters;
    }

    private OffsetDateTime computeNextFire(String cron, String tz) {
        if (cron == null || cron.isBlank()) return null;
        try {
            CronExpression expr = CronExpression.parse(cron);
            ZoneId zone = (tz == null || tz.isBlank()) ? ZoneId.of("UTC") : ZoneId.of(tz);
            LocalDateTime next = expr.next(LocalDateTime.now(zone));
            return next == null ? null : next.atZone(zone).toOffsetDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private String currentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof UserPrincipal up ? up.getId() : null;
    }

    public record ScheduleRequest(String name, String cronExpression, String timeZone, String format,
                                  List<String> recipients, Map<String, Object> bindings, Integer maxRows) {}

    public record ShareRequest(List<String> userIds) {}
}
