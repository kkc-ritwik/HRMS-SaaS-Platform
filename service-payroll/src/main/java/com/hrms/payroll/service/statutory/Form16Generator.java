package com.hrms.payroll.service.statutory;

import com.hrms.pdf.service.PdfService;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Generates a simplified Form 16 (Part A + Part B summary) PDF and stores it.
 * NOTE: This is the printable salary-and-TDS summary that employees use as proof
 * of TDS deducted by the employer. A fully NSDL-compliant Form 16 requires the
 * TAN-issued Part A from the NSDL portal — this stores the Part B (provided by employer).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form16Generator {

    private final PdfService pdfService;
    private final StorageService storage;

    public String generateAndStore(Form16Data d) {
        String html = """
            <html><body style='font-family:Helvetica,Arial,sans-serif;font-size:10px;color:#111'>
              <h2 style='margin:0;text-align:center'>FORM 16 — PART B</h2>
              <p style='text-align:center;margin:4px 0 12px 0'>
                Certificate of Tax Deducted at Source<br/>
                Assessment Year <strong>{{ay}}</strong> &nbsp; Financial Year <strong>{{fy}}</strong>
              </p>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td><strong>Employee Name</strong></td><td>{{employeeName}}</td>
                    <td><strong>PAN</strong></td><td>{{pan}}</td></tr>
                <tr><td><strong>Employee Code</strong></td><td>{{employeeCode}}</td>
                    <td><strong>Period</strong></td><td>{{periodFrom}} to {{periodTo}}</td></tr>
                <tr><td><strong>Employer</strong></td><td colspan='3'>{{employerName}} (TAN {{tan}})</td></tr>
              </table>
              <h3 style='margin-top:18px'>1. Gross Salary</h3>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td>(a) Salary as per Section 17(1)</td><td style='text-align:right'>{{grossSalary}}</td></tr>
                <tr><td>(b) Perquisites under Section 17(2)</td><td style='text-align:right'>{{perks}}</td></tr>
                <tr><td>(c) Profits in lieu of salary u/s 17(3)</td><td style='text-align:right'>{{profitsInLieu}}</td></tr>
                <tr><td><strong>Total</strong></td><td style='text-align:right'><strong>{{totalSalary}}</strong></td></tr>
              </table>
              <h3>2. Less: Allowances Exempt u/s 10</h3>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td>HRA exempt u/s 10(13A)</td><td style='text-align:right'>{{hraExempt}}</td></tr>
                <tr><td>LTA exempt u/s 10(5)</td><td style='text-align:right'>{{ltaExempt}}</td></tr>
                <tr><td>Standard Deduction u/s 16(ia)</td><td style='text-align:right'>{{standardDeduction}}</td></tr>
                <tr><td>Professional Tax u/s 16(iii)</td><td style='text-align:right'>{{ptTotal}}</td></tr>
              </table>
              <h3>3. Income Chargeable Under Salary</h3>
              <p style='font-size:14px'>Net taxable salary: <strong>{{netTaxable}}</strong></p>
              <h3>4. Deductions under Chapter VI-A</h3>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td>80C (PF, ELSS, LIC etc.)</td><td style='text-align:right'>{{deduction80c}}</td></tr>
                <tr><td>80D (Mediclaim)</td><td style='text-align:right'>{{deduction80d}}</td></tr>
                <tr><td>80E (Education Loan)</td><td style='text-align:right'>{{deduction80e}}</td></tr>
                <tr><td>24(b) (Housing Loan Interest)</td><td style='text-align:right'>{{section24b}}</td></tr>
              </table>
              <h3>5. Tax Payable</h3>
              <table style='border-collapse:collapse;width:100%;border:1px solid #aaa'>
                <tr><td>Total income</td><td style='text-align:right'>{{totalIncome}}</td></tr>
                <tr><td>Tax on income</td><td style='text-align:right'>{{taxOnIncome}}</td></tr>
                <tr><td>Rebate u/s 87A</td><td style='text-align:right'>{{rebate87a}}</td></tr>
                <tr><td>Surcharge</td><td style='text-align:right'>{{surcharge}}</td></tr>
                <tr><td>Health &amp; Education Cess (4%)</td><td style='text-align:right'>{{cess}}</td></tr>
                <tr><td><strong>Total Tax Payable</strong></td><td style='text-align:right'><strong>{{totalTax}}</strong></td></tr>
                <tr><td>Total TDS Deducted &amp; Deposited</td><td style='text-align:right'><strong>{{tdsDeposited}}</strong></td></tr>
              </table>
              <p style='font-size:8px;color:#666;margin-top:16px'>
                This is a system-generated document for employee reference only.
                Part A (with TAN-issued data) must be downloaded from TRACES portal.
              </p>
            </body></html>
            """;

        byte[] pdf = pdfService.renderTemplate(html, asMap(d));
        pdf = pdfService.stampHeaderFooter(pdf, "Form 16 — " + d.employeeName(),
                "Confidential — AY " + d.ay());

        String filename = "form16-" + d.employeeCode() + "-" + d.fy() + ".pdf";
        StoredFile sf = storage.upload(d.tenantId(),
                "tax/form16/" + d.fy(),
                filename, "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);
        return sf.getStorageUri();
    }

    private Map<String, Object> asMap(Form16Data d) {
        return Map.ofEntries(
                Map.entry("ay", s(d.ay())), Map.entry("fy", s(d.fy())),
                Map.entry("employeeName", s(d.employeeName())),
                Map.entry("employeeCode", s(d.employeeCode())),
                Map.entry("pan", s(d.pan())),
                Map.entry("employerName", s(d.employerName())),
                Map.entry("tan", s(d.tan())),
                Map.entry("periodFrom", s(d.periodFrom())),
                Map.entry("periodTo", s(d.periodTo())),
                Map.entry("grossSalary", s(d.grossSalary())),
                Map.entry("perks", s(d.perks())),
                Map.entry("profitsInLieu", s(d.profitsInLieu())),
                Map.entry("totalSalary", s(d.totalSalary())),
                Map.entry("hraExempt", s(d.hraExempt())),
                Map.entry("ltaExempt", s(d.ltaExempt())),
                Map.entry("standardDeduction", s(d.standardDeduction())),
                Map.entry("ptTotal", s(d.ptTotal())),
                Map.entry("netTaxable", s(d.netTaxable())),
                Map.entry("deduction80c", s(d.deduction80c())),
                Map.entry("deduction80d", s(d.deduction80d())),
                Map.entry("deduction80e", s(d.deduction80e())),
                Map.entry("section24b", s(d.section24b())),
                Map.entry("totalIncome", s(d.totalIncome())),
                Map.entry("taxOnIncome", s(d.taxOnIncome())),
                Map.entry("rebate87a", s(d.rebate87a())),
                Map.entry("surcharge", s(d.surcharge())),
                Map.entry("cess", s(d.cess())),
                Map.entry("totalTax", s(d.totalTax())),
                Map.entry("tdsDeposited", s(d.tdsDeposited()))
        );
    }

    private String s(Object o) { return o == null ? "" : o.toString(); }

    public record Form16Data(
            String tenantId, String fy, String ay,
            String employeeName, String employeeCode, String pan,
            String employerName, String tan,
            String periodFrom, String periodTo,
            BigDecimal grossSalary, BigDecimal perks, BigDecimal profitsInLieu, BigDecimal totalSalary,
            BigDecimal hraExempt, BigDecimal ltaExempt, BigDecimal standardDeduction, BigDecimal ptTotal,
            BigDecimal netTaxable,
            BigDecimal deduction80c, BigDecimal deduction80d, BigDecimal deduction80e, BigDecimal section24b,
            BigDecimal totalIncome, BigDecimal taxOnIncome, BigDecimal rebate87a,
            BigDecimal surcharge, BigDecimal cess, BigDecimal totalTax, BigDecimal tdsDeposited) {}
}
