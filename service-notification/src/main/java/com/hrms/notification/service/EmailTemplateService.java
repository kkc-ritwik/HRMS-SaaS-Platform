package com.hrms.notification.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.notification.dto.EmailTemplateDto;
import com.hrms.notification.entity.EmailTemplate;
import com.hrms.notification.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;

    public EmailTemplateDto.Response create(String tenantId, String currentUser,
                                            EmailTemplateDto.CreateRequest request) {
        EmailTemplate template = new EmailTemplate();
        template.setTenantId(tenantId);
        template.setCreatedBy(currentUser);
        template.setUpdatedBy(currentUser);
        template.setName(request.getName());
        template.setCode(request.getCode());
        template.setSubject(request.getSubject());
        template.setBodyHtml(request.getBodyHtml());
        template.setBodyText(request.getBodyText());
        template.setVariables(request.getVariables());
        template.setCategory(request.getCategory());
        template.setActive(request.isActive());
        return toResponse(emailTemplateRepository.save(template));
    }

    @Transactional(readOnly = true)
    public EmailTemplateDto.Response getById(String tenantId, UUID id) {
        EmailTemplate template = emailTemplateRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        return toResponse(template);
    }

    @Transactional(readOnly = true)
    public Page<EmailTemplateDto.Response> list(String tenantId, Pageable pageable) {
        return emailTemplateRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public EmailTemplateDto.Response findByCode(String tenantId, String code) {
        EmailTemplate template = emailTemplateRepository
                .findByCodeAndTenantIdAndDeletedFalse(code, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "code", code));
        return toResponse(template);
    }

    public EmailTemplateDto.Response update(String tenantId, UUID id, String currentUser,
                                            EmailTemplateDto.UpdateRequest request) {
        EmailTemplate template = emailTemplateRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getSubject() != null) {
            template.setSubject(request.getSubject());
        }
        if (request.getBodyHtml() != null) {
            template.setBodyHtml(request.getBodyHtml());
        }
        if (request.getBodyText() != null) {
            template.setBodyText(request.getBodyText());
        }
        if (request.getVariables() != null) {
            template.setVariables(request.getVariables());
        }
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }
        if (request.getActive() != null) {
            template.setActive(request.getActive());
        }
        template.setUpdatedBy(currentUser);
        return toResponse(emailTemplateRepository.save(template));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        EmailTemplate template = emailTemplateRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "id", id));
        template.setDeleted(true);
        template.setUpdatedBy(currentUser);
        emailTemplateRepository.save(template);
    }

    private EmailTemplateDto.Response toResponse(EmailTemplate t) {
        return EmailTemplateDto.Response.builder()
                .id(t.getId())
                .tenantId(t.getTenantId())
                .name(t.getName())
                .code(t.getCode())
                .subject(t.getSubject())
                .bodyHtml(t.getBodyHtml())
                .bodyText(t.getBodyText())
                .variables(t.getVariables())
                .category(t.getCategory())
                .active(t.isActive())
                .createdBy(t.getCreatedBy())
                .updatedBy(t.getUpdatedBy())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
