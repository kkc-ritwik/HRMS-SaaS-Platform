package com.hrms.compensation.rewards;

import com.hrms.pdf.service.PdfService;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Total Rewards Statement — annual PDF summarising:
 *   - Cash compensation (base, bonus, allowances, FBP, employer PF/ESI)
 *   - Equity (vested + unvested units × current FMV)
 *   - Benefits (mediclaim premium, life insurance, gratuity accrued)
 *   - Total cost-to-company
 *
 * Sent to employees once a year alongside the appraisal letter so they see full value
 * of their package, not just the take-home.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TotalRewardsStatementGenerator {

    private final PdfService pdfService;
    private final StorageService storage;

    public String generate(String tenantId, RewardsData d) {
        StringBuilder lineRows = new StringBuilder();
        for (Component c : d.components()) {
            lineRows.append("<tr><td>").append(esc(c.category())).append("</td>")
                    .append("<td>").append(esc(c.label())).append("</td>")
                    .append("<td style='text-align:right'>")
                    .append(c.amount() == null ? "" : c.amount().setScale(2))
                    .append("</td></tr>");
        }

        String html = """
            <html><body style='font-family:Helvetica,Arial,sans-serif;font-size:11px;color:#111'>
              <h1 style='text-align:center'>Total Rewards Statement</h1>
              <p style='text-align:center'>Period: <strong>%s</strong></p>
              <table style='border-collapse:collapse;width:100%%;margin-top:12px'>
                <tr><td><strong>Employee</strong></td><td>%s (%s)</td>
                    <td><strong>Designation</strong></td><td>%s</td></tr>
                <tr><td><strong>Department</strong></td><td>%s</td>
                    <td><strong>Date of Joining</strong></td><td>%s</td></tr>
              </table>
              <h2 style='margin-top:18px'>Compensation Breakdown</h2>
              <table style='border-collapse:collapse;width:100%%;border:1px solid #aaa'>
                <tr style='background:#f3f4f6'>
                  <th align='left'>Category</th><th align='left'>Component</th><th align='right'>Annual ₹</th>
                </tr>
                %s
                <tr style='background:#f3f4f6'>
                  <td colspan='2'><strong>Total Cost To Company</strong></td>
                  <td style='text-align:right'><strong>%s</strong></td>
                </tr>
              </table>
              <h2 style='margin-top:18px'>Equity Holdings</h2>
              <table style='border-collapse:collapse;width:100%%;border:1px solid #aaa'>
                <tr><td>Total units granted</td><td style='text-align:right'>%d</td></tr>
                <tr><td>Vested units (as of statement date)</td><td style='text-align:right'>%d</td></tr>
                <tr><td>Unvested units</td><td style='text-align:right'>%d</td></tr>
                <tr><td>Current fair market value (per unit)</td><td style='text-align:right'>%s</td></tr>
                <tr><td><strong>Estimated equity value (vested)</strong></td>
                    <td style='text-align:right'><strong>%s</strong></td></tr>
              </table>
              <p style='font-size:9px;color:#666;margin-top:24px'>
                This statement is a summary for your information. Equity values are estimates based on
                latest available FMV. Tax implications depend on your jurisdiction.
              </p>
            </body></html>
            """.formatted(
                esc(d.periodLabel()),
                esc(d.employeeName()), esc(d.employeeCode()),
                esc(d.designation()), esc(d.department()), esc(d.joinDate() == null ? "" : d.joinDate().toString()),
                lineRows.toString(),
                d.totalCtc() == null ? "" : d.totalCtc().setScale(2),
                nz(d.totalUnitsGranted()), nz(d.vestedUnits()), nz(d.unvestedUnits()),
                d.currentFmvPerUnit() == null ? "" : d.currentFmvPerUnit().setScale(2),
                d.estimatedVestedValue() == null ? "" : d.estimatedVestedValue().setScale(2));

        byte[] pdf = pdfService.htmlToPdf(html);
        pdf = pdfService.stampHeaderFooter(pdf, "Total Rewards — " + d.employeeName(), "Confidential — " + d.periodLabel());

        StoredFile sf = storage.upload(tenantId, "compensation/total-rewards/" + d.periodLabel(),
                "trs-" + d.employeeCode() + "-" + d.periodLabel() + ".pdf",
                "application/pdf", new ByteArrayInputStream(pdf), pdf.length);
        log.info("Total Rewards statement → {}", sf.getStorageUri());
        return sf.getStorageUri();
    }

    private String esc(String s) { return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;"); }
    private int nz(Integer i) { return i == null ? 0 : i; }

    public record RewardsData(
            UUID employeeId, String employeeName, String employeeCode, String designation, String department,
            LocalDate joinDate, String periodLabel,
            List<Component> components, BigDecimal totalCtc,
            Integer totalUnitsGranted, Integer vestedUnits, Integer unvestedUnits,
            BigDecimal currentFmvPerUnit, BigDecimal estimatedVestedValue) {}

    public record Component(String category, String label, BigDecimal amount) {}
}
