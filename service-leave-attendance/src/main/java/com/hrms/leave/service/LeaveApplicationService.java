package com.hrms.leave.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.LeaveApplicationDto;
import com.hrms.leave.entity.*;
import com.hrms.leave.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveApplicationService {

    private final LeaveApplicationRepository applicationRepository;
    private final LeaveApprovalRepository    approvalRepository;
    private final LeaveBalanceRepository     balanceRepository;
    private final LeaveTypeRepository        leaveTypeRepository;
    private final LeavePolicyService         policyService;
    private final LeaveBalanceService        balanceService;
    private final HolidayService             holidayService;

    // ── Apply ─────────────────────────────────────────────────────────────────

    @Transactional
    public LeaveApplicationDto.Response apply(String tenantId, UUID employeeId,
                                               LeaveApplicationDto.ApplyRequest req,
                                               String currentUser) {
        // 1. Validate leave type
        LeaveType leaveType = leaveTypeRepository
                .findByIdAndTenantIdAndDeletedFalse(req.getLeaveTypeId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", req.getLeaveTypeId()));
        if (!leaveType.isActive()) {
            throw new BusinessException("LEAVE_TYPE_INACTIVE", "Leave type is not active");
        }

        // 2. Validate dates
        if (req.getFromDate().isAfter(req.getToDate())) {
            throw new BusinessException("INVALID_DATE_RANGE", "From date must be before or equal to to date");
        }

        // 3. Find active policy
        Optional<LeavePolicy> policyOpt = policyService.findActivePolicy(tenantId, req.getLeaveTypeId());
        LeavePolicy policy = policyOpt.orElse(null);

        // 4. Check min notice days
        if (policy != null && policy.getMinNoticeDays() > 0) {
            LocalDate minApplyDate = LocalDate.now().plusDays(policy.getMinNoticeDays());
            if (req.getFromDate().isBefore(minApplyDate)) {
                throw new BusinessException("INSUFFICIENT_NOTICE",
                        "Minimum notice of " + policy.getMinNoticeDays() + " day(s) required");
            }
        }

        // 5. Validate half-day
        LeaveApplication.DayType dayType = req.getDayType() != null ? req.getDayType() : LeaveApplication.DayType.FULL;
        if (dayType != LeaveApplication.DayType.FULL && policy != null && !policy.isAllowHalfDay()) {
            throw new BusinessException("HALF_DAY_NOT_ALLOWED",
                    "Half-day leave is not allowed for this leave type");
        }
        if (dayType != LeaveApplication.DayType.FULL && !req.getFromDate().equals(req.getToDate())) {
            throw new BusinessException("HALF_DAY_SINGLE_DATE",
                    "Half-day leave must be for a single day (from = to)");
        }

        // 6. Calculate duration
        Set<LocalDate> holidays = holidayService.getHolidayDates(tenantId, req.getFromDate().getYear());
        boolean sandwichRule = policy != null && policy.isSandwichRuleEnabled();
        BigDecimal duration = calculateDuration(req.getFromDate(), req.getToDate(),
                dayType, sandwichRule, holidays);

        if (duration.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("ZERO_DURATION",
                    "Leave duration is zero (all days fall on holidays/weekends)");
        }

        // 7. Check for conflicting leaves
        List<LeaveApplication> conflicts = applicationRepository.findConflicting(
                tenantId, employeeId, req.getFromDate(), req.getToDate());
        if (!conflicts.isEmpty()) {
            throw new BusinessException("LEAVE_CONFLICT",
                    "You already have a leave application overlapping these dates");
        }

        // 8. Check balance
        int year = req.getFromDate().getYear();
        if (policy != null) {
            Optional<LeaveBalance> balOpt = balanceRepository
                    .findByEmployeeIdAndLeaveTypeIdAndYearAndTenantIdAndDeletedFalse(
                            employeeId, req.getLeaveTypeId(), year, tenantId);
            if (balOpt.isPresent()) {
                BigDecimal available = balOpt.get().getAvailable();
                if (available == null) available = BigDecimal.ZERO;
                if (available.compareTo(duration) < 0) {
                    if (!policy.isNegativeBalanceAllowed()) {
                        throw new BusinessException("INSUFFICIENT_BALANCE",
                                "Insufficient leave balance. Available: " + available + ", Required: " + duration);
                    }
                    // Check max negative
                    if (policy.getMaxNegativeDays() != null) {
                        BigDecimal deficit = duration.subtract(available);
                        if (deficit.compareTo(policy.getMaxNegativeDays()) > 0) {
                            throw new BusinessException("EXCEEDS_NEGATIVE_LIMIT",
                                    "Requested leave exceeds maximum negative balance allowed");
                        }
                    }
                }
            }
        }

        // 9. Check attachment requirement
        if (leaveType.getRequiresAttachmentAfterDays() != null
                && duration.compareTo(leaveType.getRequiresAttachmentAfterDays()) > 0
                && (req.getAttachmentUrl() == null || req.getAttachmentUrl().isBlank())) {
            throw new BusinessException("ATTACHMENT_REQUIRED",
                    "Attachment is required for leave exceeding " +
                    leaveType.getRequiresAttachmentAfterDays() + " day(s)");
        }

        // 10. Save application
        LeaveApplication application = new LeaveApplication();
        application.setTenantId(tenantId);
        application.setEmployeeId(employeeId);
        application.setLeaveTypeId(req.getLeaveTypeId());
        application.setFromDate(req.getFromDate());
        application.setToDate(req.getToDate());
        application.setDurationDays(duration);
        application.setDayType(dayType);
        application.setReason(req.getReason());
        application.setAttachmentUrl(req.getAttachmentUrl());
        application.setStatus(LeaveApplication.LeaveStatus.PENDING);
        application.setAppliedAt(Instant.now());
        application.setCreatedBy(currentUser);
        application.setUpdatedBy(currentUser);
        LeaveApplication saved = applicationRepository.save(application);

        // 11. Create approval record
        if (req.getApproverId() != null) {
            LeaveApproval approval = new LeaveApproval();
            approval.setTenantId(tenantId);
            approval.setLeaveApplicationId(saved.getId());
            approval.setApproverId(req.getApproverId());
            approval.setLevel(1);
            approval.setStatus(LeaveApproval.ApprovalStatus.PENDING);
            approval.setCreatedBy(currentUser);
            approval.setUpdatedBy(currentUser);
            approvalRepository.save(approval);
        }

        log.info("[Leave] Application {} created by employee {} for {} days of {}",
                saved.getId(), employeeId, duration, leaveType.getName());
        return toResponse(saved, leaveType);
    }

    // ── Approve ───────────────────────────────────────────────────────────────

    @Transactional
    public LeaveApplicationDto.Response approve(String tenantId, UUID applicationId,
                                                 LeaveApplicationDto.ApprovalRequest req,
                                                 UUID approverId, String currentUser) {
        LeaveApplication application = findOrThrow(tenantId, applicationId);
        if (application.getStatus() != LeaveApplication.LeaveStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS",
                    "Only PENDING applications can be approved");
        }

        // Update approval record
        approvalRepository
                .findByLeaveApplicationIdAndApproverIdAndDeletedFalse(applicationId, approverId)
                .ifPresent(approval -> {
                    approval.setStatus(LeaveApproval.ApprovalStatus.APPROVED);
                    approval.setComments(req.getComments());
                    approval.setActedAt(Instant.now());
                    approval.setUpdatedBy(currentUser);
                    approvalRepository.save(approval);
                });

        // Approve the application
        application.setStatus(LeaveApplication.LeaveStatus.APPROVED);
        application.setUpdatedBy(currentUser);
        applicationRepository.save(application);

        // Deduct from balance
        int year = application.getFromDate().getYear();
        balanceService.deductUsed(tenantId, application.getEmployeeId(),
                application.getLeaveTypeId(), year, application.getDurationDays(), currentUser);

        log.info("[Leave] Application {} APPROVED by {}", applicationId, approverId);
        return toResponse(application, leaveTypeOrNull(application.getLeaveTypeId()));
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Transactional
    public LeaveApplicationDto.Response reject(String tenantId, UUID applicationId,
                                                LeaveApplicationDto.ApprovalRequest req,
                                                UUID approverId, String currentUser) {
        LeaveApplication application = findOrThrow(tenantId, applicationId);
        if (application.getStatus() != LeaveApplication.LeaveStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS",
                    "Only PENDING applications can be rejected");
        }

        approvalRepository
                .findByLeaveApplicationIdAndApproverIdAndDeletedFalse(applicationId, approverId)
                .ifPresent(approval -> {
                    approval.setStatus(LeaveApproval.ApprovalStatus.REJECTED);
                    approval.setComments(req.getComments());
                    approval.setActedAt(Instant.now());
                    approval.setUpdatedBy(currentUser);
                    approvalRepository.save(approval);
                });

        application.setStatus(LeaveApplication.LeaveStatus.REJECTED);
        application.setUpdatedBy(currentUser);
        applicationRepository.save(application);

        log.info("[Leave] Application {} REJECTED by {}", applicationId, approverId);
        return toResponse(application, leaveTypeOrNull(application.getLeaveTypeId()));
    }

    // ── Cancel ────────────────────────────────────────────────────────────────

    @Transactional
    public LeaveApplicationDto.Response cancel(String tenantId, UUID applicationId,
                                                LeaveApplicationDto.CancelRequest req,
                                                UUID employeeId, String currentUser) {
        LeaveApplication application = findOrThrow(tenantId, applicationId);

        if (application.getStatus() == LeaveApplication.LeaveStatus.CANCELLED) {
            throw new BusinessException("ALREADY_CANCELLED", "Application is already cancelled");
        }
        if (application.getStatus() == LeaveApplication.LeaveStatus.REJECTED) {
            throw new BusinessException("ALREADY_REJECTED", "Cannot cancel a rejected application");
        }
        if (!application.getEmployeeId().equals(employeeId)) {
            throw new BusinessException("NOT_YOUR_LEAVE", "You can only cancel your own leave");
        }

        boolean wasApproved = application.getStatus() == LeaveApplication.LeaveStatus.APPROVED;

        application.setStatus(LeaveApplication.LeaveStatus.CANCELLED);
        application.setCancelledAt(Instant.now());
        application.setCancelReason(req.getCancelReason());
        application.setUpdatedBy(currentUser);
        applicationRepository.save(application);

        // Credit back balance if the leave was already approved
        if (wasApproved) {
            int year = application.getFromDate().getYear();
            balanceService.creditBack(tenantId, application.getEmployeeId(),
                    application.getLeaveTypeId(), year, application.getDurationDays(), currentUser);
            log.info("[Leave] Balance credited back for cancelled approved application {}",
                    applicationId);
        }

        return toResponse(application, leaveTypeOrNull(application.getLeaveTypeId()));
    }

    // ── My leaves ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LeaveApplicationDto.Response> getMyLeaves(String tenantId, UUID employeeId,
                                                           Integer year,
                                                           LeaveApplication.LeaveStatus status,
                                                           Pageable pageable) {
        return applicationRepository.findByEmployeeFiltered(
                        tenantId, employeeId, status, year, pageable)
                .map(a -> toResponse(a, leaveTypeOrNull(a.getLeaveTypeId())));
    }

    // ── Team leaves ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<LeaveApplicationDto.Response> getTeamLeaves(String tenantId,
                                                             List<UUID> teamMemberIds,
                                                             Pageable pageable) {
        return applicationRepository
                .findByTenantIdAndEmployeeIdInAndDeletedFalse(tenantId, teamMemberIds, pageable)
                .map(a -> toResponse(a, leaveTypeOrNull(a.getLeaveTypeId())));
    }

    /** Team/HR view: filter by an explicit employee list, else tenant-wide; optional status. */
    @Transactional(readOnly = true)
    public Page<LeaveApplicationDto.Response> listTeamLeaves(String tenantId,
                                                              List<UUID> teamMemberIds,
                                                              LeaveApplication.LeaveStatus status,
                                                              Pageable pageable) {
        Page<LeaveApplication> page = (teamMemberIds != null && !teamMemberIds.isEmpty())
                ? applicationRepository.findByTenantIdAndEmployeeIdInAndDeletedFalse(tenantId, teamMemberIds, pageable)
                : applicationRepository.findByTenantFiltered(tenantId, status, pageable);
        return page.map(a -> toResponse(a, leaveTypeOrNull(a.getLeaveTypeId())));
    }

    // ── Team calendar ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<LeaveApplicationDto.TeamCalendarItem> getTeamCalendar(
            String tenantId, List<UUID> teamMemberIds, int month, int year) {
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to   = from.withDayOfMonth(from.lengthOfMonth());
        return applicationRepository.findTeamLeaves(tenantId, teamMemberIds, from, to)
                .stream()
                .map(a -> {
                    LeaveType lt = leaveTypeOrNull(a.getLeaveTypeId());
                    return LeaveApplicationDto.TeamCalendarItem.builder()
                            .employeeId(a.getEmployeeId())
                            .leaveTypeId(a.getLeaveTypeId())
                            .leaveTypeName(lt != null ? lt.getName() : null)
                            .leaveTypeColor(lt != null ? lt.getColor() : null)
                            .fromDate(a.getFromDate()).toDate(a.getToDate())
                            .durationDays(a.getDurationDays())
                            .status(a.getStatus().name())
                            .build();
                }).toList();
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    // ── Duration calculation ──────────────────────────────────────────────────

    /**
     * Calculates leave duration:
     * <ul>
     *   <li>Half-day → 0.5</li>
     *   <li>Sandwich rule ON → all calendar days from → to (inclusive)</li>
     *   <li>Sandwich rule OFF → working days only (exclude weekends + holidays)</li>
     * </ul>
     */
    BigDecimal calculateDuration(LocalDate from, LocalDate to, LeaveApplication.DayType dayType,
                                  boolean sandwichRule, Set<LocalDate> holidays) {
        if (dayType == LeaveApplication.DayType.FIRST_HALF || dayType == LeaveApplication.DayType.SECOND_HALF) {
            return new BigDecimal("0.5");
        }
        if (sandwichRule) {
            // Count all calendar days from–to inclusive
            long days = ChronoUnit.DAYS.between(from, to) + 1;
            return BigDecimal.valueOf(days);
        } else {
            // Count only working days (Mon–Fri, non-holiday)
            long workingDays = from.datesUntil(to.plusDays(1))
                    .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY
                              && d.getDayOfWeek() != DayOfWeek.SUNDAY
                              && !holidays.contains(d))
                    .count();
            return BigDecimal.valueOf(workingDays);
        }
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private LeaveApplication findOrThrow(String tenantId, UUID id) {
        return applicationRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveApplication", "id", id));
    }

    private LeaveType leaveTypeOrNull(UUID leaveTypeId) {
        if (leaveTypeId == null) return null;
        return leaveTypeRepository.findById(leaveTypeId).orElse(null);
    }

    private LeaveApplicationDto.Response toResponse(LeaveApplication a, LeaveType lt) {
        List<LeaveApplicationDto.ApprovalInfo> approvals =
                approvalRepository.findByLeaveApplicationIdOrderByLevelAsc(a.getId())
                        .stream()
                        .map(ap -> LeaveApplicationDto.ApprovalInfo.builder()
                                .id(ap.getId())
                                .approverId(ap.getApproverId())
                                .level(ap.getLevel())
                                .status(ap.getStatus().name())
                                .comments(ap.getComments())
                                .actedAt(ap.getActedAt())
                                .build())
                        .toList();

        return LeaveApplicationDto.Response.builder()
                .id(a.getId()).tenantId(a.getTenantId())
                .employeeId(a.getEmployeeId())
                .leaveTypeId(a.getLeaveTypeId())
                .leaveTypeName(lt != null ? lt.getName() : null)
                .leaveTypeColor(lt != null ? lt.getColor() : null)
                .fromDate(a.getFromDate()).toDate(a.getToDate())
                .durationDays(a.getDurationDays()).dayType(a.getDayType())
                .reason(a.getReason()).attachmentUrl(a.getAttachmentUrl())
                .status(a.getStatus()).appliedAt(a.getAppliedAt())
                .cancelledAt(a.getCancelledAt()).cancelReason(a.getCancelReason())
                .approvals(approvals)
                .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
                .build();
    }
}
