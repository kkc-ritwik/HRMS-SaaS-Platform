package com.hrms.engagement.award;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Recognition Awards — structured, approval-gated awards (Spot Award, Employee of
 * the Month, Long Service, etc.). Complements peer kudos.
 */
@RestController
@RequestMapping("/api/v1/awards")
@RequiredArgsConstructor
@Tag(name = "Awards", description = "Recognition awards & nominations")
public class AwardController {

    private final AwardRepository repository;

    @GetMapping
    @PreAuthorize("hasAuthority('ENGAGEMENT:READ')")
    @Operation(summary = "List awards (optionally by status)")
    public ResponseEntity<ApiResponse<List<Award>>> list(
            @RequestParam(required = false) Award.Status status) {
        String tenantId = TenantContext.get();
        List<Award> awards = status != null
                ? repository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, status)
                : repository.findByTenantIdOrderByCreatedAtDesc(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(awards));
    }

    @GetMapping("/nominee/{nomineeId}")
    @PreAuthorize("hasAuthority('ENGAGEMENT:READ')")
    @Operation(summary = "List awards for an employee")
    public ResponseEntity<ApiResponse<List<Award>>> forNominee(@PathVariable UUID nomineeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                repository.findByTenantIdAndNomineeIdOrderByCreatedAtDesc(TenantContext.get(), nomineeId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENGAGEMENT:READ')")
    @Operation(summary = "Get an award by ID")
    public ResponseEntity<ApiResponse<Award>> get(@PathVariable UUID id) {
        Award award = repository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Award not found: " + id));
        return ResponseEntity.ok(ApiResponse.ok(award));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ENGAGEMENT:WRITE')")
    @Operation(summary = "Nominate an employee for an award")
    public ResponseEntity<ApiResponse<Award>> nominate(@Valid @RequestBody Award body) {
        body.setId(null);
        body.setTenantId(TenantContext.get());
        body.setStatus(Award.Status.NOMINATED);
        UUID actor = currentUserId();
        if (body.getNominatedBy() == null) body.setNominatedBy(actor);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(repository.save(body)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ENGAGEMENT:WRITE')")
    @Operation(summary = "Approve a nomination (grants the award)")
    public ResponseEntity<ApiResponse<Award>> approve(@PathVariable UUID id,
            @RequestBody(required = false) DecisionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(decide(id, Award.Status.APPROVED, req)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ENGAGEMENT:WRITE')")
    @Operation(summary = "Reject a nomination")
    public ResponseEntity<ApiResponse<Award>> reject(@PathVariable UUID id,
            @RequestBody(required = false) DecisionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(decide(id, Award.Status.REJECTED, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ENGAGEMENT:WRITE')")
    @Operation(summary = "Delete an award nomination")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        Award award = repository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Award not found: " + id));
        repository.delete(award);
        return ResponseEntity.noContent().build();
    }

    private Award decide(UUID id, Award.Status status, DecisionRequest req) {
        Award award = repository.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("Award not found: " + id));
        award.setStatus(status == Award.Status.APPROVED ? Award.Status.AWARDED : status);
        award.setDecidedBy(currentUserId());
        award.setDecidedAt(OffsetDateTime.now());
        if (req != null) award.setDecisionNotes(req.notes());
        return repository.save(award);
    }

    private UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal up && up.getId() != null) {
            try { return UUID.fromString(up.getId()); } catch (IllegalArgumentException ignored) { /* non-UUID */ }
        }
        return null;
    }

    public record DecisionRequest(String notes) {}
}
