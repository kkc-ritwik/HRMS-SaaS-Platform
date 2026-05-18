package com.hrms.offboarding.letter;

import com.hrms.pdf.service.PdfService;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Generates an Experience / Relieving letter PDF and stores it in object storage.
 * Used by the offboarding workflow once final-settlement is complete and dues are cleared.
 */
@Service
@RequiredArgsConstructor
public class ExperienceLetterGenerator {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final PdfService pdfService;
    private final StorageService storage;

    public String generateAndStore(LetterData d) {
        String html = """
            <html><body style='font-family:Times,serif;font-size:12px;color:#000;line-height:1.6'>
              <p style='text-align:right'>Date: {{issueDate}}</p>
              <h2 style='text-align:center;margin-top:30px'>TO WHOMSOEVER IT MAY CONCERN</h2>
              <p>This is to certify that <strong>{{employeeName}}</strong>
              (Employee ID: {{employeeCode}}) was employed with <strong>{{employerName}}</strong>
              from <strong>{{joinDate}}</strong> to <strong>{{lastWorkingDate}}</strong>.</p>
              <p>At the time of separation, {{pronoun}} was working as
              <strong>{{designation}}</strong> in the <strong>{{department}}</strong> department.</p>
              <p>During {{pronounPossessive}} tenure with us, {{pronoun}} was found to be
              {{remarks}}.</p>
              <p>We wish {{pronounObject}} all the best in {{pronounPossessive}} future endeavors.</p>
              <br/><br/>
              <p>For <strong>{{employerName}}</strong></p>
              <br/><br/>
              <p>______________________<br/>
              <strong>{{signatoryName}}</strong><br/>
              {{signatoryTitle}}</p>
            </body></html>
            """;
        byte[] pdf = pdfService.renderTemplate(html, Map.ofEntries(
                Map.entry("issueDate", D.format(d.issueDate() == null ? LocalDate.now() : d.issueDate())),
                Map.entry("employeeName", s(d.employeeName())),
                Map.entry("employeeCode", s(d.employeeCode())),
                Map.entry("employerName", s(d.employerName())),
                Map.entry("joinDate", D.format(d.joinDate())),
                Map.entry("lastWorkingDate", D.format(d.lastWorkingDate())),
                Map.entry("designation", s(d.designation())),
                Map.entry("department", s(d.department())),
                Map.entry("pronoun", s(d.pronoun())),
                Map.entry("pronounPossessive", s(d.pronounPossessive())),
                Map.entry("pronounObject", s(d.pronounObject())),
                Map.entry("remarks", s(d.remarks())),
                Map.entry("signatoryName", s(d.signatoryName())),
                Map.entry("signatoryTitle", s(d.signatoryTitle()))));

        StoredFile sf = storage.upload(d.tenantId(),
                "offboarding/experience-letters",
                "experience-" + d.employeeCode() + ".pdf",
                "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);
        return sf.getStorageUri();
    }

    private String s(Object o) { return o == null ? "" : o.toString(); }

    public record LetterData(
            String tenantId, UUID employeeId, String employeeName, String employeeCode,
            String employerName, LocalDate joinDate, LocalDate lastWorkingDate,
            String designation, String department,
            String pronoun, String pronounPossessive, String pronounObject,
            String remarks, String signatoryName, String signatoryTitle,
            LocalDate issueDate) {}
}
