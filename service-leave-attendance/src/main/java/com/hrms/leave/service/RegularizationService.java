package com.hrms.leave.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.RegularizationDto;
import com.hrms.leave.entity.AttendanceRecord;
import com.hrms.leave.entity.RegularizationRequest;
import com.hrms.leave.entity.Shift;
import com.hrms.leave.repository.AttendanceRecordRepository;
import com.hrms.leave.repository.RegularizationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegularizationService {

    private final RegularizationRequestRepository regularizationRepository;
    private final AttendanceRecordRepository      recordRepository;
    private final ShiftService                    shiftService;
    private final AttendanceService               attendanceService;

    // ── Submit a regularization request ───────────────────────────────────────

    @Transactional
    public RegularizationDto.Response request(String tenantId, UUID employeeId,
                                               RegularizationDto.RequestDto req,
                                               String currentUser) {
        if (req.getCorrectedOut().isBefore(req.getCorrectedIn())) {
            throw new BusinessException("INVALID_TIME_RANGE", "correctedOut must be after correctedIn");
        }
        if (req.getDate().isAfter(LocalDate.now(ZoneOffset.UTC))) {
            throw new BusinessException("FUTURE_DATE", "Cannot regularize a future date");
        }

        // Snapshot the existing check-in / check-out times (if any)
        Optional<AttendanceRecord> existing = recordRepository
                .findByEmployeeIdAndAttendanceDateAndTenantIdAndDeletedFalse(employeeId, req.getDate(), tenantId);
        Instant originalIn  = existing.map(AttendanceRecord::getFirstCheckIn).orElse(null);
        Instant originalOut = existing.map(AttendanceRecord::getLastCheckOut).orElse(null);

        RegularizationRequest reg = new RegularizationRequest();
        reg.setTenantId(tenantId);
        reg.setEmployeeId(employeeId);
        reg.setRequestDate(req.getDate());
        reg.setOriginalIn(originalIn);
        reg.setOriginalOut(originalOut);
        reg.setCorrectedIn(req.getCorrectedIn());
        reg.setCorrectedOut(req.getCorrectedOut());
        reg.setReason(req.getReason());
        reg.setStatus(RegularizationRequest.RegularizationStatus.PENDING);
        reg.setCreatedBy(currentUser);
        regularizationRepository.save(reg);

        log.info("Regularization requested: id={} employee={} date={}", reg.getId(), employeeId, req.getDate());
        return toResponse(reg);
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @Transactional
    public RegularizationDto.Response approve(String tenantId, UUID regId,
                                               RegularizationDto.ApprovalDto req,
                                               UUID approverId, String currentUser) {
        RegularizationRequest reg = getRegRequest(tenantId, regId);
        if (reg.getStatus() != RegularizationRequest.RegularizationStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "Regularization request is not in PENDING state");
        }

        reg.setStatus(RegularizationRequest.RegularizationStatus.APPROVED);
        reg.setApprovedBy(approverId);
        reg.setUpdatedBy(currentUser);
        regularizationRepository.save(reg);

        // Apply the corrected times to the attendance record
        applyRegularizationToRecord(tenantId, reg, currentUser);

        log.info("Regularization approved: id={} approvedBy={}", regId, approverId);
        return toResponse(reg);
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Transactional
    public RegularizationDto.Response reject(String tenantId, UUID regId,
                                              RegularizationDto.ApprovalDto req,
                                              UUID approverId, String currentUser) {
        RegularizationRequest reg = getRegRequest(tenantId, regId);
        if (reg.getStatus() != RegularizationRequest.RegularizationStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "Regularization request is not in PENDING state");
        }

        reg.setStatus(RegularizationRequest.RegularizationStatus.REJECTED);
        reg.setApprovedBy(approverId);
        reg.setUpdatedBy(currentUser);
        regularizationRepository.save(reg);

        log.info("Regularization rejected: id={} rejectedBy={}", regId, approverId);
        return toResponse(reg);
    }

    // ── Employee: my requests ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<RegularizationDto.Response> getMyRequests(String tenantId, UUID employeeId, Pageable pageable) {
        return regularizationRepository
                .findByEmployeeIdAndTenantIdAndDeletedFalse(employeeId, tenantId, pageable)
                .map(this::toResponse);
    }

    // ── Manager / HR: pending requests ────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RegularizationDto.Response> getPendingRequests(String tenantId) {
        return regularizationRepository
                .findByTenantIdAndStatusAndDeletedFalse(tenantId, RegularizationRequest.RegularizationStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RegularizationDto.Response> getPendingForTeam(String tenantId, List<UUID> employeeIds) {
        return regularizationRepository
                .findByTenantIdAndEmployeeIdInAndStatusAndDeletedFalse(
                        tenantId, employeeIds, RegularizationRequest.RegularizationStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private void applyRegularizationToRecord(String tenantId, RegularizationRequest reg, String currentUser) {
        LocalDate date = reg.getRequestDate();
        UUID employeeId = reg.getEmployeeId();

        AttendanceRecord record = recordRepository
                .findByEmployeeIdAndAttendanceDateAndTenantIdAndDeletedFalse(employeeId, date, tenantId)
                .orElseGet(() -> {
                    AttendanceRecord r = new AttendanceRecord();
                    r.setTenantId(tenantId);
                    r.setEmployeeId(employeeId);
                    r.setAttendanceDate(date);
                    r.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
                    r.setCreatedBy(currentUser);
                    return r;
                });

        record.setFirstCheckIn(reg.getCorrectedIn());
        record.setLastCheckOut(reg.getCorrectedOut());
        record.setRegularized(true);
        record.setUpdatedBy(currentUser);

        Optional<Shift> shiftOpt = shiftService.getEmployeeShiftForDate(tenantId, employeeId, date);
        Instant dayStart = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd   = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        attendanceService.recalculateRecord(record, shiftOpt.orElse(null), tenantId, employeeId, dayStart, dayEnd);

        recordRepository.save(record);
    }

    private RegularizationRequest getRegRequest(String tenantId, UUID id) {
        return regularizationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RegularizationRequest", "id", id));
    }

    private RegularizationDto.Response toResponse(RegularizationRequest r) {
        return RegularizationDto.Response.builder()
                .id(r.getId())
                .employeeId(r.getEmployeeId())
                .date(r.getRequestDate())
                .originalIn(r.getOriginalIn())
                .originalOut(r.getOriginalOut())
                .correctedIn(r.getCorrectedIn())
                .correctedOut(r.getCorrectedOut())
                .reason(r.getReason())
                .status(r.getStatus())
                .approvedBy(r.getApprovedBy())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
