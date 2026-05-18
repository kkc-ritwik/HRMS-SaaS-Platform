package com.hrms.payroll.service.statutory;

import com.hrms.storage.model.StoredFile;
import com.hrms.storage.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * EPFO Electronic Challan-cum-Return (ECR v2.0) text file generator.
 * Format (one row per member, fields delimited by `#~#`):
 *   UAN # Member Name # Gross # EPF wages # EPS wages # EDLI wages
 *     # EE PF (12%) # ER PF (3.67%) # ER Pension (8.33%) # NCP days # Refund
 * The EPFO portal accepts this file for monthly PF compliance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpfoEcrGenerator {

    private static final String SEP = "#~#";

    private final StorageService storage;

    public String generateAndStore(String tenantId, String month, String year, List<EcrLine> lines) {
        StringBuilder sb = new StringBuilder();
        for (EcrLine l : lines) {
            sb.append(safe(l.uan())).append(SEP)
              .append(safe(l.memberName())).append(SEP)
              .append(fmt(l.grossWages())).append(SEP)
              .append(fmt(l.epfWages())).append(SEP)
              .append(fmt(l.epsWages())).append(SEP)
              .append(fmt(l.edliWages())).append(SEP)
              .append(fmt(l.eePfContribution())).append(SEP)
              .append(fmt(l.erPfContribution())).append(SEP)
              .append(fmt(l.erPensionContribution())).append(SEP)
              .append(l.ncpDays()).append(SEP)
              .append(fmt(l.refundOfAdvances()))
              .append("\n");
        }
        byte[] bytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
        String filename = "ecr-" + year + "-" + month + ".txt";
        StoredFile sf = storage.upload(tenantId, "epfo-ecr/" + year, filename,
                "text/plain", new ByteArrayInputStream(bytes), bytes.length);
        log.info("ECR file generated: {} lines → {}", lines.size(), sf.getStorageUri());
        return sf.getStorageUri();
    }

    private String safe(String v) { return v == null ? "" : v.replaceAll("[#~]", " "); }
    private String fmt(BigDecimal v) { return v == null ? "0" : v.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString(); }

    public record EcrLine(String uan, String memberName,
                          BigDecimal grossWages, BigDecimal epfWages,
                          BigDecimal epsWages, BigDecimal edliWages,
                          BigDecimal eePfContribution, BigDecimal erPfContribution,
                          BigDecimal erPensionContribution,
                          Integer ncpDays, BigDecimal refundOfAdvances) {}
}
