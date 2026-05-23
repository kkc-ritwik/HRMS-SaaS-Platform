package com.hrms.payroll.service.disbursement;

import com.hrms.security.model.TenantContext;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Bank disbursement file generator for salary credit.
 *
 * Three formats supported (India + globally common):
 *
 *   NEFT — Indian "NEFT Bulk Upload" CSV (corporate net banking). Columns:
 *      Beneficiary IFSC, Account No, A/c Holder Name, Amount, Remittance Info, Email
 *
 *   RTGS — RTGS Bulk format (≥ ₹2 lakh per beneficiary). Includes additional fields
 *      Sender Reference, Beneficiary Address (line 1), MICR.
 *
 *   ACH  — NACH/ACH credit (Aadhaar mapper file, India NACH 2.0). 24-column fixed-width.
 *
 *   ISO20022 — pain.001 XML (cross-border SWIFT-compatible payments).
 *
 * The generated file is uploaded to MinIO/S3 and a {@code StoredFile} returned. The
 * caller (PayrollRunService) then signs and submits to corporate banking portal.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankFileGenerator {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final StorageService storage;

    @Transactional
    public StoredFile generate(BankFileRequest req) {
        String content = switch (req.format) {
            case NEFT -> generateNeft(req);
            case RTGS -> generateRtgs(req);
            case ACH  -> generateAch(req);
            case ISO20022_PAIN_001 -> generateIso20022(req);
        };

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        String ext = req.format == Format.ISO20022_PAIN_001 ? "xml" : "csv";
        String filename = "salary-" + req.format.name().toLowerCase() + "-"
                + D.format(req.disbursementDate) + "." + ext;
        StoredFile sf = storage.upload(TenantContext.get(),
                "payroll/disbursement/" + req.disbursementDate.getYear() + "/" + req.payrollRunId,
                filename,
                req.format == Format.ISO20022_PAIN_001 ? "application/xml" : "text/csv",
                new ByteArrayInputStream(bytes), bytes.length);
        log.info("Generated {} disbursement file: {} ({} beneficiaries, total {})",
                req.format, sf.getStorageUri(), req.entries.size(), totalAmount(req.entries));
        return sf;
    }

    private String generateNeft(BankFileRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Beneficiary IFSC,Account No,Account Holder,Amount,Remittance Info,Email\n");
        for (Entry e : req.entries) {
            sb.append(csv(e.ifscCode)).append(',')
                    .append(csv(e.accountNumber)).append(',')
                    .append(csv(e.accountHolder)).append(',')
                    .append(e.amount.setScale(2, RoundingMode.HALF_UP).toPlainString()).append(',')
                    .append(csv("Salary " + req.disbursementDate.getMonth() + " " + req.disbursementDate.getYear())).append(',')
                    .append(csv(e.email)).append('\n');
        }
        return sb.toString();
    }

    private String generateRtgs(BankFileRequest req) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sender Ref,Beneficiary IFSC,MICR,Account No,Account Holder,Beneficiary Address,Amount,Purpose,Remarks\n");
        int seq = 1;
        for (Entry e : req.entries) {
            sb.append("HRMS").append(req.payrollRunId.toString().replace("-","").substring(0,8)).append(String.format("%05d", seq++)).append(',')
                    .append(csv(e.ifscCode)).append(',')
                    .append(csv(e.micr == null ? "" : e.micr)).append(',')
                    .append(csv(e.accountNumber)).append(',')
                    .append(csv(e.accountHolder)).append(',')
                    .append(csv(e.beneficiaryAddress == null ? "" : e.beneficiaryAddress)).append(',')
                    .append(e.amount.setScale(2, RoundingMode.HALF_UP).toPlainString()).append(',')
                    .append("SAL,").append(csv("Salary " + req.disbursementDate)).append('\n');
        }
        return sb.toString();
    }

    private String generateAch(BankFileRequest req) {
        // NACH credit fixed-width sample header
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-1s%-9s%-7s%-9s%-1s%-1s%-50s%-30s%-9s%-19s%-13s%-7s%-15s%-30s%-22s%-9s%-9s%-15s%-15s%-15s%-15s%-15s%-15s%-15s%n",
                "H", req.utilityCode, "00001",
                D.format(req.disbursementDate), "C", "N",
                req.companyName == null ? "" : req.companyName, "", "",
                "", "", "", "", "", "",
                "", "", "", "", "", "", "", "", ""));
        int seq = 1;
        for (Entry e : req.entries) {
            sb.append(String.format("%-1s%-9s%-7s%-9s%-1s%-9s%-19s%-13s%-7s%-15s%-30s%-22s%n",
                    "D", req.utilityCode, String.format("%07d", seq++),
                    D.format(req.disbursementDate), "C",
                    "", e.accountNumber == null ? "" : e.accountNumber,
                    e.ifscCode == null ? "" : e.ifscCode,
                    "", e.amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                    e.accountHolder == null ? "" : e.accountHolder,
                    "SAL" + req.disbursementDate));
        }
        sb.append(String.format("%-1s%-9s%-7s%-9s%n", "T", req.utilityCode,
                String.format("%07d", req.entries.size()), totalAmount(req.entries).toPlainString()));
        return sb.toString();
    }

    private String generateIso20022(BankFileRequest req) {
        StringBuilder body = new StringBuilder();
        BigDecimal total = totalAmount(req.entries);
        body.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.09\">\n")
                .append("  <CstmrCdtTrfInitn>\n")
                .append("    <GrpHdr>\n")
                .append("      <MsgId>HRMS-").append(req.payrollRunId).append("</MsgId>\n")
                .append("      <CreDtTm>").append(java.time.OffsetDateTime.now()).append("</CreDtTm>\n")
                .append("      <NbOfTxs>").append(req.entries.size()).append("</NbOfTxs>\n")
                .append("      <CtrlSum>").append(total).append("</CtrlSum>\n")
                .append("      <InitgPty><Nm>").append(req.companyName == null ? "HRMS" : esc(req.companyName)).append("</Nm></InitgPty>\n")
                .append("    </GrpHdr>\n")
                .append("    <PmtInf>\n")
                .append("      <PmtInfId>SAL-").append(D.format(req.disbursementDate)).append("</PmtInfId>\n")
                .append("      <PmtMtd>TRF</PmtMtd>\n")
                .append("      <ReqdExctnDt>").append(req.disbursementDate).append("</ReqdExctnDt>\n")
                .append("      <Dbtr><Nm>").append(esc(req.companyName == null ? "HRMS" : req.companyName)).append("</Nm></Dbtr>\n")
                .append("      <DbtrAcct><Id><Othr><Id>").append(esc(req.companyAccountNumber)).append("</Id></Othr></Id></DbtrAcct>\n");
        int seq = 1;
        for (Entry e : req.entries) {
            body.append("      <CdtTrfTxInf>\n")
                    .append("        <PmtId><EndToEndId>SAL").append(String.format("%06d", seq++)).append("</EndToEndId></PmtId>\n")
                    .append("        <Amt><InstdAmt Ccy=\"").append(req.currency).append("\">").append(e.amount).append("</InstdAmt></Amt>\n")
                    .append("        <CdtrAgt><FinInstnId><BICFI>").append(esc(e.bic == null ? e.ifscCode : e.bic)).append("</BICFI></FinInstnId></CdtrAgt>\n")
                    .append("        <Cdtr><Nm>").append(esc(e.accountHolder)).append("</Nm></Cdtr>\n")
                    .append("        <CdtrAcct><Id><Othr><Id>").append(esc(e.accountNumber)).append("</Id></Othr></Id></CdtrAcct>\n")
                    .append("        <RmtInf><Ustrd>Salary ").append(req.disbursementDate).append("</Ustrd></RmtInf>\n")
                    .append("      </CdtTrfTxInf>\n");
        }
        body.append("    </PmtInf>\n  </CstmrCdtTrfInitn>\n</Document>\n");
        return body.toString();
    }

    private static BigDecimal totalAmount(List<Entry> entries) {
        BigDecimal t = BigDecimal.ZERO;
        for (Entry e : entries) t = t.add(e.amount);
        return t.setScale(2, RoundingMode.HALF_UP);
    }

    private static String csv(String s) {
        if (s == null) return "";
        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
    private static String esc(String s) { return s == null ? "" : s.replace("<","&lt;").replace(">","&gt;").replace("&","&amp;"); }

    public enum Format { NEFT, RTGS, ACH, ISO20022_PAIN_001 }

    public static class BankFileRequest {
        public UUID payrollRunId;
        public Format format = Format.NEFT;
        public LocalDate disbursementDate;
        public String currency = "INR";
        public String companyName;
        public String companyAccountNumber;
        public String utilityCode;        // NACH utility code
        public List<Entry> entries = new ArrayList<>();
    }

    public static class Entry {
        public UUID employeeId;
        public String accountHolder;
        public String accountNumber;
        public String ifscCode;
        public String micr;
        public String bic;                // SWIFT BIC for cross-border
        public String beneficiaryAddress;
        public String email;
        public BigDecimal amount;
    }
}
