package com.hrms.leave.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.DuplicateResourceException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.ShiftDto;
import com.hrms.leave.entity.Shift;
import com.hrms.leave.entity.ShiftSchedule;
import com.hrms.leave.repository.ShiftRepository;
import com.hrms.leave.repository.ShiftScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftScheduleRepository scheduleRepository;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Transactional
    public ShiftDto.Response createShift(String tenantId, ShiftDto.CreateRequest req, String currentUser) {
        if (shiftRepository.existsByCodeAndTenantIdAndDeletedFalse(req.getCode(), tenantId)) {
            throw new DuplicateResourceException("Shift", "code", req.getCode());
        }
        Shift shift = new Shift();
        shift.setTenantId(tenantId);
        shift.setName(req.getName());
        shift.setCode(req.getCode());
        shift.setStartTime(req.getStartTime());
        shift.setEndTime(req.getEndTime());
        shift.setBreakDurationMins(req.getBreakDurationMins());
        shift.setMinHoursFullDay(req.getMinHoursFullDay());
        shift.setMinHoursHalfDay(req.getMinHoursHalfDay());
        shift.setGracePeriodMins(req.getGracePeriodMins());
        shift.setOvertimeThresholdMins(req.getOvertimeThresholdMins());
        shift.setFlexible(req.isFlexible());
        shift.setFlexStartTime(req.getFlexStartTime());
        shift.setFlexEndTime(req.getFlexEndTime());
        shift.setCoreStartTime(req.getCoreStartTime());
        shift.setCoreEndTime(req.getCoreEndTime());
        shift.setNightShift(req.isNightShift());
        shift.setColor(req.getColor());
        shift.setCreatedBy(currentUser);
        return toResponse(shiftRepository.save(shift));
    }

    @Transactional
    public ShiftDto.Response updateShift(String tenantId, UUID id, ShiftDto.UpdateRequest req, String currentUser) {
        Shift shift = getShiftEntity(tenantId, id);
        if (req.getName() != null)                 shift.setName(req.getName());
        if (req.getStartTime() != null)            shift.setStartTime(req.getStartTime());
        if (req.getEndTime() != null)              shift.setEndTime(req.getEndTime());
        if (req.getBreakDurationMins() != null)    shift.setBreakDurationMins(req.getBreakDurationMins());
        if (req.getMinHoursFullDay() != null)      shift.setMinHoursFullDay(req.getMinHoursFullDay());
        if (req.getMinHoursHalfDay() != null)      shift.setMinHoursHalfDay(req.getMinHoursHalfDay());
        if (req.getGracePeriodMins() != null)      shift.setGracePeriodMins(req.getGracePeriodMins());
        if (req.getOvertimeThresholdMins() != null) shift.setOvertimeThresholdMins(req.getOvertimeThresholdMins());
        if (req.getFlexible() != null)             shift.setFlexible(req.getFlexible());
        if (req.getFlexStartTime() != null)        shift.setFlexStartTime(req.getFlexStartTime());
        if (req.getFlexEndTime() != null)          shift.setFlexEndTime(req.getFlexEndTime());
        if (req.getCoreStartTime() != null)        shift.setCoreStartTime(req.getCoreStartTime());
        if (req.getCoreEndTime() != null)          shift.setCoreEndTime(req.getCoreEndTime());
        if (req.getNightShift() != null)           shift.setNightShift(req.getNightShift());
        if (req.getColor() != null)                shift.setColor(req.getColor());
        if (req.getActive() != null)               shift.setActive(req.getActive());
        shift.setUpdatedBy(currentUser);
        return toResponse(shiftRepository.save(shift));
    }

    @Transactional(readOnly = true)
    public ShiftDto.Response getShift(String tenantId, UUID id) {
        return toResponse(getShiftEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<ShiftDto.Response> listShifts(String tenantId, Pageable pageable) {
        return shiftRepository.findByTenantIdAndDeletedFalse(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ShiftDto.Response> listActiveShifts(String tenantId) {
        return shiftRepository.findByTenantIdAndActiveAndDeletedFalse(tenantId, true)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteShift(String tenantId, UUID id, String currentUser) {
        Shift shift = getShiftEntity(tenantId, id);
        shift.setDeleted(true);
        shift.setUpdatedBy(currentUser);
        shiftRepository.save(shift);
    }

    // ── Assignment ────────────────────────────────────────────────────────────

    /**
     * Assign a shift (or mark week-off / holiday) for an employee across a date range.
     * A null shiftId with weekOff=true or holiday=true marks the day accordingly.
     */
    @Transactional
    public void assignShiftToEmployee(String tenantId, UUID shiftId, ShiftDto.AssignRequest req,
                                       String currentUser) {
        // If a shiftId is provided, verify it exists
        if (shiftId != null) {
            getShiftEntity(tenantId, shiftId);
        }

        LocalDate toDate = req.getToDate() != null ? req.getToDate() : req.getFromDate();
        if (toDate.isBefore(req.getFromDate())) {
            throw new BusinessException("INVALID_DATE_RANGE", "toDate must be on or after fromDate");
        }

        LocalDate cursor = req.getFromDate();
        while (!cursor.isAfter(toDate)) {
            final LocalDate date = cursor;
            ShiftSchedule schedule = scheduleRepository
                    .findByEmployeeIdAndScheduleDateAndTenantIdAndDeletedFalse(req.getEmployeeId(), date, tenantId)
                    .orElseGet(() -> {
                        ShiftSchedule s = new ShiftSchedule();
                        s.setTenantId(tenantId);
                        s.setEmployeeId(req.getEmployeeId());
                        s.setScheduleDate(date);
                        s.setCreatedBy(currentUser);
                        return s;
                    });
            schedule.setShiftId(shiftId);
            schedule.setWeekOff(req.isWeekOff());
            schedule.setHoliday(req.isHoliday());
            schedule.setAssignedBy(currentUser);
            schedule.setUpdatedBy(currentUser);
            scheduleRepository.save(schedule);
            cursor = cursor.plusDays(1);
        }
        log.info("Shift assigned: tenantId={} employee={} shift={} from={} to={}",
                tenantId, req.getEmployeeId(), shiftId, req.getFromDate(), toDate);
    }

    /**
     * Returns the Shift for an employee on a given date, if a schedule entry exists.
     */
    @Transactional(readOnly = true)
    public Optional<Shift> getEmployeeShiftForDate(String tenantId, UUID employeeId, LocalDate date) {
        return scheduleRepository
                .findByEmployeeIdAndScheduleDateAndTenantIdAndDeletedFalse(employeeId, date, tenantId)
                .filter(ss -> ss.getShiftId() != null)
                .flatMap(ss -> shiftRepository.findByIdAndTenantIdAndDeletedFalse(ss.getShiftId(), tenantId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Shift getShiftEntity(String tenantId, UUID id) {
        return shiftRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
    }

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

    private ShiftDto.Response toResponse(Shift s) {
        return ShiftDto.Response.builder()
                .id(s.getId())
                .name(s.getName())
                .code(s.getCode())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .breakDurationMins(s.getBreakDurationMins())
                .minHoursFullDay(s.getMinHoursFullDay())
                .minHoursHalfDay(s.getMinHoursHalfDay())
                .gracePeriodMins(s.getGracePeriodMins())
                .overtimeThresholdMins(s.getOvertimeThresholdMins())
                .flexible(s.isFlexible())
                .flexStartTime(s.getFlexStartTime())
                .flexEndTime(s.getFlexEndTime())
                .coreStartTime(s.getCoreStartTime())
                .coreEndTime(s.getCoreEndTime())
                .nightShift(s.isNightShift())
                .color(s.getColor())
                .active(s.isActive())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
