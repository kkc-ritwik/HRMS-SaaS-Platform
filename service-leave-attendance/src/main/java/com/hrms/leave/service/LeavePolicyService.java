package com.hrms.leave.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.leave.dto.LeavePolicyDto;
import com.hrms.leave.entity.LeavePolicy;
import com.hrms.leave.repository.LeavePolicyRepository;
import com.hrms.leave.repository.LeaveTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeavePolicyService {

    private final LeavePolicyRepository policyRepository;
    private final LeaveTypeRepository   leaveTypeRepository;
    private final LeaveTypeService      leaveTypeService;

    @Transactional
    public LeavePolicyDto.Response create(String tenantId, LeavePolicyDto.CreateRequest req,
                                           String currentUser) {
        // Validate leave type exists in tenant
        leaveTypeService.findOrThrow(tenantId, req.getLeaveTypeId());

        LeavePolicy policy = new LeavePolicy();
        policy.setTenantId(tenantId);
        mapRequest(req, policy);
        policy.setCreatedBy(currentUser);
        policy.setUpdatedBy(currentUser);
        return toResponse(policyRepository.save(policy), tenantId);
    }

    @Transactional(readOnly = true)
    public Page<LeavePolicyDto.Response> list(String tenantId, Pageable pageable) {
        return policyRepository.findByTenantIdAndDeletedFalse(tenantId, pageable)
                .map(p -> toResponse(p, tenantId));
    }

    @Transactional(readOnly = true)
    public LeavePolicyDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id), tenantId);
    }

    @Transactional
    public LeavePolicyDto.Response update(String tenantId, UUID id,
                                           LeavePolicyDto.UpdateRequest req, String currentUser) {
        LeavePolicy policy = findOrThrow(tenantId, id);
        if (req.getName() != null)                   policy.setName(req.getName());
        if (req.getAccrualType() != null)            policy.setAccrualType(req.getAccrualType());
        if (req.getAccrualAmount() != null)          policy.setAccrualAmount(req.getAccrualAmount());
        if (req.getMaxAccrual() != null)             policy.setMaxAccrual(req.getMaxAccrual());
        if (req.getCarryForwardEnabled() != null)    policy.setCarryForwardEnabled(req.getCarryForwardEnabled());
        if (req.getMaxCarryForward() != null)        policy.setMaxCarryForward(req.getMaxCarryForward());
        if (req.getCarryForwardExpiryMonths() != null) policy.setCarryForwardExpiryMonths(req.getCarryForwardExpiryMonths());
        if (req.getEncashmentEnabled() != null)      policy.setEncashmentEnabled(req.getEncashmentEnabled());
        if (req.getMaxEncashmentDays() != null)      policy.setMaxEncashmentDays(req.getMaxEncashmentDays());
        if (req.getNegativeBalanceAllowed() != null) policy.setNegativeBalanceAllowed(req.getNegativeBalanceAllowed());
        if (req.getMaxNegativeDays() != null)        policy.setMaxNegativeDays(req.getMaxNegativeDays());
        if (req.getSandwichRuleEnabled() != null)    policy.setSandwichRuleEnabled(req.getSandwichRuleEnabled());
        if (req.getMinNoticeDays() != null)          policy.setMinNoticeDays(req.getMinNoticeDays());
        if (req.getAllowHalfDay() != null)            policy.setAllowHalfDay(req.getAllowHalfDay());
        if (req.getAllowShortLeave() != null)         policy.setAllowShortLeave(req.getAllowShortLeave());
        if (req.getEffectiveTo() != null)            policy.setEffectiveTo(req.getEffectiveTo());
        if (req.getActive() != null)                 policy.setActive(req.getActive());
        policy.setUpdatedBy(currentUser);
        return toResponse(policyRepository.save(policy), tenantId);
    }

    @Transactional
    public void delete(String tenantId, UUID id, String currentUser) {
        LeavePolicy policy = findOrThrow(tenantId, id);
        policy.setDeleted(true);
        policy.setUpdatedBy(currentUser);
        policyRepository.save(policy);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize()).total(page.getTotalElements())
                .totalPages(page.getTotalPages()).hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    /** Find the currently active policy for a leave type. */
    public Optional<LeavePolicy> findActivePolicy(String tenantId, UUID leaveTypeId) {
        return policyRepository
                .findFirstByLeaveTypeIdAndTenantIdAndActiveAndDeletedFalseAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        leaveTypeId, tenantId, true, LocalDate.now());
    }

    // ── private ───────────────────────────────────────────────────────────────

    private LeavePolicy findOrThrow(String tenantId, UUID id) {
        return policyRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("LeavePolicy", "id", id));
    }

    private void mapRequest(LeavePolicyDto.CreateRequest req, LeavePolicy policy) {
        policy.setName(req.getName());
        policy.setLeaveTypeId(req.getLeaveTypeId());
        policy.setAccrualType(req.getAccrualType());
        policy.setAccrualAmount(req.getAccrualAmount());
        policy.setMaxAccrual(req.getMaxAccrual());
        policy.setCarryForwardEnabled(req.isCarryForwardEnabled());
        policy.setMaxCarryForward(req.getMaxCarryForward());
        policy.setCarryForwardExpiryMonths(req.getCarryForwardExpiryMonths());
        policy.setEncashmentEnabled(req.isEncashmentEnabled());
        policy.setMaxEncashmentDays(req.getMaxEncashmentDays());
        policy.setProRataOnJoining(req.isProRataOnJoining());
        policy.setProRataOnExit(req.isProRataOnExit());
        policy.setNegativeBalanceAllowed(req.isNegativeBalanceAllowed());
        policy.setMaxNegativeDays(req.getMaxNegativeDays());
        policy.setSandwichRuleEnabled(req.isSandwichRuleEnabled());
        policy.setMinNoticeDays(req.getMinNoticeDays());
        policy.setAllowHalfDay(req.isAllowHalfDay());
        policy.setAllowShortLeave(req.isAllowShortLeave());
        policy.setEffectiveFrom(req.getEffectiveFrom());
        policy.setEffectiveTo(req.getEffectiveTo());
        policy.setActive(true);
    }

    private LeavePolicyDto.Response toResponse(LeavePolicy p, String tenantId) {
        String ltName = leaveTypeRepository.findById(p.getLeaveTypeId())
                .map(lt -> lt.getName()).orElse(null);
        return LeavePolicyDto.Response.builder()
                .id(p.getId()).name(p.getName())
                .leaveTypeId(p.getLeaveTypeId()).leaveTypeName(ltName)
                .accrualType(p.getAccrualType()).accrualAmount(p.getAccrualAmount())
                .maxAccrual(p.getMaxAccrual())
                .carryForwardEnabled(p.isCarryForwardEnabled())
                .maxCarryForward(p.getMaxCarryForward())
                .carryForwardExpiryMonths(p.getCarryForwardExpiryMonths())
                .encashmentEnabled(p.isEncashmentEnabled())
                .maxEncashmentDays(p.getMaxEncashmentDays())
                .proRataOnJoining(p.isProRataOnJoining()).proRataOnExit(p.isProRataOnExit())
                .negativeBalanceAllowed(p.isNegativeBalanceAllowed())
                .maxNegativeDays(p.getMaxNegativeDays())
                .sandwichRuleEnabled(p.isSandwichRuleEnabled())
                .minNoticeDays(p.getMinNoticeDays())
                .allowHalfDay(p.isAllowHalfDay()).allowShortLeave(p.isAllowShortLeave())
                .effectiveFrom(p.getEffectiveFrom()).effectiveTo(p.getEffectiveTo())
                .active(p.isActive())
                .createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
                .build();
    }
}
