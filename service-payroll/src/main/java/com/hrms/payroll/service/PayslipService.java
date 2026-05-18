package com.hrms.payroll.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.mail.model.MailAttachment;
import com.hrms.mail.model.MailRequest;
import com.hrms.mail.service.MailService;
import com.hrms.payroll.dto.PayslipDto;
import com.hrms.payroll.entity.Payslip;
import com.hrms.payroll.entity.PayslipLineItem;
import com.hrms.payroll.repository.PayslipRepository;
import com.hrms.pdf.service.PdfService;
import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipService {

    private final PayslipRepository payslipRepository;
    private final PdfService pdfService;
    private final StorageService storage;
    private final MailService mailService;

    @Transactional(readOnly = true)
    public Page<PayslipDto.Summary> getMyPayslips(String tenantId, UUID employeeId, Pageable pageable) {
        return payslipRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalseOrderByYearDescMonthDesc(
                        tenantId, employeeId, pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public PayslipDto.Response getPayslipById(String tenantId, UUID payslipId) {
        Payslip p = payslipRepository.findByIdAndTenantIdAndDeletedFalse(payslipId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", payslipId));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PayslipDto.Response> getRunPayslips(String tenantId, UUID runId) {
        return payslipRepository.findByPayrollRunIdAndTenantIdAndDeletedFalse(runId, tenantId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Generates a PDF payslip, persists it to object storage, and stores the storage URI on the payslip.
     * Idempotent — returns existing URL if already generated.
     */
    @Transactional
    public String generatePayslipPdf(String tenantId, UUID payslipId) {
        Payslip p = payslipRepository.findByIdAndTenantIdAndDeletedFalse(payslipId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", payslipId));
        if (p.getPdfUrl() != null && !p.getPdfUrl().isBlank()) {
            return p.getPdfUrl();
        }

        byte[] pdf = pdfService.renderTemplate(payslipHtmlTemplate(), buildVars(p));
        pdf = pdfService.stampHeaderFooter(pdf,
                "HRMS Payslip — " + p.getMonth() + "/" + p.getYear(),
                "Confidential — generated " + OffsetDateTime.now().toLocalDate());

        String filename = "payslip-" + p.getEmployeeId() + "-" + p.getYear() + "-" + p.getMonth() + ".pdf";
        StoredFile stored = storage.upload(tenantId, "payslips/" + p.getYear() + "/" + p.getMonth(),
                filename, "application/pdf",
                new ByteArrayInputStream(pdf), pdf.length);

        p.setPdfUrl(stored.getStorageUri());
        payslipRepository.save(p);
        return stored.getStorageUri();
    }

    /**
     * Emails the payslip PDF to the employee's email address. Generates the PDF if needed.
     */
    @Transactional
    public void emailPayslip(String tenantId, UUID payslipId, String employeeEmail, String employeeName) {
        Payslip p = payslipRepository.findByIdAndTenantIdAndDeletedFalse(payslipId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", payslipId));

        String uri = (p.getPdfUrl() == null) ? generatePayslipPdf(tenantId, payslipId) : p.getPdfUrl();
        byte[] pdf;
        try (var in = storage.download(uri)) { pdf = in.readAllBytes(); }
        catch (Exception e) { throw new RuntimeException("Failed to fetch payslip PDF", e); }

        mailService.send(MailRequest.builder()
                .to(employeeEmail)
                .subject("Your payslip for " + p.getMonth() + "/" + p.getYear())
                .html("""
                      <p>Hi %s,</p>
                      <p>Please find your payslip for <strong>%d/%d</strong> attached.</p>
                      <p>Net pay: <strong>%s</strong></p>
                      <p>Regards,<br/>HR Team</p>
                      """.formatted(employeeName == null ? "" : employeeName,
                                    p.getMonth(), p.getYear(), p.getNetPay()))
                .attachment(MailAttachment.builder()
                        .filename("payslip-" + p.getYear() + "-" + p.getMonth() + ".pdf")
                        .contentType("application/pdf").content(pdf).build())
                .category("payroll-payslip")
                .build());

        p.setEmailedAt(Instant.now());
        payslipRepository.save(p);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private Map<String, Object> buildVars(Payslip p) {
        StringBuilder earnings = new StringBuilder();
        StringBuilder deductions = new StringBuilder();
        if (p.getComponentsJson() != null) {
            for (PayslipLineItem li : p.getComponentsJson()) {
                String row = "<tr><td>%s</td><td style='text-align:right'>%s</td></tr>"
                        .formatted(li.getName(), li.getAmount());
                if ("EARNING".equalsIgnoreCase(li.getType())) earnings.append(row);
                else if ("DEDUCTION".equalsIgnoreCase(li.getType())) deductions.append(row);
            }
        }
        return Map.ofEntries(
                Map.entry("month", String.valueOf(p.getMonth())),
                Map.entry("year", String.valueOf(p.getYear())),
                Map.entry("daysPayable", String.valueOf(p.getDaysPayable())),
                Map.entry("daysWorked", String.valueOf(p.getDaysWorked())),
                Map.entry("lopDays", String.valueOf(p.getLopDays())),
                Map.entry("grossEarnings", String.valueOf(p.getGrossEarnings())),
                Map.entry("totalDeductions", String.valueOf(p.getTotalDeductions())),
                Map.entry("netPay", String.valueOf(p.getNetPay())),
                Map.entry("tds", String.valueOf(p.getTds())),
                Map.entry("earningsRows", earnings.toString()),
                Map.entry("deductionsRows", deductions.toString())
        );
    }

    private String payslipHtmlTemplate() {
        return """
            <html><body style='font-family:Helvetica,Arial,sans-serif;font-size:11px;color:#111'>
              <h2 style='margin:0 0 8px 0'>Payslip — {{month}}/{{year}}</h2>
              <table style='border-collapse:collapse;width:100%;margin-bottom:12px'>
                <tr><td>Days Payable</td><td style='text-align:right'>{{daysPayable}}</td>
                    <td>Days Worked</td><td style='text-align:right'>{{daysWorked}}</td>
                    <td>LOP</td><td style='text-align:right'>{{lopDays}}</td></tr>
              </table>
              <table style='border-collapse:collapse;width:100%;border:1px solid #ddd'>
                <tr style='background:#f3f4f6'><th align='left'>Earnings</th><th align='right'>Amount</th></tr>
                {{earningsRows}}
                <tr><td><strong>Gross Earnings</strong></td><td style='text-align:right'><strong>{{grossEarnings}}</strong></td></tr>
              </table>
              <table style='border-collapse:collapse;width:100%;border:1px solid #ddd;margin-top:8px'>
                <tr style='background:#f3f4f6'><th align='left'>Deductions</th><th align='right'>Amount</th></tr>
                {{deductionsRows}}
                <tr><td><strong>Total Deductions</strong></td><td style='text-align:right'><strong>{{totalDeductions}}</strong></td></tr>
              </table>
              <h3 style='text-align:right'>Net Pay: {{netPay}}</h3>
              <p style='font-size:9px;color:#666'>This payslip is system-generated. TDS deducted: {{tds}}.</p>
            </body></html>
            """;
    }

    private PayslipDto.Response toResponse(Payslip p) {
        List<PayslipLineItem> components = p.getComponentsJson();
        return PayslipDto.Response.builder()
                .id(p.getId()).payrollRunId(p.getPayrollRunId()).employeeId(p.getEmployeeId())
                .month(p.getMonth()).year(p.getYear())
                .daysInMonth(p.getDaysInMonth()).daysPayable(p.getDaysPayable())
                .daysWorked(p.getDaysWorked()).lopDays(p.getLopDays())
                .grossEarnings(p.getGrossEarnings()).totalDeductions(p.getTotalDeductions())
                .netPay(p.getNetPay())
                .employeePf(p.getEmployeePf()).employeeEsi(p.getEmployeeEsi()).employeePt(p.getEmployeePt())
                .tds(p.getTds())
                .employerPf(p.getEmployerPf()).employerEsi(p.getEmployerEsi()).employerLwf(p.getEmployerLwf())
                .components(components).status(p.getStatus())
                .pdfUrl(p.getPdfUrl()).emailedAt(p.getEmailedAt()).createdAt(p.getCreatedAt())
                .build();
    }

    private PayslipDto.Summary toSummary(Payslip p) {
        return PayslipDto.Summary.builder()
                .id(p.getId()).month(p.getMonth()).year(p.getYear())
                .grossEarnings(p.getGrossEarnings()).netPay(p.getNetPay()).tds(p.getTds())
                .status(p.getStatus()).pdfUrl(p.getPdfUrl())
                .build();
    }
}
