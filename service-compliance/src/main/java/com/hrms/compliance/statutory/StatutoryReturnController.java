package com.hrms.compliance.statutory;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/compliance/statutory-returns")
@RequiredArgsConstructor
public class StatutoryReturnController {

    private final Form24QGenerator form24Q;

    /** Generate Form 24Q quarterly TDS return — returns the storage URI of the FVU text file. */
    @PostMapping("/form24q")
    public Map<String, String> generate24Q(@RequestBody Form24QGenerator.Form24QRequest req) {
        String uri = form24Q.generateAndStore(TenantContext.get(), req);
        return Map.of("storageUri", uri, "form", "24Q",
                "fy", req.fy(), "quarter", String.valueOf(req.quarter()));
    }
}
