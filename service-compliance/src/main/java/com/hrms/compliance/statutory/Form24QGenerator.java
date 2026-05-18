package com.hrms.compliance.statutory;

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
 * Form 24Q (TDS quarterly return) text file generator. Output is the FVU input format
 * accepted by the NSDL Return Preparation Utility (RPU). Includes Annexure I (deductee-wise
 * payment + TDS details for each month of the quarter) plus the file header (FH/BH records).
 *
 * Structure (^ delimited):
 *   File Header: FH ^ filename ^ uploadType ^ ...
 *   Batch Header: BH ^ TAN ^ FY ^ quarter ^ formType=24Q ^ ...
 *   Challan rows: CD ^ challanSequenceNo ^ amount ^ totalTax ^ ...
 *   Deductee rows: DD ^ employeeSerial ^ PAN ^ name ^ section=92B ^ taxableAmount ^ tdsAmount ^ ...
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Form24QGenerator {

    private static final String SEP = "^";

    private final StorageService storage;

    public String generateAndStore(String tenantId, Form24QRequest req) {
        StringBuilder sb = new StringBuilder();

        // File Header
        sb.append("FH").append(SEP).append("24Q_").append(req.fy()).append("_Q").append(req.quarter())
          .append(SEP).append("Regular").append(SEP).append(req.tan()).append("\n");

        // Batch Header
        BigDecimal totalTds = req.deductees().stream()
                .map(DeducteeRow::tdsAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTaxable = req.deductees().stream()
                .map(DeducteeRow::taxableAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        sb.append("BH").append(SEP).append(req.tan())
          .append(SEP).append(req.fy()).append(SEP).append(req.quarter())
          .append(SEP).append("24Q").append(SEP).append(req.responsibleName() == null ? "" : req.responsibleName())
          .append(SEP).append(req.responsiblePan() == null ? "" : req.responsiblePan())
          .append(SEP).append(totalTaxable.setScale(2)).append(SEP).append(totalTds.setScale(2))
          .append("\n");

        // Challan rows
        int challanSeq = 1;
        for (ChallanRow c : req.challans()) {
            sb.append("CD").append(SEP).append(challanSeq++)
              .append(SEP).append(c.bsrCode())
              .append(SEP).append(c.depositDate())
              .append(SEP).append(c.challanSerial())
              .append(SEP).append(c.amount().setScale(2))
              .append("\n");
        }

        // Deductee rows
        int deducteeSeq = 1;
        for (DeducteeRow d : req.deductees()) {
            sb.append("DD").append(SEP).append(deducteeSeq++)
              .append(SEP).append(d.employeePan() == null ? "" : d.employeePan())
              .append(SEP).append(d.employeeName() == null ? "" : d.employeeName())
              .append(SEP).append("92B")                            // section 92B = salaries
              .append(SEP).append(d.taxableAmount().setScale(2))
              .append(SEP).append(d.tdsAmount().setScale(2))
              .append(SEP).append(d.deductionDate())
              .append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.US_ASCII);
        String filename = "Form24Q_" + req.fy() + "_Q" + req.quarter() + ".txt";
        StoredFile sf = storage.upload(tenantId, "statutory/form24q/" + req.fy(),
                filename, "text/plain", new ByteArrayInputStream(bytes), bytes.length);
        log.info("Form 24Q generated for FY {} Q{} → {} deductees, ₹{} total TDS",
                req.fy(), req.quarter(), req.deductees().size(), totalTds);
        return sf.getStorageUri();
    }

    public record Form24QRequest(String fy, int quarter, String tan,
                                  String responsibleName, String responsiblePan,
                                  List<ChallanRow> challans, List<DeducteeRow> deductees) {}
    public record ChallanRow(String bsrCode, String depositDate, String challanSerial, BigDecimal amount) {}
    public record DeducteeRow(String employeePan, String employeeName,
                              BigDecimal taxableAmount, BigDecimal tdsAmount,
                              String deductionDate) {}
}
