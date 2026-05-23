package com.hrms.document.letters;

import com.hrms.pdf.service.PdfService;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generates routine employment letters — Confirmation, Promotion, Transfer, Increment,
 * Probation Extension. Stores PDF in object storage and returns the storage URI; the
 * caller is responsible for triggering e-sign + email distribution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmploymentLetterGenerator {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final PdfService pdfService;
    private final StorageService storage;

    public String generate(LetterRequest req) {
        String html = templateFor(req.type());
        byte[] pdf = pdfService.renderTemplate(html, asMap(req));
        pdf = pdfService.stampHeaderFooter(pdf,
                req.companyName() == null ? "HRMS" : req.companyName(),
                "Issued " + D.format(LocalDate.now()));

        String filename = req.type().name().toLowerCase() + "-letter-" + req.employeeCode() + ".pdf";
        StoredFile sf = storage.upload(req.tenantId(),
                "letters/" + req.type().name().toLowerCase(),
                filename, "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);
        log.info("Generated {} letter for {} → {}", req.type(), req.employeeCode(), sf.getStorageUri());
        return sf.getStorageUri();
    }

    private Map<String, Object> asMap(LetterRequest r) {
        Map<String, Object> m = new HashMap<>();
        m.put("today", D.format(LocalDate.now()));
        m.put("employeeName", n(r.employeeName()));
        m.put("employeeCode", n(r.employeeCode()));
        m.put("companyName", n(r.companyName()));
        m.put("currentDesignation", n(r.currentDesignation()));
        m.put("newDesignation", n(r.newDesignation()));
        m.put("currentDepartment", n(r.currentDepartment()));
        m.put("newDepartment", n(r.newDepartment()));
        m.put("currentLocation", n(r.currentLocation()));
        m.put("newLocation", n(r.newLocation()));
        m.put("effectiveDate", r.effectiveDate() == null ? "" : D.format(r.effectiveDate()));
        m.put("joinDate", r.joinDate() == null ? "" : D.format(r.joinDate()));
        m.put("probationEndDate", r.probationEndDate() == null ? "" : D.format(r.probationEndDate()));
        m.put("newSalary", n(r.newSalary()));
        m.put("currentSalary", n(r.currentSalary()));
        m.put("signatoryName", n(r.signatoryName()));
        m.put("signatoryTitle", n(r.signatoryTitle()));
        m.put("reason", n(r.reason()));
        return m;
    }

    private String n(Object o) { return o == null ? "" : o.toString(); }

    private String templateFor(LetterType type) {
        return switch (type) {
            case CONFIRMATION -> CONFIRMATION_TMPL;
            case PROBATION_EXTENSION -> PROBATION_EXTENSION_TMPL;
            case PROMOTION -> PROMOTION_TMPL;
            case TRANSFER -> TRANSFER_TMPL;
            case INCREMENT -> INCREMENT_TMPL;
            case APPRECIATION -> APPRECIATION_TMPL;
        };
    }

    public enum LetterType { CONFIRMATION, PROBATION_EXTENSION, PROMOTION, TRANSFER, INCREMENT, APPRECIATION }

    public record LetterRequest(
            String tenantId, UUID employeeId, LetterType type,
            String employeeName, String employeeCode, String companyName,
            String currentDesignation, String newDesignation,
            String currentDepartment, String newDepartment,
            String currentLocation, String newLocation,
            String currentSalary, String newSalary,
            LocalDate effectiveDate, LocalDate joinDate, LocalDate probationEndDate,
            String signatoryName, String signatoryTitle, String reason) {}

    // ── Letter HTML templates ───────────────────────────────────────────────

    private static final String CONFIRMATION_TMPL = """
        <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
          <p style='text-align:right'>Date: {{today}}</p>
          <h2 style='text-align:center'>LETTER OF CONFIRMATION</h2>
          <p>Dear {{employeeName}} ({{employeeCode}}),</p>
          <p>We are pleased to confirm your employment with <strong>{{companyName}}</strong> as
          <strong>{{currentDesignation}}</strong> in the <strong>{{currentDepartment}}</strong>
          department, effective <strong>{{effectiveDate}}</strong>.</p>
          <p>You joined us on {{joinDate}} on a probationary basis. Based on your performance,
          conduct and contribution during the probation period, we are happy to confirm your
          appointment.</p>
          <p>All other terms and conditions of your appointment letter remain unchanged.</p>
          <p>We wish you continued success.</p>
          <br/><br/>
          <p>Sincerely,<br/><strong>{{signatoryName}}</strong><br/>{{signatoryTitle}}</p>
        </body></html>
        """;

    private static final String PROBATION_EXTENSION_TMPL = """
        <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
          <p style='text-align:right'>Date: {{today}}</p>
          <h2 style='text-align:center'>PROBATION EXTENSION</h2>
          <p>Dear {{employeeName}} ({{employeeCode}}),</p>
          <p>With reference to your appointment dated {{joinDate}}, this is to inform you that
          your probationary period has been extended until <strong>{{probationEndDate}}</strong>.</p>
          <p>Reason: {{reason}}</p>
          <p>You will be evaluated again at the end of the extended period for confirmation.</p>
          <br/><br/>
          <p>Sincerely,<br/><strong>{{signatoryName}}</strong><br/>{{signatoryTitle}}</p>
        </body></html>
        """;

    private static final String PROMOTION_TMPL = """
        <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
          <p style='text-align:right'>Date: {{today}}</p>
          <h2 style='text-align:center'>LETTER OF PROMOTION</h2>
          <p>Dear {{employeeName}} ({{employeeCode}}),</p>
          <p>It gives us great pleasure to inform you that based on your outstanding performance
          and contribution, you have been promoted from <strong>{{currentDesignation}}</strong>
          to <strong>{{newDesignation}}</strong>, effective <strong>{{effectiveDate}}</strong>.</p>
          <p>Your revised compensation will be communicated separately. All other terms and
          conditions of your employment will remain unchanged unless modified in writing.</p>
          <p>Congratulations and best wishes in your new role.</p>
          <br/><br/>
          <p>Sincerely,<br/><strong>{{signatoryName}}</strong><br/>{{signatoryTitle}}</p>
        </body></html>
        """;

    private static final String TRANSFER_TMPL = """
        <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
          <p style='text-align:right'>Date: {{today}}</p>
          <h2 style='text-align:center'>LETTER OF TRANSFER</h2>
          <p>Dear {{employeeName}} ({{employeeCode}}),</p>
          <p>This letter confirms your transfer from <strong>{{currentDepartment}}</strong>,
          {{currentLocation}} to <strong>{{newDepartment}}</strong>, {{newLocation}}, effective
          <strong>{{effectiveDate}}</strong>.</p>
          <p>Your designation, compensation and other employment conditions remain unchanged
          unless modified in writing. Please coordinate with HR for the necessary onboarding
          at your new location.</p>
          <p>We wish you the very best in your new assignment.</p>
          <br/><br/>
          <p>Sincerely,<br/><strong>{{signatoryName}}</strong><br/>{{signatoryTitle}}</p>
        </body></html>
        """;

    private static final String INCREMENT_TMPL = """
        <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
          <p style='text-align:right'>Date: {{today}}</p>
          <h2 style='text-align:center'>LETTER OF SALARY REVISION</h2>
          <p>Dear {{employeeName}} ({{employeeCode}}),</p>
          <p>We are pleased to inform you that your annual compensation has been revised from
          <strong>{{currentSalary}}</strong> to <strong>{{newSalary}}</strong>, effective
          <strong>{{effectiveDate}}</strong>.</p>
          <p>This revision is in recognition of your performance and continued contribution.
          All other terms of your employment remain unchanged.</p>
          <p>Congratulations and continued best wishes.</p>
          <br/><br/>
          <p>Sincerely,<br/><strong>{{signatoryName}}</strong><br/>{{signatoryTitle}}</p>
        </body></html>
        """;

    private static final String APPRECIATION_TMPL = """
        <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
          <p style='text-align:right'>Date: {{today}}</p>
          <h2 style='text-align:center'>LETTER OF APPRECIATION</h2>
          <p>Dear {{employeeName}},</p>
          <p>{{reason}}</p>
          <p>On behalf of <strong>{{companyName}}</strong>, thank you for your excellent work.</p>
          <br/><br/>
          <p>Sincerely,<br/><strong>{{signatoryName}}</strong><br/>{{signatoryTitle}}</p>
        </body></html>
        """;
}
