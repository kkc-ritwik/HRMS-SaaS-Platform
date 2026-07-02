package com.hrms.recruitment.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.recruitment.dto.OfferLetterDto;
import com.hrms.recruitment.service.OfferLetterService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
@Tag(name = "Offer Letters", description = "Generate, send and track offer letters")
public class OfferController {

    private final OfferLetterService offerLetterService;

    @PostMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Generate a DRAFT offer letter (template is merged automatically)")
    public ResponseEntity<ApiResponse<OfferLetterDto.Response>> create(
            @Valid @RequestBody OfferLetterDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                offerLetterService.create(tenantId(), req, currentUserId())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all offer letters for the tenant")
    public ResponseEntity<ApiResponse<List<OfferLetterDto.Response>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(offerLetterService.listAll(tenantId())));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "Get an offer letter by ID")
    public ResponseEntity<ApiResponse<OfferLetterDto.Response>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(offerLetterService.get(tenantId(), id)));
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAuthority('RECRUITMENT:READ')")
    @Operation(summary = "List all offer letters for an application")
    public ResponseEntity<ApiResponse<List<OfferLetterDto.Response>>> byApplication(
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(ApiResponse.ok(
                offerLetterService.listByApplication(tenantId(), applicationId)));
    }

    @PostMapping("/{id}/send")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Send a DRAFT offer letter to the candidate")
    public ResponseEntity<ApiResponse<OfferLetterDto.Response>> send(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                offerLetterService.send(tenantId(), id, currentUserId())));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Record candidate's response (accepted=true/false). " +
                         "Accepted → application moves to HIRED; Declined → REJECTED")
    public ResponseEntity<ApiResponse<OfferLetterDto.Response>> respond(
            @PathVariable UUID id,
            @Valid @RequestBody OfferLetterDto.RespondRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                offerLetterService.respond(tenantId(), id, req, currentUserId())));
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasAuthority('RECRUITMENT:WRITE')")
    @Operation(summary = "Revoke a DRAFT or SENT offer letter")
    public ResponseEntity<ApiResponse<OfferLetterDto.Response>> revoke(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                offerLetterService.revoke(tenantId(), id, currentUserId())));
    }

    private String tenantId() { return TenantContext.get(); }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
