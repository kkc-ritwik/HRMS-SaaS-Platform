package com.hrms.leave.service;

import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.CompOffDto;
import com.hrms.leave.entity.CompOffRequest;
import com.hrms.leave.repository.CompOffRequestRepository;
import com.hrms.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompOffService {

    private final CompOffRequestRepository compOffRepository;
    private final LeaveTypeRepository      leaveTypeRepository;
    private final LeaveBalanceService      balanceService;

    /** Employee submits a comp-off request for a day they worked on a holiday/weekend. */
    @Transactional
    public CompOffDto.Response request(String tenantId, UUID employeeId,
                                        CompOffDto.RequestDto req, String currentUser) {
        CompOffRequest entity = new CompOffRequest();
        entity.setTenantId(tenantId);
        entity.setEmployeeId(employeeId);
        entity.setWorkedDate(req.getWorkedDate());
        entity.setReason(req.getReason());
        entity.setExpiresAt(req.getExpiresAt() != null
                ? req.getExpiresAt()
                : req.getWorkedDate().plusMonths(3)); // default 3-month expiry
        entity.setStatus(CompOffRequest.CompOffStatus.PENDING);
        entity.setCreatedBy(currentUser);
        entity.setUpdatedBy(currentUser);
        return toResponse(compOffRepository.save(entity));
    }

    /** Manager approves comp-off → credit 1 day to comp-off leave type balance. */
    @Transactional
    public CompOffDto.Response approve(String tenantId, UUID requestId,
                                        UUID compOffLeaveTypeId,
                                        UUID approverId, String currentUser) {
        CompOffRequest entity = findOrThrow(tenantId, requestId);
        if (entity.getStatus() != CompOffRequest.CompOffStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "Only PENDING comp-off requests can be approved");
        }
        // Validate leave type exists
        leaveTypeRepository.findByIdAndTenantIdAndDeletedFalse(compOffLeaveTypeId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveType", "id", compOffLeaveTypeId));

        entity.setStatus(CompOffRequest.CompOffStatus.APPROVED);
        entity.setApprovedBy(approverId);
        entity.setUpdatedBy(currentUser);
        compOffRepository.save(entity);

        // Credit 1 comp-off day to balance
        balanceService.creditCompOff(tenantId, entity.getEmployeeId(),
                compOffLeaveTypeId, BigDecimal.ONE, currentUser);

        log.info("[CompOff] Request {} approved by {}, credited 1 day for employee {}",
                requestId, approverId, entity.getEmployeeId());
        return toResponse(entity);
    }

    /** Manager rejects comp-off request. */
    @Transactional
    public CompOffDto.Response reject(String tenantId, UUID requestId, String currentUser) {
        CompOffRequest entity = findOrThrow(tenantId, requestId);
        if (entity.getStatus() != CompOffRequest.CompOffStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS", "Only PENDING comp-off requests can be rejected");
        }
        entity.setStatus(CompOffRequest.CompOffStatus.REJECTED);
        entity.setUpdatedBy(currentUser);
        return toResponse(compOffRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<CompOffDto.Response> getMyRequests(String tenantId, UUID employeeId,
                                                    Pageable pageable) {
        return compOffRepository.findByEmployeeIdAndTenantIdAndDeletedFalse(
                employeeId, tenantId, pageable).map(this::toResponse);
    }

    // ── private ───────────────────────────────────────────────────────────────

    private CompOffRequest findOrThrow(String tenantId, UUID id) {
        return compOffRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("CompOffRequest", "id", id));
    }

    private CompOffDto.Response toResponse(CompOffRequest e) {
        return CompOffDto.Response.builder()
                .id(e.getId()).employeeId(e.getEmployeeId())
                .workedDate(e.getWorkedDate()).reason(e.getReason())
                .expiresAt(e.getExpiresAt()).status(e.getStatus())
                .approvedBy(e.getApprovedBy()).createdAt(e.getCreatedAt())
                .build();
    }
}
