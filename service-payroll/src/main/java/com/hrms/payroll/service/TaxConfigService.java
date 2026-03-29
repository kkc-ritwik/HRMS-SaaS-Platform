package com.hrms.payroll.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.payroll.dto.TaxConfigDto;
import com.hrms.payroll.entity.EsiConfig;
import com.hrms.payroll.entity.PfConfig;
import com.hrms.payroll.entity.PtSlab;
import com.hrms.payroll.repository.EsiConfigRepository;
import com.hrms.payroll.repository.PfConfigRepository;
import com.hrms.payroll.repository.PtSlabRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxConfigService {

    private final PfConfigRepository  pfConfigRepository;
    private final EsiConfigRepository esiConfigRepository;
    private final PtSlabRepository    ptSlabRepository;

    // ── PF Config ─────────────────────────────────────────────────────────────

    @Transactional
    public TaxConfigDto.PfConfigResponse savePfConfig(String tenantId,
                                                       TaxConfigDto.PfConfigRequest req,
                                                       String currentUser) {
        PfConfig config = new PfConfig();
        config.setTenantId(tenantId);
        config.setPfNumber(req.getPfNumber());
        if (req.getBasicWageCeiling() != null)       config.setBasicWageCeiling(req.getBasicWageCeiling());
        if (req.getPfRateEmployee() != null)         config.setPfRateEmployee(req.getPfRateEmployee());
        if (req.getPfRateEmployer() != null)         config.setPfRateEmployer(req.getPfRateEmployer());
        if (req.getEpsRate() != null)                config.setEpsRate(req.getEpsRate());
        if (req.getEdliRate() != null)               config.setEdliRate(req.getEdliRate());
        if (req.getAdminChargeRate() != null)        config.setAdminChargeRate(req.getAdminChargeRate());
        if (req.getIncludeEmployerPfInCtc() != null) config.setIncludeEmployerPfInCtc(req.getIncludeEmployerPfInCtc());
        config.setEffectiveFrom(req.getEffectiveFrom());
        config.setCreatedBy(currentUser);
        return toPfResponse(pfConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public TaxConfigDto.PfConfigResponse getCurrentPfConfig(String tenantId) {
        PfConfig config = pfConfigRepository
                .findFirstByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PfConfig", "tenantId", tenantId));
        return toPfResponse(config);
    }

    // ── ESI Config ────────────────────────────────────────────────────────────

    @Transactional
    public TaxConfigDto.EsiConfigResponse saveEsiConfig(String tenantId,
                                                         TaxConfigDto.EsiConfigRequest req,
                                                         String currentUser) {
        EsiConfig config = new EsiConfig();
        config.setTenantId(tenantId);
        config.setEsiNumber(req.getEsiNumber());
        if (req.getWageCeiling() != null)   config.setWageCeiling(req.getWageCeiling());
        if (req.getEmployeeRate() != null)  config.setEmployeeRate(req.getEmployeeRate());
        if (req.getEmployerRate() != null)  config.setEmployerRate(req.getEmployerRate());
        config.setEffectiveFrom(req.getEffectiveFrom());
        config.setCreatedBy(currentUser);
        return toEsiResponse(esiConfigRepository.save(config));
    }

    @Transactional(readOnly = true)
    public TaxConfigDto.EsiConfigResponse getCurrentEsiConfig(String tenantId) {
        EsiConfig config = esiConfigRepository
                .findFirstByTenantIdAndDeletedFalseOrderByEffectiveFromDesc(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EsiConfig", "tenantId", tenantId));
        return toEsiResponse(config);
    }

    // ── PT Slabs ──────────────────────────────────────────────────────────────

    @Transactional
    public TaxConfigDto.PtSlabResponse createPtSlab(String tenantId,
                                                     TaxConfigDto.PtSlabRequest req,
                                                     String currentUser) {
        PtSlab slab = new PtSlab();
        slab.setTenantId(tenantId);
        slab.setState(req.getState().toUpperCase());
        slab.setSlabFrom(req.getSlabFrom());
        slab.setSlabTo(req.getSlabTo());
        slab.setMonthlyTax(req.getMonthlyTax());
        slab.setGender(req.getGender());
        slab.setEffectiveFrom(req.getEffectiveFrom());
        slab.setCreatedBy(currentUser);
        return toPtResponse(ptSlabRepository.save(slab));
    }

    @Transactional(readOnly = true)
    public List<TaxConfigDto.PtSlabResponse> getPtSlabsByState(String tenantId, String state) {
        return ptSlabRepository
                .findByTenantIdAndStateAndDeletedFalseOrderBySlabFrom(tenantId, state.toUpperCase())
                .stream().map(this::toPtResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deletePtSlab(String tenantId, UUID id, String currentUser) {
        PtSlab slab = ptSlabRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("PtSlab", "id", id));
        slab.setDeleted(true);
        slab.setUpdatedBy(currentUser);
        ptSlabRepository.save(slab);
    }

    /** Calculate PT for a given monthly salary, state, gender, and date. Returns ZERO if no slab found. */
    @Transactional(readOnly = true)
    public BigDecimal calculatePt(String tenantId, String state, BigDecimal monthlySalary,
                                   String gender, LocalDate asOf) {
        return ptSlabRepository
                .findApplicableSlab(tenantId, state.toUpperCase(), monthlySalary, gender, asOf)
                .stream().findFirst()
                .map(PtSlab::getMonthlyTax)
                .orElse(BigDecimal.ZERO);
    }

    // ── Mapping helpers ───────────────────────────────────────────────────────

    private TaxConfigDto.PfConfigResponse toPfResponse(PfConfig c) {
        return TaxConfigDto.PfConfigResponse.builder()
                .id(c.getId())
                .pfNumber(c.getPfNumber())
                .basicWageCeiling(c.getBasicWageCeiling())
                .pfRateEmployee(c.getPfRateEmployee())
                .pfRateEmployer(c.getPfRateEmployer())
                .epsRate(c.getEpsRate())
                .edliRate(c.getEdliRate())
                .adminChargeRate(c.getAdminChargeRate())
                .includeEmployerPfInCtc(c.isIncludeEmployerPfInCtc())
                .effectiveFrom(c.getEffectiveFrom())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private TaxConfigDto.EsiConfigResponse toEsiResponse(EsiConfig c) {
        return TaxConfigDto.EsiConfigResponse.builder()
                .id(c.getId())
                .esiNumber(c.getEsiNumber())
                .wageCeiling(c.getWageCeiling())
                .employeeRate(c.getEmployeeRate())
                .employerRate(c.getEmployerRate())
                .effectiveFrom(c.getEffectiveFrom())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private TaxConfigDto.PtSlabResponse toPtResponse(PtSlab s) {
        return TaxConfigDto.PtSlabResponse.builder()
                .id(s.getId())
                .state(s.getState())
                .slabFrom(s.getSlabFrom())
                .slabTo(s.getSlabTo())
                .monthlyTax(s.getMonthlyTax())
                .gender(s.getGender())
                .effectiveFrom(s.getEffectiveFrom())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
