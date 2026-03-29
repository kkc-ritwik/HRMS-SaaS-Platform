package com.hrms.leave.service;

import com.hrms.leave.dto.AttendanceDto;
import com.hrms.leave.entity.*;
import com.hrms.leave.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendancePunchRepository     punchRepository;
    private final AttendanceRecordRepository    recordRepository;
    private final ShiftService                  shiftService;

    // ── Punch (auto-detect IN / OUT) ──────────────────────────────────────────

    @Transactional
    public AttendanceDto.PunchResponse punch(String tenantId, UUID employeeId,
                                              AttendanceDto.PunchRequest req,
                                              String currentUser) {
        Instant now      = Instant.now();
        LocalDate today  = now.atZone(ZoneOffset.UTC).toLocalDate();
        Instant dayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant dayEnd   = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // Determine punch type by looking at today's punches
        List<AttendancePunch> todayPunches = punchRepository.findDayPunches(tenantId, employeeId, dayStart, dayEnd);
        AttendancePunch.PunchType punchType;
        if (todayPunches.isEmpty()) {
            punchType = AttendancePunch.PunchType.IN;
        } else {
            AttendancePunch last = todayPunches.get(todayPunches.size() - 1);
            punchType = (last.getType() == AttendancePunch.PunchType.IN)
                    ? AttendancePunch.PunchType.OUT
                    : AttendancePunch.PunchType.IN;
        }

        // Save punch record
        AttendancePunch punch = new AttendancePunch();
        punch.setTenantId(tenantId);
        punch.setEmployeeId(employeeId);
        punch.setPunchTime(now);
        punch.setType(punchType);
        punch.setSource(req.getSource());
        punch.setIpAddress(req.getIpAddress());
        punch.setLatitude(req.getLatitude());
        punch.setLongitude(req.getLongitude());
        punch.setCreatedBy(currentUser);
        punchRepository.save(punch);

        // Get or create attendance record for today
        AttendanceRecord record = recordRepository
                .findByEmployeeIdAndAttendanceDateAndTenantIdAndDeletedFalse(employeeId, today, tenantId)
                .orElseGet(() -> {
                    AttendanceRecord r = new AttendanceRecord();
                    r.setTenantId(tenantId);
                    r.setEmployeeId(employeeId);
                    r.setAttendanceDate(today);
                    r.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
                    r.setCreatedBy(currentUser);
                    return r;
                });

        Optional<Shift> shiftOpt = shiftService.getEmployeeShiftForDate(tenantId, employeeId, today);

        if (punchType == AttendancePunch.PunchType.IN) {
            // Only set firstCheckIn on very first IN punch of the day
            if (record.getFirstCheckIn() == null) {
                record.setFirstCheckIn(now);
                record.setSource(req.getSource());
                record.setCheckInLat(req.getLatitude());
                record.setCheckInLng(req.getLongitude());

                if (shiftOpt.isPresent()) {
                    Shift shift = shiftOpt.get();
                    record.setShiftId(shift.getId());
                    LocalTime effective = shift.getStartTime().plusMinutes(shift.getGracePeriodMins());
                    LocalTime checkInTime = now.atZone(ZoneOffset.UTC).toLocalTime();
                    if (checkInTime.isAfter(effective)) {
                        record.setLateByMins((int) ChronoUnit.MINUTES.between(effective, checkInTime));
                    }
                }
                record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            }
        } else {
            // OUT punch: update last checkout and recalculate
            record.setLastCheckOut(now);
            recalculateRecord(record, shiftOpt.orElse(null), tenantId, employeeId, dayStart, dayEnd);
        }

        record.setUpdatedBy(currentUser);
        recordRepository.save(record);

        BigDecimal effectiveHours = record.getEffectiveHours() != null
                ? record.getEffectiveHours() : BigDecimal.ZERO;

        return AttendanceDto.PunchResponse.builder()
                .punchId(punch.getId())
                .employeeId(employeeId)
                .punchTime(now)
                .type(punchType)
                .source(req.getSource())
                .currentStatus(record.getStatus())
                .firstCheckIn(record.getFirstCheckIn())
                .lastCheckOut(record.getLastCheckOut())
                .effectiveHoursToday(effectiveHours)
                .lateByMins(record.getLateByMins())
                .message(punchType == AttendancePunch.PunchType.IN ? "Checked in successfully" : "Checked out successfully")
                .build();
    }

    // ── My Attendance (month summary) ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceDto.MonthSummary getMyAttendance(String tenantId, UUID employeeId, int year, int month) {
        List<AttendanceRecord> records = recordRepository.findByEmployeeAndMonth(tenantId, employeeId, year, month);

        // Fetch all punches for the month in one query
        YearMonth ym = YearMonth.of(year, month);
        Instant monthStart = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant monthEnd   = ym.atEndOfMonth().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<AttendancePunch> allPunches = punchRepository
                .findByEmployeeIdAndTenantIdAndPunchTimeBetweenOrderByPunchTimeAsc(employeeId, tenantId, monthStart, monthEnd);
        Map<LocalDate, List<AttendancePunch>> punchesByDate = allPunches.stream()
                .collect(Collectors.groupingBy(p -> p.getPunchTime().atZone(ZoneOffset.UTC).toLocalDate()));

        int presentDays  = count(records, AttendanceRecord.AttendanceStatus.PRESENT);
        int halfDays     = count(records, AttendanceRecord.AttendanceStatus.HALF_DAY);
        int absentDays   = count(records, AttendanceRecord.AttendanceStatus.ABSENT);
        int leaveDays    = count(records, AttendanceRecord.AttendanceStatus.ON_LEAVE);
        int holidayDays  = count(records, AttendanceRecord.AttendanceStatus.HOLIDAY);
        int weekOffDays  = count(records, AttendanceRecord.AttendanceStatus.WEEK_OFF);
        BigDecimal totalOvertime = records.stream()
                .map(r -> r.getOvertimeHours() != null ? r.getOvertimeHours() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<AttendanceDto.RecordResponse> recordResponses = records.stream()
                .map(r -> toRecordResponse(r, punchesByDate.getOrDefault(r.getAttendanceDate(), List.of())))
                .collect(Collectors.toList());

        return AttendanceDto.MonthSummary.builder()
                .year(year).month(month).employeeId(employeeId)
                .presentDays(presentDays).halfDays(halfDays).absentDays(absentDays)
                .leaveDays(leaveDays).holidayDays(holidayDays).weekOffDays(weekOffDays)
                .totalOvertimeHours(totalOvertime)
                .records(recordResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<AttendanceDto.RecordResponse> getAttendancePage(String tenantId, UUID employeeId, Pageable pageable) {
        return recordRepository
                .findByEmployeeIdAndTenantIdAndDeletedFalse(employeeId, tenantId, pageable)
                .map(r -> toRecordResponse(r, List.of()));
    }

    // ── Team Attendance ────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AttendanceDto.TeamAttendanceItem> getTeamAttendance(String tenantId,
                                                                     List<UUID> employeeIds,
                                                                     LocalDate date) {
        List<AttendanceRecord> records = recordRepository.findTeamAttendance(tenantId, employeeIds, date);
        Map<UUID, AttendanceRecord> byEmployee = records.stream()
                .collect(Collectors.toMap(AttendanceRecord::getEmployeeId, r -> r));

        return employeeIds.stream().map(empId -> {
            AttendanceRecord rec = byEmployee.get(empId);
            if (rec == null) {
                return AttendanceDto.TeamAttendanceItem.builder()
                        .employeeId(empId)
                        .status(AttendanceRecord.AttendanceStatus.ABSENT)
                        .build();
            }
            return AttendanceDto.TeamAttendanceItem.builder()
                    .employeeId(empId)
                    .status(rec.getStatus())
                    .firstCheckIn(rec.getFirstCheckIn())
                    .lastCheckOut(rec.getLastCheckOut())
                    .effectiveHours(rec.getEffectiveHours())
                    .lateByMins(rec.getLateByMins())
                    .build();
        }).collect(Collectors.toList());
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AttendanceDto.DashboardResponse getDashboard(String tenantId, LocalDate date) {
        List<AttendanceRecord> allRecords = recordRepository
                .findByTenantIdAndAttendanceDateAndDeletedFalse(tenantId, date);

        long presentCount = allRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT
                          || r.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY)
                .count();
        long halfDayCount = allRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY).count();
        long lateCount = allRecords.stream()
                .filter(r -> (r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT
                           || r.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY)
                          && r.getLateByMins() > 0)
                .count();
        long onLeaveCount = allRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ON_LEAVE).count();
        long absentCount = allRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT).count();

        List<AttendanceDto.TeamAttendanceItem> presentEmployees = allRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.PRESENT
                          || r.getStatus() == AttendanceRecord.AttendanceStatus.HALF_DAY)
                .map(r -> AttendanceDto.TeamAttendanceItem.builder()
                        .employeeId(r.getEmployeeId())
                        .status(r.getStatus())
                        .firstCheckIn(r.getFirstCheckIn())
                        .lastCheckOut(r.getLastCheckOut())
                        .effectiveHours(r.getEffectiveHours())
                        .lateByMins(r.getLateByMins())
                        .build())
                .collect(Collectors.toList());

        List<AttendanceDto.TeamAttendanceItem> lateEmployees = presentEmployees.stream()
                .filter(e -> e.getLateByMins() > 0)
                .collect(Collectors.toList());

        List<UUID> absentEmployeeIds = allRecords.stream()
                .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.ABSENT)
                .map(AttendanceRecord::getEmployeeId)
                .collect(Collectors.toList());

        return AttendanceDto.DashboardResponse.builder()
                .date(date)
                .totalExpected(allRecords.size())
                .presentCount(presentCount)
                .absentCount(absentCount)
                .lateCount(lateCount)
                .onLeaveCount(onLeaveCount)
                .halfDayCount(halfDayCount)
                .presentEmployees(presentEmployees)
                .absentEmployeeIds(absentEmployeeIds)
                .lateEmployees(lateEmployees)
                .build();
    }

    // ── Internal: recalculate an attendance record after checkout ─────────────

    void recalculateRecord(AttendanceRecord record, Shift shift,
                           String tenantId, UUID employeeId,
                           Instant dayStart, Instant dayEnd) {
        if (record.getFirstCheckIn() == null || record.getLastCheckOut() == null) return;

        // Total span from first check-in to last check-out (seconds)
        long spanSeconds = ChronoUnit.SECONDS.between(record.getFirstCheckIn(), record.getLastCheckOut());

        // Sum break durations from BREAK_START / BREAK_END pairs
        List<AttendancePunch> punches = punchRepository.findDayPunches(tenantId, employeeId, dayStart, dayEnd);
        long breakSeconds = 0;
        Instant breakStart = null;
        for (AttendancePunch p : punches) {
            if (p.getType() == AttendancePunch.PunchType.BREAK_START) breakStart = p.getPunchTime();
            if (p.getType() == AttendancePunch.PunchType.BREAK_END && breakStart != null) {
                breakSeconds += ChronoUnit.SECONDS.between(breakStart, p.getPunchTime());
                breakStart = null;
            }
        }

        // If shift defines fixed break, also include that (take the larger of recorded vs configured)
        if (shift != null && shift.getBreakDurationMins() > 0) {
            breakSeconds = Math.max(breakSeconds, (long) shift.getBreakDurationMins() * 60);
        }

        BigDecimal totalHours     = secondsToHours(spanSeconds);
        BigDecimal breakHours     = secondsToHours(breakSeconds);
        BigDecimal effectiveHours = totalHours.subtract(breakHours).max(BigDecimal.ZERO);

        record.setTotalHours(totalHours);
        record.setBreakHours(breakHours);
        record.setEffectiveHours(effectiveHours);

        // Determine status
        if (shift != null) {
            if (effectiveHours.compareTo(shift.getMinHoursFullDay()) >= 0) {
                record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            } else if (effectiveHours.compareTo(shift.getMinHoursHalfDay()) >= 0) {
                record.setStatus(AttendanceRecord.AttendanceStatus.HALF_DAY);
            } else {
                record.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
            }

            // Early leaving
            LocalTime checkOutTime = record.getLastCheckOut().atZone(ZoneOffset.UTC).toLocalTime();
            if (checkOutTime.isBefore(shift.getEndTime())) {
                record.setEarlyLeavingMins((int) ChronoUnit.MINUTES.between(checkOutTime, shift.getEndTime()));
            }

            // Overtime
            long thresholdSeconds = (long) (shift.getMinHoursFullDay().doubleValue() * 3600)
                    + shift.getOvertimeThresholdMins() * 60L;
            long effectiveSeconds = (long) (effectiveHours.doubleValue() * 3600);
            if (effectiveSeconds > thresholdSeconds) {
                record.setOvertimeHours(secondsToHours(effectiveSeconds - thresholdSeconds));
            } else {
                record.setOvertimeHours(BigDecimal.ZERO);
            }
        } else {
            // No shift: present if >= 4h, half-day if >= 2h
            if (effectiveHours.compareTo(BigDecimal.valueOf(8)) >= 0) {
                record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT);
            } else if (effectiveHours.compareTo(BigDecimal.valueOf(4)) >= 0) {
                record.setStatus(AttendanceRecord.AttendanceStatus.HALF_DAY);
            } else if (record.getFirstCheckIn() != null) {
                record.setStatus(AttendanceRecord.AttendanceStatus.PRESENT); // any punch counts
            }
        }
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    AttendanceDto.RecordResponse toRecordResponse(AttendanceRecord r, List<AttendancePunch> punches) {
        List<AttendanceDto.PunchInfo> punchInfos = punches.stream()
                .map(p -> AttendanceDto.PunchInfo.builder()
                        .id(p.getId())
                        .punchTime(p.getPunchTime())
                        .type(p.getType())
                        .source(p.getSource())
                        .ipAddress(p.getIpAddress())
                        .latitude(p.getLatitude())
                        .longitude(p.getLongitude())
                        .build())
                .collect(Collectors.toList());

        return AttendanceDto.RecordResponse.builder()
                .id(r.getId())
                .employeeId(r.getEmployeeId())
                .attendanceDate(r.getAttendanceDate())
                .firstCheckIn(r.getFirstCheckIn())
                .lastCheckOut(r.getLastCheckOut())
                .totalHours(r.getTotalHours())
                .effectiveHours(r.getEffectiveHours())
                .breakHours(r.getBreakHours())
                .overtimeHours(r.getOvertimeHours())
                .status(r.getStatus())
                .lateByMins(r.getLateByMins())
                .earlyLeavingMins(r.getEarlyLeavingMins())
                .source(r.getSource())
                .regularized(r.isRegularized())
                .punches(punchInfos)
                .build();
    }

    private BigDecimal secondsToHours(long seconds) {
        return BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
    }

    private int count(List<AttendanceRecord> records, AttendanceRecord.AttendanceStatus status) {
        return (int) records.stream().filter(r -> r.getStatus() == status).count();
    }
}
