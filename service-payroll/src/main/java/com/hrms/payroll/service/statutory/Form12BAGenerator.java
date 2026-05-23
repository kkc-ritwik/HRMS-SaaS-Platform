package com.hrms.payroll.service.statutory;

import com.hrms.pdf.service.PdfService;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Form 12BA — Statement showing particulars of perquisites, other fringe benefits or
 * amenities and profits in lieu of salary with value thereof. Issued annually along
 * with Form 16 when the salary income exceeds ₹150,000 + perquisites are paid.
 *
 * Reference: Rule 26A(2) of Income Tax Rules, 1962.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form12BAGenerator {

    private final PdfService pdfService;
    private final StorageService storage;

    public String generateAndStore(String tenantId, Form12BAData d) {
        String html = """
            <html><body style='font-family:Helvetica,Arial,sans-serif;font-size:10px;color:#111'>
              <h2 style='margin:0;text-align:center'>FORM No. 12BA</h2>
              <p style='text-align:center;margin:4px 0 12px 0;font-size:9px'>
                (See rule 26A(2))<br/>
                Statement showing particulars of perquisites, other fringe benefits or amenities
                and profits in lieu of salary with value thereof
              </p>

              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa;font-size:10px'>
                <tr><td><strong>1. Name and address of employer</strong></td><td>{{employerName}}<br/>{{employerAddress}}</td></tr>
                <tr><td><strong>2. TAN</strong></td><td>{{tan}}</td></tr>
                <tr><td><strong>3. TDS Assessment Range</strong></td><td>{{tdsRange}}</td></tr>
                <tr><td><strong>4. Name, designation and PAN of employee</strong></td>
                    <td>{{employeeName}}<br/>{{designation}}<br/>PAN: {{pan}}</td></tr>
                <tr><td><strong>5. Is the employee a director?</strong></td><td>{{isDirector}}</td></tr>
                <tr><td><strong>6. Income under head "Salaries" (excluding perks)</strong></td>
                    <td style='text-align:right'>{{salaryExcludingPerks}}</td></tr>
                <tr><td><strong>7. Financial year</strong></td><td>{{fy}}</td></tr>
                <tr><td><strong>8. Valuation of perquisites</strong></td><td>see table below</td></tr>
              </table>

              <h3 style='margin-top:14px'>Details of perquisites, other fringe benefits</h3>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa;font-size:9.5px'>
                <tr style='background:#f3f4f6'>
                  <th align='left'>Nature of perquisite</th>
                  <th align='right'>(a) Value as per Rule 3</th>
                  <th align='right'>(b) Amount recovered from employee</th>
                  <th align='right'>(c) Amount of perquisite chargeable [a-b]</th>
                </tr>
                <tr><td>Accommodation</td><td style='text-align:right'>{{accommA}}</td><td style='text-align:right'>{{accommB}}</td><td style='text-align:right'>{{accommC}}</td></tr>
                <tr><td>Cars / Other automotive</td><td style='text-align:right'>{{carA}}</td><td style='text-align:right'>{{carB}}</td><td style='text-align:right'>{{carC}}</td></tr>
                <tr><td>Sweeper, gardener, watchman, personal attendant</td><td style='text-align:right'>{{servantA}}</td><td style='text-align:right'>{{servantB}}</td><td style='text-align:right'>{{servantC}}</td></tr>
                <tr><td>Gas, electricity, water</td><td style='text-align:right'>{{utilitiesA}}</td><td style='text-align:right'>{{utilitiesB}}</td><td style='text-align:right'>{{utilitiesC}}</td></tr>
                <tr><td>Interest-free or concessional loans</td><td style='text-align:right'>{{loanA}}</td><td style='text-align:right'>{{loanB}}</td><td style='text-align:right'>{{loanC}}</td></tr>
                <tr><td>Holiday expenses</td><td style='text-align:right'>{{holidayA}}</td><td style='text-align:right'>{{holidayB}}</td><td style='text-align:right'>{{holidayC}}</td></tr>
                <tr><td>Free meals</td><td style='text-align:right'>{{mealsA}}</td><td style='text-align:right'>{{mealsB}}</td><td style='text-align:right'>{{mealsC}}</td></tr>
                <tr><td>Free education</td><td style='text-align:right'>{{educationA}}</td><td style='text-align:right'>{{educationB}}</td><td style='text-align:right'>{{educationC}}</td></tr>
                <tr><td>Gifts, vouchers, etc.</td><td style='text-align:right'>{{giftsA}}</td><td style='text-align:right'>{{giftsB}}</td><td style='text-align:right'>{{giftsC}}</td></tr>
                <tr><td>Stock options (ESOPs)</td><td style='text-align:right'>{{esopA}}</td><td style='text-align:right'>{{esopB}}</td><td style='text-align:right'>{{esopC}}</td></tr>
                <tr><td>Other benefits / amenities</td><td style='text-align:right'>{{otherA}}</td><td style='text-align:right'>{{otherB}}</td><td style='text-align:right'>{{otherC}}</td></tr>
                <tr style='background:#f3f4f6'>
                  <td><strong>Total value of perquisites</strong></td>
                  <td style='text-align:right'><strong>{{totalA}}</strong></td>
                  <td style='text-align:right'><strong>{{totalB}}</strong></td>
                  <td style='text-align:right'><strong>{{totalC}}</strong></td>
                </tr>
              </table>

              <h3>Details of tax</h3>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td>(a) Tax deducted from salary u/s 192(1)</td><td style='text-align:right'>{{tdsOnSalary}}</td></tr>
                <tr><td>(b) Tax paid by employer on behalf of employee u/s 192(1A)</td><td style='text-align:right'>{{tdsByEmployer}}</td></tr>
                <tr><td>(c) Total tax paid</td><td style='text-align:right'>{{totalTaxPaid}}</td></tr>
              </table>

              <p style='font-size:9px;color:#666;margin-top:16px'>
                I, {{signatoryName}}, working as {{signatoryDesignation}}, certify that the above
                particulars are correct. — Place: {{place}} &nbsp; Date: {{issueDate}}.
              </p>
            </body></html>
            """;
        byte[] pdf = pdfService.renderTemplate(html, asMap(d));
        pdf = pdfService.stampHeaderFooter(pdf,
                "Form 12BA — " + d.employeeName(),
                "Confidential — FY " + d.fy());

        String filename = "form12ba-" + d.pan() + "-" + d.fy() + ".pdf";
        StoredFile sf = storage.upload(tenantId, "tax/form12ba/" + d.fy(),
                filename, "application/pdf", new ByteArrayInputStream(pdf), pdf.length);
        log.info("Form 12BA generated → {}", sf.getStorageUri());
        return sf.getStorageUri();
    }

    private Map<String, Object> asMap(Form12BAData d) {
        Map<String, Object> m = new HashMap<>();
        m.put("employerName", s(d.employerName())); m.put("employerAddress", s(d.employerAddress()));
        m.put("tan", s(d.tan())); m.put("tdsRange", s(d.tdsRange()));
        m.put("employeeName", s(d.employeeName())); m.put("designation", s(d.designation()));
        m.put("pan", s(d.pan())); m.put("isDirector", d.isDirector() ? "Yes" : "No");
        m.put("salaryExcludingPerks", s(d.salaryExcludingPerks()));
        m.put("fy", s(d.fy()));
        for (var e : d.perquisites().entrySet()) m.put(e.getKey(), s(e.getValue()));
        m.put("tdsOnSalary", s(d.tdsOnSalary()));
        m.put("tdsByEmployer", s(d.tdsByEmployer()));
        m.put("totalTaxPaid", s(d.totalTaxPaid()));
        m.put("signatoryName", s(d.signatoryName()));
        m.put("signatoryDesignation", s(d.signatoryDesignation()));
        m.put("place", s(d.place())); m.put("issueDate", s(d.issueDate()));
        return m;
    }

    private String s(Object o) { return o == null ? "0" : o.toString(); }

    public record Form12BAData(
            String employerName, String employerAddress, String tan, String tdsRange,
            String employeeName, String designation, String pan, boolean isDirector,
            BigDecimal salaryExcludingPerks, String fy,
            /** Keys per row: accommA/B/C, carA/B/C, ... totalA/B/C. */
            Map<String, BigDecimal> perquisites,
            BigDecimal tdsOnSalary, BigDecimal tdsByEmployer, BigDecimal totalTaxPaid,
            String signatoryName, String signatoryDesignation, String place, String issueDate) {}
}
