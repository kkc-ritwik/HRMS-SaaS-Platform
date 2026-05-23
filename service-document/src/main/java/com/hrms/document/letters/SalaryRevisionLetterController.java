package com.hrms.document.letters;

import com.hrms.pdf.service.PdfService;
import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Dedicated salary revision letter — extends the short INCREMENT template by appending
 * a structured breakup table (Basic / HRA / Special / Bonus / Gross / CTC).
 *
 * Inputs are caller-supplied (typically populated by service-compensation from the
 * approved revision record); this service only handles document rendering + archival.
 */
@Slf4j
@RestController
@RequestMapping("/api/documents/letters/salary-revision")
@RequiredArgsConstructor
public class SalaryRevisionLetterController {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final PdfService pdfService;
    private final StorageService storage;

    @PostMapping
    public Map<String, Object> generate(@RequestBody RevisionRequest req) {
        String tenant = TenantContext.get();
        String html = renderHtml(req);

        byte[] pdf = pdfService.renderTemplate(html, Map.of());
        pdf = pdfService.stampHeaderFooter(pdf,
                req.companyName == null ? "HRMS" : req.companyName,
                "Salary revision · " + D.format(LocalDate.now()));

        String filename = "salary-revision-" + req.employeeCode + "-" +
                req.effectiveDate.toString() + ".pdf";
        StoredFile sf = storage.upload(tenant, "letters/salary-revision",
                filename, "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);

        log.info("Salary revision letter generated for {} → {}", req.employeeCode, sf.getStorageUri());
        return Map.of(
                "storageUri", sf.getStorageUri(),
                "filename", filename,
                "effectiveDate", req.effectiveDate.toString(),
                "percentIncrease", percentChange(req.currentCtc, req.newCtc)
        );
    }

    private String renderHtml(RevisionRequest r) {
        StringBuilder rows = new StringBuilder();
        for (BreakupRow row : safe(r.breakup)) {
            rows.append("<tr><td>").append(esc(row.component)).append("</td>")
                    .append("<td style='text-align:right'>").append(money(row.current)).append("</td>")
                    .append("<td style='text-align:right'>").append(money(row.revised)).append("</td>")
                    .append("<td style='text-align:right'>").append(money(diff(row.current, row.revised))).append("</td>")
                    .append("</tr>");
        }

        BigDecimal pctCtc = percentChange(r.currentCtc, r.newCtc);

        return """
            <html><body style='font-family:Times,serif;font-size:12px;line-height:1.5'>
              <p style='text-align:right'>Date: %s</p>
              <h2 style='text-align:center'>SALARY REVISION LETTER</h2>
              <p>Dear %s (%s),</p>
              <p>We are pleased to inform you that based on your performance, contribution and
              the annual compensation review, your total compensation has been revised effective
              <strong>%s</strong>.</p>
              <p><strong>Reason:</strong> %s</p>
              <table border='1' cellpadding='6' cellspacing='0' style='border-collapse:collapse;width:100%%'>
                <thead style='background:#f0f0f0'>
                  <tr><th>Component</th><th>Current (%s)</th><th>Revised (%s)</th><th>Change</th></tr>
                </thead>
                <tbody>%s</tbody>
                <tfoot style='background:#fafafa;font-weight:bold'>
                  <tr><td>Total CTC</td>
                      <td style='text-align:right'>%s</td>
                      <td style='text-align:right'>%s</td>
                      <td style='text-align:right'>+%s%%</td>
                  </tr>
                </tfoot>
              </table>
              <p style='margin-top:18px'>All other terms and conditions of your employment remain
              unchanged. Statutory deductions (PF / ESI / Income Tax) will be applied per applicable
              law on the revised figures.</p>
              <p>Congratulations on this revision. We look forward to your continued contributions.</p>
              <br/><br/>
              <p>Sincerely,<br/><strong>%s</strong><br/>%s<br/>%s</p>
            </body></html>
            """.formatted(
                D.format(LocalDate.now()),
                esc(r.employeeName), esc(r.employeeCode),
                D.format(r.effectiveDate),
                esc(r.reason),
                esc(r.currency), esc(r.currency),
                rows.toString(),
                money(r.currentCtc), money(r.newCtc), pctCtc,
                esc(r.signatoryName), esc(r.signatoryTitle), esc(r.companyName)
        );
    }

    private static BigDecimal diff(BigDecimal a, BigDecimal b) {
        if (a == null) a = BigDecimal.ZERO;
        if (b == null) b = BigDecimal.ZERO;
        return b.subtract(a);
    }

    private static BigDecimal percentChange(BigDecimal from, BigDecimal to) {
        if (from == null || to == null || from.signum() == 0) return BigDecimal.ZERO;
        return to.subtract(from)
                .multiply(BigDecimal.valueOf(100))
                .divide(from, 2, RoundingMode.HALF_UP);
    }

    private static String money(BigDecimal v) { return v == null ? "-" : v.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
    private static String esc(String s) { return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;"); }
    private static <T> List<T> safe(List<T> in) { return in == null ? Collections.emptyList() : in; }

    public static class RevisionRequest {
        public UUID employeeId;
        public String employeeName;
        public String employeeCode;
        public String companyName;
        public String currency;
        public LocalDate effectiveDate;
        public String reason;
        public BigDecimal currentCtc;
        public BigDecimal newCtc;
        public List<BreakupRow> breakup;
        public String signatoryName;
        public String signatoryTitle;
    }

    public static class BreakupRow {
        public String component;
        public BigDecimal current;
        public BigDecimal revised;
    }
}
