package com.hrms.asset.handover;

import com.hrms.esign.model.SignatureRequest;
import com.hrms.esign.service.ESignService;
import com.hrms.pdf.service.PdfService;
import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates a Goods Handover / Return form PDF for an asset assignment, then submits it
 * to the configured e-sign provider. Employee + IT/HR signers receive the email; once both
 * complete, the signed PDF is attached back to the AssetAssignment record.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetHandoverService {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final PdfService pdfService;
    private final StorageService storage;
    private final ESignService eSign;

    @Transactional
    public SignatureRequest requestHandoverSignature(HandoverData d) {
        byte[] pdf = pdfService.renderTemplate(handoverHtml(), Map.ofEntries(
                Map.entry("kind", d.action().toLowerCase()),
                Map.entry("today", D.format(LocalDate.now())),
                Map.entry("assetName", n(d.assetName())),
                Map.entry("assetCode", n(d.assetCode())),
                Map.entry("serialNumber", n(d.serialNumber())),
                Map.entry("conditionNotes", n(d.conditionNotes())),
                Map.entry("employeeName", n(d.employeeName())),
                Map.entry("employeeCode", n(d.employeeCode())),
                Map.entry("issuedBy", n(d.issuedByName()))));
        pdf = pdfService.stampHeaderFooter(pdf,
                "Asset " + d.action() + " — " + d.assetCode(),
                "Generated " + D.format(LocalDate.now()));

        StoredFile sf = storage.upload(TenantContext.get(),
                "asset/handover/" + d.assetId(),
                "handover-" + d.assetCode() + "-" + d.action().toLowerCase() + ".pdf",
                "application/pdf", new ByteArrayInputStream(pdf), pdf.length);

        SignatureRequest req = new SignatureRequest();
        req.setTenantId(TenantContext.get());
        req.setSubjectType("ASSET_HANDOVER");
        req.setSubjectId(d.assignmentId());
        req.setDocumentStorageUri(sf.getStorageUri());
        req.setTitle("Asset " + d.action() + ": " + d.assetName());
        req.setProvider(SignatureRequest.Provider.INTERNAL);
        req.setSigners(List.of(
                new SignatureRequest.Signer(d.employeeName(), d.employeeEmail(),
                        1, "EMPLOYEE", "PENDING", null, null),
                new SignatureRequest.Signer(d.issuedByName(), d.issuedByEmail(),
                        2, "IT_OWNER", "PENDING", null, null)));
        return eSign.createAndSend(req, pdf);
    }

    private String n(String s) { return s == null ? "" : s; }

    private String handoverHtml() {
        return """
            <html><body style='font-family:Helvetica,Arial,sans-serif;font-size:11px;color:#111'>
              <h2 style='text-align:center'>ASSET {{kind}}</h2>
              <p style='text-align:right'>Date: {{today}}</p>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td><strong>Asset</strong></td><td>{{assetName}}</td>
                    <td><strong>Code</strong></td><td>{{assetCode}}</td></tr>
                <tr><td><strong>Serial No.</strong></td><td>{{serialNumber}}</td>
                    <td><strong>Issued By</strong></td><td>{{issuedBy}}</td></tr>
                <tr><td><strong>Issued To</strong></td><td colspan='3'>{{employeeName}} ({{employeeCode}})</td></tr>
                <tr><td><strong>Condition Notes</strong></td><td colspan='3'>{{conditionNotes}}</td></tr>
              </table>
              <p>I acknowledge that I have received the asset described above and am responsible
              for its care, return in good working condition, and reporting of any damage or loss.</p>
              <br/>
              <p>______________________________ /sig1/</p>
              <p>Employee signature</p>
              <br/>
              <p>______________________________ /sig2/</p>
              <p>Issuer signature</p>
            </body></html>
            """;
    }

    public record HandoverData(UUID assignmentId, UUID assetId, String action,
                                String assetName, String assetCode, String serialNumber,
                                String conditionNotes,
                                UUID employeeId, String employeeName, String employeeCode, String employeeEmail,
                                String issuedByName, String issuedByEmail) {}
}
