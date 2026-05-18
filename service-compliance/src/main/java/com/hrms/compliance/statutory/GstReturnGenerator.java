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
import java.util.Map;

/**
 * Generates GSTR-1 (outward supplies) and GSTR-3B (monthly summary) returns in the
 * JSON format accepted by the GST portal's offline tool / API. HRMS isn't a sales
 * system, but where the platform issues reimbursable invoices (e.g. for outsourced
 * payroll services, partner training, FBP vendor passthroughs) we need to file them.
 *
 * Output structure mirrors the GSTN schema v3.1 (subset — only B2B, NIL_RATED, EXEMPT
 * sections used by HR contexts).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GstReturnGenerator {

    private final StorageService storage;

    public String generateGstr1(String tenantId, Gstr1Request req) {
        String json = """
            {
              "gstin": "%s",
              "fp": "%s",
              "gt": %s,
              "cur_gt": %s,
              "b2b": %s,
              "nil": [
                { "sply_ty": "INTRB2B", "expt_amt": 0, "nil_amt": %s, "ngsup_amt": 0 }
              ]
            }
            """.formatted(req.gstin(), req.period(), req.aggregateTurnover(), req.currentTurnover(),
                          buildB2bJson(req.b2bInvoices()), req.nilRatedAmount());
        return persist(tenantId, "GSTR1_" + req.period(), json);
    }

    public String generateGstr3b(String tenantId, Gstr3bRequest req) {
        String json = """
            {
              "gstin": "%s",
              "ret_period": "%s",
              "sup_details": {
                "osup_det": { "txval": %s, "iamt": %s, "camt": %s, "samt": %s, "csamt": 0 },
                "osup_zero": { "txval": 0, "iamt": 0, "csamt": 0 },
                "osup_nil_exmp": { "txval": %s },
                "isup_rev": { "txval": 0, "iamt": 0, "camt": 0, "samt": 0, "csamt": 0 },
                "osup_nongst": { "txval": 0 }
              },
              "inter_sup": { "unreg_details": [], "comp_details": [], "uin_details": [] },
              "itc_elg": {
                "itc_avl": [ { "ty": "OTH", "iamt": %s, "camt": %s, "samt": %s, "csamt": 0 } ],
                "itc_inelg": [], "itc_rev": [], "itc_net": {}
              },
              "inward_sup": { "isup_details": [] },
              "tx_pmt": {
                "tax_pmt": [ { "ty": "OTH", "iamt": %s, "camt": %s, "samt": %s, "csamt": 0, "fee": 0, "intr": 0 } ]
              }
            }
            """.formatted(req.gstin(), req.period(),
                          req.outwardTaxableValue(), req.igstOutward(), req.cgstOutward(), req.sgstOutward(),
                          req.nilExemptOutward(),
                          req.igstItc(), req.cgstItc(), req.sgstItc(),
                          req.igstPayable(), req.cgstPayable(), req.sgstPayable());
        return persist(tenantId, "GSTR3B_" + req.period(), json);
    }

    private String buildB2bJson(List<B2bInvoice> rows) {
        if (rows == null || rows.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (B2bInvoice r : rows) {
            if (!first) sb.append(',');
            first = false;
            sb.append("""
                {
                  "ctin": "%s",
                  "inv": [{
                    "inum": "%s", "idt": "%s", "val": %s,
                    "pos": "%s", "rchrg": "N", "inv_typ": "R",
                    "itms": [{
                      "num": 1, "itm_det": {
                        "rt": %s, "txval": %s, "iamt": %s, "camt": %s, "samt": %s, "csamt": 0
                      }
                    }]
                  }]
                }""".formatted(r.recipientGstin(), r.invoiceNumber(), r.invoiceDate(), r.invoiceValue(),
                               r.placeOfSupply(), r.taxRate(), r.taxableValue(),
                               r.igst(), r.cgst(), r.sgst()));
        }
        sb.append(']');
        return sb.toString();
    }

    private String persist(String tenantId, String fileBase, String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        StoredFile sf = storage.upload(tenantId, "statutory/gst",
                fileBase + ".json", "application/json",
                new ByteArrayInputStream(bytes), bytes.length);
        log.info("GST return generated → {}", sf.getStorageUri());
        return sf.getStorageUri();
    }

    public record Gstr1Request(String gstin, String period,
                               BigDecimal aggregateTurnover, BigDecimal currentTurnover,
                               List<B2bInvoice> b2bInvoices,
                               BigDecimal nilRatedAmount) {}

    public record Gstr3bRequest(String gstin, String period,
                                BigDecimal outwardTaxableValue,
                                BigDecimal igstOutward, BigDecimal cgstOutward, BigDecimal sgstOutward,
                                BigDecimal nilExemptOutward,
                                BigDecimal igstItc, BigDecimal cgstItc, BigDecimal sgstItc,
                                BigDecimal igstPayable, BigDecimal cgstPayable, BigDecimal sgstPayable) {}

    public record B2bInvoice(String recipientGstin, String invoiceNumber, String invoiceDate,
                              BigDecimal invoiceValue, String placeOfSupply, BigDecimal taxRate,
                              BigDecimal taxableValue, BigDecimal igst, BigDecimal cgst, BigDecimal sgst,
                              Map<String, Object> extra) {}
}
