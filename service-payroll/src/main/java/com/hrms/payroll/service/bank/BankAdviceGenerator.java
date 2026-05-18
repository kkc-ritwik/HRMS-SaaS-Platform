package com.hrms.payroll.service.bank;

import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates bulk salary disbursement files in the formats expected by Indian banks
 * (HDFC, ICICI, Axis). Defaults to HDFC corporate NetBanking CSV format; switchable
 * via `format`.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankAdviceGenerator {

    private final StorageService storage;

    public String generateAndStore(String tenantId, BankAdvice advice) {
        return switch (advice.format()) {
            case "HDFC"  -> hdfcCsv(tenantId, advice);
            case "ICICI" -> iciciCsv(tenantId, advice);
            case "AXIS"  -> axisCsv(tenantId, advice);
            case "SBI"   -> sbiCsv(tenantId, advice);
            default      -> genericCsv(tenantId, advice);
        };
    }

    private String hdfcCsv(String tenant, BankAdvice a) {
        StringBuilder sb = new StringBuilder("PYMT_PROD_CODE,DEBIT_ACC_NO,BNF_NAME,BNF_ACC_NO,BNF_BANK_CODE,BNF_BRANCH_CODE,IFSC_CODE,AMOUNT,DEBIT_NARR,CREDIT_NARR,MOBILE_NUM,EMAIL_ID,REMARKS\n");
        for (Payment p : a.payments()) {
            sb.append("NEFT,").append(a.debitAccount()).append(',')
              .append(esc(p.beneficiaryName())).append(',')
              .append(p.beneficiaryAccount()).append(',').append(',').append(',')
              .append(p.ifsc()).append(',').append(p.amount().setScale(2)).append(',')
              .append("Salary ").append(a.month()).append('/').append(a.year()).append(',')
              .append("Salary ").append(a.month()).append('/').append(a.year()).append(',')
              .append(esc(p.mobile())).append(',').append(esc(p.email())).append(',')
              .append("EMP-").append(p.employeeCode()).append('\n');
        }
        return persist(tenant, a, sb.toString(), "csv");
    }

    private String iciciCsv(String tenant, BankAdvice a) {
        StringBuilder sb = new StringBuilder("Beneficiary Name,Beneficiary Account No,IFSC,Amount,Narration\n");
        for (Payment p : a.payments())
            sb.append(esc(p.beneficiaryName())).append(',')
              .append(p.beneficiaryAccount()).append(',').append(p.ifsc()).append(',')
              .append(p.amount().setScale(2)).append(',')
              .append("Salary-").append(a.month()).append('-').append(a.year()).append('\n');
        return persist(tenant, a, sb.toString(), "csv");
    }

    private String axisCsv(String tenant, BankAdvice a) {
        StringBuilder sb = new StringBuilder("BeneficiaryAccount,BeneficiaryName,IFSC,Amount,TransactionType,Remarks\n");
        for (Payment p : a.payments())
            sb.append(p.beneficiaryAccount()).append(',').append(esc(p.beneficiaryName())).append(',')
              .append(p.ifsc()).append(',').append(p.amount().setScale(2)).append(',')
              .append("NEFT,Salary ").append(a.month()).append('/').append(a.year()).append('\n');
        return persist(tenant, a, sb.toString(), "csv");
    }

    private String sbiCsv(String tenant, BankAdvice a) {
        StringBuilder sb = new StringBuilder("Sl,Beneficiary Name,A/c No,Amount,IFSC,Remarks\n");
        int sl = 1;
        for (Payment p : a.payments())
            sb.append(sl++).append(',').append(esc(p.beneficiaryName())).append(',')
              .append(p.beneficiaryAccount()).append(',').append(p.amount().setScale(2)).append(',')
              .append(p.ifsc()).append(',').append("SAL").append(a.month()).append(a.year()).append('\n');
        return persist(tenant, a, sb.toString(), "csv");
    }

    private String genericCsv(String tenant, BankAdvice a) {
        StringBuilder sb = new StringBuilder("EmployeeCode,Name,Account,IFSC,Amount\n");
        for (Payment p : a.payments())
            sb.append(p.employeeCode()).append(',').append(esc(p.beneficiaryName())).append(',')
              .append(p.beneficiaryAccount()).append(',').append(p.ifsc()).append(',')
              .append(p.amount().setScale(2)).append('\n');
        return persist(tenant, a, sb.toString(), "csv");
    }

    private String persist(String tenant, BankAdvice a, String body, String ext) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        String filename = "bankadvice-" + a.format().toLowerCase() + "-"
                + a.year() + "-" + a.month() + "-"
                + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()) + "." + ext;
        StoredFile sf = storage.upload(tenant, "bank-advice/" + a.year(),
                filename, "text/csv", new ByteArrayInputStream(bytes), bytes.length);
        log.info("Bank advice {} generated for {}/{}: {} payments → {}",
                a.format(), a.month(), a.year(), a.payments().size(), sf.getStorageUri());
        return sf.getStorageUri();
    }

    private String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    public record BankAdvice(String format, int month, int year,
                             String debitAccount, List<Payment> payments) {}
    public record Payment(String employeeCode, String beneficiaryName, String beneficiaryAccount,
                          String ifsc, BigDecimal amount, String mobile, String email) {}
}
