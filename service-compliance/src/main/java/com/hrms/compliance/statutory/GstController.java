package com.hrms.compliance.statutory;

import com.hrms.compliance.report.ComplianceReportGenerator;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/compliance")
@RequiredArgsConstructor
public class GstController {

    private final GstReturnGenerator gst;
    private final ComplianceReportGenerator reportGen;

    @PostMapping("/gst/gstr1")
    public Map<String, String> gstr1(@RequestBody GstReturnGenerator.Gstr1Request req) {
        return Map.of("storageUri", gst.generateGstr1(TenantContext.get(), req));
    }

    @PostMapping("/gst/gstr3b")
    public Map<String, String> gstr3b(@RequestBody GstReturnGenerator.Gstr3bRequest req) {
        return Map.of("storageUri", gst.generateGstr3b(TenantContext.get(), req));
    }

    @PostMapping("/reports/audit")
    public Map<String, String> auditReport(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                            @RequestParam(required = false, defaultValue = "SOC2") String framework) {
        return Map.of("storageUri", reportGen.generate(TenantContext.get(), from, to, framework));
    }
}
