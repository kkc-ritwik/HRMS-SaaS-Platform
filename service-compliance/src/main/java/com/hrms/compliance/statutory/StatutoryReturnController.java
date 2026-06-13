package com.hrms.compliance.statutory;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/compliance/statutory-returns")
@RequiredArgsConstructor
public class StatutoryReturnController {

    private final Form24QGenerator form24Q;
    private final StatutoryReturnRepository repository;

    /** Generate Form 24Q quarterly TDS return — returns the storage URI of the FVU text file. */
    @PostMapping("/form24q")
    public Map<String, String> generate24Q(@RequestBody Form24QGenerator.Form24QRequest req) {
        String uri = form24Q.generateAndStore(TenantContext.get(), req);
        return Map.of("storageUri", uri, "form", "24Q",
                "fy", req.fy(), "quarter", String.valueOf(req.quarter()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StatutoryReturn>>> list(
            @RequestParam(value = "returnType", required = false) StatutoryReturn.ReturnType returnType) {
        String tenantId = TenantContext.get();
        List<StatutoryReturn> returns = returnType != null
                ? repository.findByTenantIdAndReturnTypeAndDeletedFalseOrderByCreatedAtDesc(tenantId, returnType)
                : repository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(returns));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StatutoryReturn>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(find(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StatutoryReturn>> create(@RequestBody StatutoryReturn body) {
        body.setTenantId(TenantContext.get());
        body.setCreatedBy(currentUserId());
        if (body.getStatus() == null) body.setStatus(StatutoryReturn.Status.DRAFT);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(repository.save(body)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<StatutoryReturn>> submit(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        StatutoryReturn sr = find(id);
        sr.setStatus(StatutoryReturn.Status.SUBMITTED);
        sr.setSubmittedAt(OffsetDateTime.now());
        sr.setUpdatedBy(currentUserId());
        if (body != null && body.get("acknowledgementNumber") != null) {
            sr.setAcknowledgementNumber(body.get("acknowledgementNumber"));
        }
        return ResponseEntity.ok(ApiResponse.ok(repository.save(sr)));
    }

    private StatutoryReturn find(UUID id) {
        return repository.findByIdAndTenantIdAndDeletedFalse(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Statutory return not found: " + id));
    }

    private String currentUserId() {
        Object p = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return p instanceof UserPrincipal up ? up.getId() : null;
    }
}
