package com.hrms.document.letters;

import com.hrms.pdf.service.PdfService;
import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Exit-pack letter generator — produces the four documents an employee receives at separation:
 *
 *   RELIEVING       — confirms last working day and that they are released from all duties.
 *   EXPERIENCE      — total tenure, last designation, conduct statement.
 *   SERVICE         — period of service certificate (sometimes used for visa / bank).
 *   REFERENCE       — optional, manager-signed reference (only if requested + approved).
 *
 * Letters are signed by the HR signatory; in production these are routed through the e-sign
 * provider (DocuSign / Aadhaar e-sign) via service-document's existing signing service.
 */
@Slf4j
@RestController
@RequestMapping("/api/documents/letters/exit")
@RequiredArgsConstructor
public class ExitLetterGenerator {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final PdfService pdfService;
    private final StorageService storage;

    @PostMapping("/{type}")
    public Map<String, Object> generate(@PathVariable Type type, @RequestBody ExitLetterRequest req) {
        String html = switch (type) {
            case RELIEVING   -> relieving(req);
            case EXPERIENCE  -> experience(req);
            case SERVICE     -> service(req);
            case REFERENCE   -> reference(req);
        };
        byte[] pdf = pdfService.renderTemplate(html, Map.of());
        pdf = pdfService.stampHeaderFooter(pdf,
                req.companyName == null ? "HRMS" : req.companyName,
                type.name() + " · " + D.format(LocalDate.now()));

        String filename = type.name().toLowerCase() + "-letter-" + req.employeeCode + ".pdf";
        StoredFile sf = storage.upload(TenantContext.get(),
                "letters/exit/" + type.name().toLowerCase(),
                filename, "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);
        log.info("{} letter generated for {} → {}", type, req.employeeCode, sf.getStorageUri());
        return Map.of("type", type, "storageUri", sf.getStorageUri(), "filename", filename);
    }

    private String relieving(ExitLetterRequest r) {
        return base("RELIEVING LETTER", r,
                """
                  <p>Dear %s (%s),</p>
                  <p>This is with reference to your resignation dated <strong>%s</strong>.</p>
                  <p>We confirm that you have been relieved from the services of
                  <strong>%s</strong> effective close of business on <strong>%s</strong>,
                  having served your contractual notice obligations.</p>
                  <p>You held the position of <strong>%s</strong> in the
                  <strong>%s</strong> department at the time of your separation.</p>
                  <p>Your full and final settlement is in process and will be credited to your
                  registered bank account within 45 days of your last working day, subject
                  to clearance from all departments.</p>
                  <p>We thank you for your contributions during your association with us, and
                  wish you the very best in your future endeavours.</p>
                """.formatted(
                        esc(r.employeeName), esc(r.employeeCode),
                        r.resignationDate == null ? "" : D.format(r.resignationDate),
                        esc(r.companyName),
                        D.format(r.lastWorkingDay),
                        esc(r.designation), esc(r.department)
                ));
    }

    private String experience(ExitLetterRequest r) {
        return base("EXPERIENCE CERTIFICATE", r,
                """
                  <p>TO WHOMSOEVER IT MAY CONCERN</p>
                  <p>This is to certify that <strong>%s</strong> (Employee ID: %s) was employed
                  with <strong>%s</strong> from <strong>%s</strong> to <strong>%s</strong>.</p>
                  <p>During the tenure of <strong>%s</strong>, %s served in the capacity of
                  <strong>%s</strong> in the <strong>%s</strong> department, with the last
                  drawn designation being <strong>%s</strong>.</p>
                  <p>We found %s to be sincere, hard-working and of good moral character.
                  %s has been relieved from our services with effect from %s.</p>
                  <p>We wish %s all the best for future endeavours.</p>
                """.formatted(
                        esc(r.employeeName), esc(r.employeeCode), esc(r.companyName),
                        D.format(r.joinDate), D.format(r.lastWorkingDay),
                        esc(r.tenure),
                        r.pronoun == null ? "he/she" : esc(r.pronoun),
                        esc(r.firstDesignation == null ? r.designation : r.firstDesignation),
                        esc(r.department),
                        esc(r.designation),
                        r.pronoun == null ? "him/her" : esc(r.pronoun),
                        r.pronounNominative == null ? "He/She" : esc(r.pronounNominative),
                        D.format(r.lastWorkingDay),
                        r.pronounObjective == null ? "him/her" : esc(r.pronounObjective)
                ));
    }

    private String service(ExitLetterRequest r) {
        return base("SERVICE CERTIFICATE", r,
                """
                  <p>TO WHOMSOEVER IT MAY CONCERN</p>
                  <p>This is to certify that <strong>%s</strong> (Employee ID: %s) was employed
                  with <strong>%s</strong> from <strong>%s</strong> to <strong>%s</strong>,
                  a total period of <strong>%s</strong>.</p>
                  <p>%s held the position of <strong>%s</strong> at the time of separation.</p>
                  <p>This certificate is issued upon the request of the employee and may be
                  used for any official purpose as required.</p>
                """.formatted(
                        esc(r.employeeName), esc(r.employeeCode), esc(r.companyName),
                        D.format(r.joinDate), D.format(r.lastWorkingDay),
                        esc(r.tenure),
                        r.pronounNominative == null ? "He/She" : esc(r.pronounNominative),
                        esc(r.designation)
                ));
    }

    private String reference(ExitLetterRequest r) {
        return base("REFERENCE LETTER", r,
                """
                  <p>TO WHOMSOEVER IT MAY CONCERN</p>
                  <p>I have had the pleasure of working with <strong>%s</strong> during their
                  tenure at <strong>%s</strong> from <strong>%s</strong> to <strong>%s</strong>
                  as a <strong>%s</strong>.</p>
                  <p>%s</p>
                  <p>I have no hesitation in recommending %s for future opportunities and am
                  happy to be contacted for any further reference.</p>
                """.formatted(
                        esc(r.employeeName), esc(r.companyName),
                        D.format(r.joinDate), D.format(r.lastWorkingDay),
                        esc(r.designation),
                        esc(r.referenceBody == null ? "Throughout this period the employee demonstrated strong commitment, technical expertise, and collaborative spirit." : r.referenceBody),
                        r.pronounObjective == null ? "him/her" : esc(r.pronounObjective)
                ));
    }

    private String base(String title, ExitLetterRequest r, String body) {
        return """
            <html><body style='font-family:Times,serif;font-size:12px;line-height:1.6'>
              <p style='text-align:right'>Date: %s</p>
              <h2 style='text-align:center'>%s</h2>
              %s
              <br/><br/>
              <p>Sincerely,<br/><strong>%s</strong><br/>%s<br/>%s</p>
            </body></html>
            """.formatted(
                D.format(LocalDate.now()), title, body,
                esc(r.signatoryName), esc(r.signatoryTitle), esc(r.companyName)
        );
    }

    private static String esc(String s) { return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;"); }

    public enum Type { RELIEVING, EXPERIENCE, SERVICE, REFERENCE }

    public static class ExitLetterRequest {
        public java.util.UUID employeeId;
        public String employeeName;
        public String employeeCode;
        public String companyName;
        public String designation;
        public String firstDesignation;
        public String department;
        public String tenure;                  // e.g. "3 years 4 months"
        public LocalDate joinDate;
        public LocalDate resignationDate;
        public LocalDate lastWorkingDay;
        public String pronoun;                 // "he"/"she"/"they"
        public String pronounNominative;       // "He"/"She"/"They"
        public String pronounObjective;        // "him"/"her"/"them"
        public String signatoryName;
        public String signatoryTitle;
        public String referenceBody;
    }
}
