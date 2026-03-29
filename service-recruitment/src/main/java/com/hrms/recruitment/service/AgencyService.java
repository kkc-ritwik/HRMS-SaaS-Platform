package com.hrms.recruitment.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.recruitment.dto.AgencyDto;
import com.hrms.recruitment.entity.RecruitmentAgency;
import com.hrms.recruitment.repository.RecruitmentAgencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgencyService {

    private final RecruitmentAgencyRepository agencyRepository;

    @Transactional
    public AgencyDto.Response create(String tenantId, AgencyDto.CreateRequest req, String currentUser) {
        if (agencyRepository.existsByTenantIdAndNameAndDeletedFalse(tenantId, req.getName())) {
            throw new BusinessException("DUPLICATE_AGENCY", "Agency with name '" + req.getName() + "' already exists");
        }
        RecruitmentAgency agency = new RecruitmentAgency();
        agency.setTenantId(tenantId);
        agency.setName(req.getName());
        agency.setContactPerson(req.getContactPerson());
        agency.setContactEmail(req.getContactEmail());
        agency.setContactPhone(req.getContactPhone());
        agency.setCommissionPercent(req.getCommissionPercent() != null ? req.getCommissionPercent() : java.math.BigDecimal.ZERO);
        agency.setActive(true);
        agency.setCreatedBy(currentUser);
        return toResponse(agencyRepository.save(agency));
    }

    @Transactional
    public AgencyDto.Response update(String tenantId, UUID id, AgencyDto.UpdateRequest req, String currentUser) {
        RecruitmentAgency agency = getEntity(tenantId, id);
        if (req.getName() != null)            agency.setName(req.getName());
        if (req.getContactPerson() != null)   agency.setContactPerson(req.getContactPerson());
        if (req.getContactEmail() != null)    agency.setContactEmail(req.getContactEmail());
        if (req.getContactPhone() != null)    agency.setContactPhone(req.getContactPhone());
        if (req.getCommissionPercent() != null) agency.setCommissionPercent(req.getCommissionPercent());
        if (req.getActive() != null)          agency.setActive(req.getActive());
        agency.setUpdatedBy(currentUser);
        return toResponse(agencyRepository.save(agency));
    }

    @Transactional(readOnly = true)
    public AgencyDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<AgencyDto.Response> list(String tenantId, Pageable pageable) {
        return agencyRepository.findByTenantIdAndDeletedFalseOrderByNameAsc(tenantId, pageable)
                .map(this::toResponse);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private RecruitmentAgency getEntity(String tenantId, UUID id) {
        return agencyRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("RecruitmentAgency", "id", id));
    }

    private AgencyDto.Response toResponse(RecruitmentAgency a) {
        return AgencyDto.Response.builder()
                .id(a.getId())
                .name(a.getName())
                .contactPerson(a.getContactPerson())
                .contactEmail(a.getContactEmail())
                .contactPhone(a.getContactPhone())
                .commissionPercent(a.getCommissionPercent())
                .active(a.isActive())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
