package com.hrms.cases.service;

import com.hrms.cases.entity.*;
import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CasesService {

    public interface HrCaseRepo extends JpaRepository<HrCase, UUID> {
        Page<HrCase> findByTenantId(String tenantId, Pageable p);
        Page<HrCase> findByTenantIdAndStatus(String tenantId, HrCase.Status s, Pageable p);
    }
    public interface CaseNoteRepo extends JpaRepository<CaseNote, UUID> {
        List<CaseNote> findByTenantIdAndCaseIdOrderByCreatedAtAsc(String tenantId, UUID caseId);
    }
    public interface IccCommitteeRepo extends JpaRepository<IccCommittee, UUID> {}

    private final HrCaseRepo cases;
    private final CaseNoteRepo notes;
    private final IccCommitteeRepo committees;
    private final EventPublisher events;

    @Transactional
    public HrCase fileCase(HrCase c) {
        c.setTenantId(TenantContext.get());
        c.setCaseNumber(generateCaseNumber(c.getType()));
        c.setStatus(HrCase.Status.NEW);
        if (c.getType() == HrCase.CaseType.POSH || c.getType() == HrCase.CaseType.HARASSMENT) {
            c.setSlaDueDate(OffsetDateTime.now().plusDays(90));
        }
        HrCase saved = cases.save(c);
        events.publish(Topics.CASES, DomainEvent.of("case.filed", "cases",
                saved.getTenantId(), saved.getId().toString(), "HrCase",
                Map.of("caseNumber", saved.getCaseNumber(), "type", saved.getType())));
        return saved;
    }

    @Transactional
    public HrCase updateStatus(UUID caseId, HrCase.Status status, String resolution) {
        HrCase c = cases.findById(caseId).orElseThrow();
        c.setStatus(status);
        if (status == HrCase.Status.RESOLVED || status == HrCase.Status.CLOSED) {
            c.setResolvedAt(OffsetDateTime.now());
            c.setResolution(resolution);
        }
        events.publish(Topics.CASES, DomainEvent.of("case.status_changed", "cases",
                c.getTenantId(), caseId.toString(), "HrCase", Map.of("status", status)));
        return cases.save(c);
    }

    @Transactional
    public CaseNote addNote(CaseNote n) {
        n.setTenantId(TenantContext.get());
        return notes.save(n);
    }

    public List<CaseNote> listNotes(UUID caseId) {
        return notes.findByTenantIdAndCaseIdOrderByCreatedAtAsc(TenantContext.get(), caseId);
    }

    public Page<HrCase> list(HrCase.Status status, Pageable p) {
        return status == null
                ? cases.findByTenantId(TenantContext.get(), p)
                : cases.findByTenantIdAndStatus(TenantContext.get(), status, p);
    }

    private String generateCaseNumber(HrCase.CaseType t) {
        String prefix = switch (t == null ? HrCase.CaseType.OTHER : t) {
            case POSH -> "POSH"; case HARASSMENT -> "HRS"; case GRIEVANCE -> "GRV";
            case DISCRIMINATION -> "DIS"; case ETHICS -> "ETH"; case WHISTLEBLOWER -> "WB";
            default -> "CASE";
        };
        return prefix + "-" + OffsetDateTime.now().toEpochSecond();
    }
}
