package com.hrms.payroll.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.PayslipDto;
import com.hrms.payroll.entity.Payslip;
import com.hrms.payroll.entity.PayslipLineItem;
import com.hrms.payroll.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayslipService {

    private final PayslipRepository payslipRepository;

    // ── Employee self-service ─────────────────────────────────────────────────

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

    // ── PDF generation (placeholder) ─────────────────────────────────────────

    /**
     * Returns a signed URL / base64 PDF for the payslip.
     * Real implementation would use a PDF library (e.g. iText, Apache PDFBox)
     * or an async job that uploads to object storage and returns the URL.
     */
    public String generatePayslipPdf(String tenantId, UUID payslipId) {
        Payslip p = payslipRepository.findByIdAndTenantIdAndDeletedFalse(payslipId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Payslip", "id", payslipId));
        if (p.getPdfUrl() != null) {
            return p.getPdfUrl();
        }
        // TODO: generate PDF, upload to storage, set p.setPdfUrl(...), save, return URL
        log.warn("PDF generation not yet implemented for payslip={}", payslipId);
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private PayslipDto.Response toResponse(Payslip p) {
        List<PayslipLineItem> components = p.getComponentsJson();
        return PayslipDto.Response.builder()
                .id(p.getId())
                .payrollRunId(p.getPayrollRunId())
                .employeeId(p.getEmployeeId())
                .month(p.getMonth())
                .year(p.getYear())
                .daysInMonth(p.getDaysInMonth())
                .daysPayable(p.getDaysPayable())
                .daysWorked(p.getDaysWorked())
                .lopDays(p.getLopDays())
                .grossEarnings(p.getGrossEarnings())
                .totalDeductions(p.getTotalDeductions())
                .netPay(p.getNetPay())
                .employeePf(p.getEmployeePf())
                .employeeEsi(p.getEmployeeEsi())
                .employeePt(p.getEmployeePt())
                .tds(p.getTds())
                .employerPf(p.getEmployerPf())
                .employerEsi(p.getEmployerEsi())
                .employerLwf(p.getEmployerLwf())
                .components(components)
                .status(p.getStatus())
                .pdfUrl(p.getPdfUrl())
                .emailedAt(p.getEmailedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PayslipDto.Summary toSummary(Payslip p) {
        return PayslipDto.Summary.builder()
                .id(p.getId())
                .month(p.getMonth())
                .year(p.getYear())
                .grossEarnings(p.getGrossEarnings())
                .netPay(p.getNetPay())
                .tds(p.getTds())
                .status(p.getStatus())
                .pdfUrl(p.getPdfUrl())
                .build();
    }
}
