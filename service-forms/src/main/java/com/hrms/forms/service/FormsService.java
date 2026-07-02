package com.hrms.forms.service;

import com.hrms.events.model.DomainEvent;
import com.hrms.events.model.Topics;
import com.hrms.events.publisher.EventPublisher;
import com.hrms.forms.entity.FormDefinition;
import com.hrms.forms.entity.FormSubmission;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FormsService {

    public interface FormDefRepo extends JpaRepository<FormDefinition, UUID> {
        Optional<FormDefinition> findFirstByTenantIdAndCodeOrderByVersionDesc(String tenantId, String code);
        Page<FormDefinition> findByTenantIdAndStatus(String tenantId, FormDefinition.Status s, Pageable p);
        List<FormDefinition> findByTenantId(String tenantId);
    }
    public interface SubmissionRepo extends JpaRepository<FormSubmission, UUID> {
        Page<FormSubmission> findByTenantIdAndFormId(String tenantId, UUID formId, Pageable p);
        List<FormSubmission> findByTenantIdAndSubmitterId(String tenantId, UUID submitterId);
    }

    private final FormDefRepo defs;
    private final SubmissionRepo subs;
    private final EventPublisher events;

    @Transactional
    public FormDefinition createOrUpdate(FormDefinition d) {
        d.setTenantId(TenantContext.get());
        if (d.getVersion() == null) d.setVersion(1);
        if (d.getStatus() == null) d.setStatus(FormDefinition.Status.DRAFT);
        return defs.save(d);
    }
    @Transactional
    public FormDefinition publish(UUID id) {
        FormDefinition d = defs.findById(id).orElseThrow(() -> new ResourceNotFoundException("Form", "id", id));
        d.setStatus(FormDefinition.Status.PUBLISHED); d.setIsActive(true);
        return defs.save(d);
    }
    public FormDefinition latestPublished(String code) {
        return defs.findFirstByTenantIdAndCodeOrderByVersionDesc(TenantContext.get(), code)
                .orElseThrow(() -> new ResourceNotFoundException("Form", "code", code));
    }

    @Transactional
    public FormSubmission submit(FormSubmission s) {
        s.setTenantId(TenantContext.get());
        FormSubmission saved = subs.save(s);
        events.publish(Topics.WORKFLOW, DomainEvent.of("form.submitted", "forms",
                s.getTenantId(), saved.getId().toString(), "FormSubmission",
                Map.of("formId", saved.getFormId(), "submitterId", saved.getSubmitterId())));
        return saved;
    }

    public Page<FormSubmission> submissions(UUID formId, Pageable p) {
        return subs.findByTenantIdAndFormId(TenantContext.get(), formId, p);
    }

    public List<FormDefinition> listAll() {
        return defs.findByTenantId(TenantContext.get());
    }
}
