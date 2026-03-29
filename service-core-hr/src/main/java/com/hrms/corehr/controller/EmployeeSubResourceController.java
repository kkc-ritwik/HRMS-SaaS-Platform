package com.hrms.corehr.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.corehr.dto.*;
import com.hrms.corehr.service.EmployeeSubEntityService;
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
@RequestMapping("/api/v1/employees/{employeeId}")
@RequiredArgsConstructor
@Tag(name = "Employee Sub-Resources", description = "Addresses, emergency contacts, family, education, work history")
public class EmployeeSubResourceController {

    private final EmployeeSubEntityService subEntityService;

    // ── Addresses ─────────────────────────────────────────────────────────────

    @GetMapping("/addresses")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "List all addresses for an employee")
    public ResponseEntity<ApiResponse<List<AddressDto.Response>>> listAddresses(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.listAddresses(tenantId(), employeeId)));
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Add an address for an employee")
    public ResponseEntity<ApiResponse<AddressDto.Response>> addAddress(
            @PathVariable UUID employeeId,
            @Valid @RequestBody AddressDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                subEntityService.addAddress(tenantId(), employeeId, req, currentUserId())));
    }

    @PutMapping("/addresses/{addressId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update an address")
    public ResponseEntity<ApiResponse<AddressDto.Response>> updateAddress(
            @PathVariable UUID employeeId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.updateAddress(tenantId(), employeeId, addressId, req, currentUserId())));
    }

    @DeleteMapping("/addresses/{addressId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Delete an address (soft delete)")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable UUID employeeId,
            @PathVariable UUID addressId) {
        subEntityService.deleteAddress(tenantId(), employeeId, addressId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Emergency Contacts ────────────────────────────────────────────────────

    @GetMapping("/emergency-contacts")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "List emergency contacts for an employee")
    public ResponseEntity<ApiResponse<List<EmergencyContactDto.Response>>> listEmergencyContacts(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.listEmergencyContacts(tenantId(), employeeId)));
    }

    @PostMapping("/emergency-contacts")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Add an emergency contact for an employee")
    public ResponseEntity<ApiResponse<EmergencyContactDto.Response>> addEmergencyContact(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmergencyContactDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                subEntityService.addEmergencyContact(tenantId(), employeeId, req, currentUserId())));
    }

    @PutMapping("/emergency-contacts/{contactId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update an emergency contact")
    public ResponseEntity<ApiResponse<EmergencyContactDto.Response>> updateEmergencyContact(
            @PathVariable UUID employeeId,
            @PathVariable UUID contactId,
            @Valid @RequestBody EmergencyContactDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.updateEmergencyContact(tenantId(), employeeId, contactId, req, currentUserId())));
    }

    @DeleteMapping("/emergency-contacts/{contactId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Delete an emergency contact (soft delete)")
    public ResponseEntity<Void> deleteEmergencyContact(
            @PathVariable UUID employeeId,
            @PathVariable UUID contactId) {
        subEntityService.deleteEmergencyContact(tenantId(), employeeId, contactId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Family Members ────────────────────────────────────────────────────────

    @GetMapping("/family")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "List family members for an employee")
    public ResponseEntity<ApiResponse<List<FamilyMemberDto.Response>>> listFamily(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.listFamilyMembers(tenantId(), employeeId)));
    }

    @PostMapping("/family")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Add a family member for an employee")
    public ResponseEntity<ApiResponse<FamilyMemberDto.Response>> addFamilyMember(
            @PathVariable UUID employeeId,
            @Valid @RequestBody FamilyMemberDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                subEntityService.addFamilyMember(tenantId(), employeeId, req, currentUserId())));
    }

    @PutMapping("/family/{memberId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update a family member")
    public ResponseEntity<ApiResponse<FamilyMemberDto.Response>> updateFamilyMember(
            @PathVariable UUID employeeId,
            @PathVariable UUID memberId,
            @Valid @RequestBody FamilyMemberDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.updateFamilyMember(tenantId(), employeeId, memberId, req, currentUserId())));
    }

    @DeleteMapping("/family/{memberId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Delete a family member (soft delete)")
    public ResponseEntity<Void> deleteFamilyMember(
            @PathVariable UUID employeeId,
            @PathVariable UUID memberId) {
        subEntityService.deleteFamilyMember(tenantId(), employeeId, memberId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Education ─────────────────────────────────────────────────────────────

    @GetMapping("/education")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "List education records for an employee")
    public ResponseEntity<ApiResponse<List<EducationDto.Response>>> listEducation(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.listEducation(tenantId(), employeeId)));
    }

    @PostMapping("/education")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Add an education record for an employee")
    public ResponseEntity<ApiResponse<EducationDto.Response>> addEducation(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EducationDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                subEntityService.addEducation(tenantId(), employeeId, req, currentUserId())));
    }

    @PutMapping("/education/{educationId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update an education record")
    public ResponseEntity<ApiResponse<EducationDto.Response>> updateEducation(
            @PathVariable UUID employeeId,
            @PathVariable UUID educationId,
            @Valid @RequestBody EducationDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.updateEducation(tenantId(), employeeId, educationId, req, currentUserId())));
    }

    @DeleteMapping("/education/{educationId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Delete an education record (soft delete)")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable UUID employeeId,
            @PathVariable UUID educationId) {
        subEntityService.deleteEducation(tenantId(), employeeId, educationId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Employment History ────────────────────────────────────────────────────

    @GetMapping("/work-history")
    @PreAuthorize("hasAuthority('EMPLOYEES:READ')")
    @Operation(summary = "List previous employment history for an employee")
    public ResponseEntity<ApiResponse<List<EmploymentHistoryDto.Response>>> listWorkHistory(
            @PathVariable UUID employeeId) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.listWorkHistory(tenantId(), employeeId)));
    }

    @PostMapping("/work-history")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Add a work history entry for an employee")
    public ResponseEntity<ApiResponse<EmploymentHistoryDto.Response>> addWorkHistory(
            @PathVariable UUID employeeId,
            @Valid @RequestBody EmploymentHistoryDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                subEntityService.addWorkHistory(tenantId(), employeeId, req, currentUserId())));
    }

    @PutMapping("/work-history/{historyId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:WRITE')")
    @Operation(summary = "Update a work history entry")
    public ResponseEntity<ApiResponse<EmploymentHistoryDto.Response>> updateWorkHistory(
            @PathVariable UUID employeeId,
            @PathVariable UUID historyId,
            @Valid @RequestBody EmploymentHistoryDto.CreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                subEntityService.updateWorkHistory(tenantId(), employeeId, historyId, req, currentUserId())));
    }

    @DeleteMapping("/work-history/{historyId}")
    @PreAuthorize("hasAuthority('EMPLOYEES:DELETE')")
    @Operation(summary = "Delete a work history entry (soft delete)")
    public ResponseEntity<Void> deleteWorkHistory(
            @PathVariable UUID employeeId,
            @PathVariable UUID historyId) {
        subEntityService.deleteWorkHistory(tenantId(), employeeId, historyId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String tenantId() {
        return TenantContext.get();
    }

    private String currentUserId() {
        return ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }
}
