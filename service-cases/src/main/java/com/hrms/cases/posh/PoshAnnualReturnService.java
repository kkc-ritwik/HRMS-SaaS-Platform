package com.hrms.cases.posh;

import com.hrms.cases.entity.HrCase;
import com.hrms.pdf.service.PdfService;
import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Generates the POSH (Prevention of Sexual Harassment) annual return as required by
 * Section 21 of the Sexual Harassment of Women at Workplace Act, 2013 and Rule 14 of the
 * 2013 Rules. The report must be filed by every employer with the District Officer by
 * 31 January each year covering the previous calendar year.
 *
 * Captured fields:
 *  · Total complaints received in calendar year
 *  · Complaints disposed of
 *  · Complaints pending > 90 days
 *  · Workshops / awareness programmes conducted (caller-supplied)
 *  · Nature of action taken by the employer
 */
@Slf4j
@RestController
@RequestMapping("/api/cases/posh")
@RequiredArgsConstructor
public class PoshAnnualReturnService {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy");

    @PersistenceContext private EntityManager em;
    private final PdfService pdfService;
    private final StorageService storage;

    @GetMapping("/annual-return/preview")
    @Transactional(readOnly = true)
    public Map<String, Object> preview(@RequestParam int year) {
        return compileSummary(TenantContext.get(), year);
    }

    @PostMapping("/annual-return/generate")
    @Transactional
    public Map<String, Object> generate(@RequestBody Map<String, Object> body) {
        String tenant = TenantContext.get();
        int year = ((Number) body.get("year")).intValue();
        Map<String, Object> summary = new LinkedHashMap<>(compileSummary(tenant, year));

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("year", year);
        details.put("employerName", body.get("employerName"));
        details.put("employerAddress", body.get("employerAddress"));
        details.put("iccChairperson", body.get("iccChairperson"));
        details.put("iccMembers", body.getOrDefault("iccMembers", List.of()));
        details.put("workshopsConducted", body.getOrDefault("workshopsConducted", 0));
        details.put("workshopAttendance", body.getOrDefault("workshopAttendance", 0));
        details.put("natureOfAction", body.getOrDefault("natureOfAction", ""));

        String html = renderHtml(summary, details);
        byte[] pdf = pdfService.renderTemplate(html, Map.of());
        pdf = pdfService.stampHeaderFooter(pdf,
                String.valueOf(details.get("employerName")),
                "POSH Annual Return · CY " + year);

        String filename = "posh-annual-return-" + year + ".pdf";
        StoredFile sf = storage.upload(tenant, "compliance/posh/" + year,
                filename, "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);

        log.info("POSH annual return generated for {} (year {}): {}",
                tenant, year, sf.getStorageUri());

        return Map.of(
                "year", year,
                "storageUri", sf.getStorageUri(),
                "summary", summary
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> compileSummary(String tenant, int year) {
        OffsetDateTime from = LocalDate.of(year, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime to = LocalDate.of(year, 12, 31).atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        Object[] counts = (Object[]) em.createNativeQuery(
                "SELECT " +
                "  COUNT(*) FILTER (WHERE created_at BETWEEN :from AND :to) AS received," +
                "  COUNT(*) FILTER (WHERE created_at BETWEEN :from AND :to AND status IN ('RESOLVED','CLOSED')) AS disposed," +
                "  COUNT(*) FILTER (WHERE created_at BETWEEN :from AND :to AND status NOT IN ('RESOLVED','CLOSED') " +
                "                     AND EXTRACT(EPOCH FROM (NOW() - created_at)) / 86400 > 90) AS pending_90," +
                "  COUNT(*) FILTER (WHERE created_at BETWEEN :from AND :to AND severity = 'CRITICAL') AS critical," +
                "  COUNT(*) FILTER (WHERE created_at BETWEEN :from AND :to AND is_anonymous = true) AS anonymous_count " +
                "FROM hr_cases " +
                "WHERE tenant_id = :t AND type IN ('POSH','HARASSMENT')")
                .setParameter("from", from)
                .setParameter("to", to)
                .setParameter("t", tenant)
                .getSingleResult();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalReceived", num(counts[0]));
        out.put("disposed", num(counts[1]));
        out.put("pendingMoreThan90Days", num(counts[2]));
        out.put("critical", num(counts[3]));
        out.put("anonymous", num(counts[4]));

        List<Object[]> byStatus = em.createNativeQuery(
                "SELECT status, COUNT(*) FROM hr_cases " +
                "WHERE tenant_id = :t AND type IN ('POSH','HARASSMENT') AND created_at BETWEEN :from AND :to " +
                "GROUP BY status")
                .setParameter("t", tenant)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (Object[] r : byStatus) statusMap.put(String.valueOf(r[0]), ((Number) r[1]).longValue());
        out.put("byStatus", statusMap);

        return out;
    }

    private static long num(Object o) { return o == null ? 0 : ((Number) o).longValue(); }

    private String renderHtml(Map<String, Object> summary, Map<String, Object> d) {
        StringBuilder iccRows = new StringBuilder();
        for (Object m : (List<?>) d.get("iccMembers")) {
            iccRows.append("<li>").append(esc(String.valueOf(m))).append("</li>");
        }

        return """
            <html><body style='font-family:Times,serif;font-size:12px;line-height:1.5'>
              <h2 style='text-align:center'>POSH ANNUAL RETURN</h2>
              <p style='text-align:center'>Filed under Section 21, Sexual Harassment of Women at
              Workplace (Prevention, Prohibition and Redressal) Act, 2013</p>
              <p><strong>Calendar Year:</strong> %s<br/>
                 <strong>Filed on:</strong> %s</p>

              <h3>1. Employer Details</h3>
              <p><strong>Name:</strong> %s<br/>
                 <strong>Address:</strong> %s</p>

              <h3>2. Internal Complaints Committee (ICC)</h3>
              <p><strong>Presiding Officer:</strong> %s</p>
              <ol>%s</ol>

              <h3>3. Complaint Summary</h3>
              <table border='1' cellpadding='6' style='border-collapse:collapse;width:60%%'>
                <tr><td>Complaints received during the year</td><td style='text-align:right'>%s</td></tr>
                <tr><td>Complaints disposed of</td><td style='text-align:right'>%s</td></tr>
                <tr><td>Complaints pending &gt; 90 days</td><td style='text-align:right'>%s</td></tr>
                <tr><td>Severity: Critical</td><td style='text-align:right'>%s</td></tr>
                <tr><td>Anonymous complaints</td><td style='text-align:right'>%s</td></tr>
              </table>

              <h3>4. Awareness Programmes</h3>
              <p>Number of workshops / orientation programmes conducted: <strong>%s</strong></p>
              <p>Total attendance: <strong>%s</strong></p>

              <h3>5. Nature of Action Taken by Employer</h3>
              <p>%s</p>

              <br/><br/>
              <p>I hereby declare that the information furnished above is true and correct to
              the best of my knowledge.</p>
              <p>Signature of Employer / Authorised Representative</p>
            </body></html>
            """.formatted(
                d.get("year"), D.format(LocalDate.now()),
                esc(String.valueOf(d.get("employerName"))),
                esc(String.valueOf(d.get("employerAddress"))),
                esc(String.valueOf(d.get("iccChairperson"))),
                iccRows.toString(),
                summary.get("totalReceived"), summary.get("disposed"),
                summary.get("pendingMoreThan90Days"), summary.get("critical"),
                summary.get("anonymous"),
                d.get("workshopsConducted"), d.get("workshopAttendance"),
                esc(String.valueOf(d.get("natureOfAction")))
        );
    }

    private static String esc(String s) { return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;"); }
}
