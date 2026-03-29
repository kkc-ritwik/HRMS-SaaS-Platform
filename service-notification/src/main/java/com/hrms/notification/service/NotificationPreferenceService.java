package com.hrms.notification.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.notification.dto.NotificationPreferenceDto;
import com.hrms.notification.entity.NotificationPreference;
import com.hrms.notification.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceDto.Response create(String tenantId, String currentUser,
                                                     NotificationPreferenceDto.CreateRequest request) {
        NotificationPreference preference = new NotificationPreference();
        preference.setTenantId(tenantId);
        preference.setCreatedBy(currentUser);
        preference.setUpdatedBy(currentUser);
        preference.setEmployeeId(request.getEmployeeId());
        preference.setNotificationType(request.getNotificationType());
        preference.setEmailEnabled(request.isEmailEnabled());
        preference.setPushEnabled(request.isPushEnabled());
        preference.setInAppEnabled(request.isInAppEnabled());
        preference.setSmsEnabled(request.isSmsEnabled());
        return toResponse(notificationPreferenceRepository.save(preference));
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceDto.Response getById(String tenantId, UUID id) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationPreference", "id", id));
        return toResponse(preference);
    }

    @Transactional(readOnly = true)
    public Page<NotificationPreferenceDto.Response> list(String tenantId, Pageable pageable) {
        return notificationPreferenceRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<NotificationPreferenceDto.Response> listForEmployee(String tenantId, UUID employeeId) {
        return notificationPreferenceRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalse(tenantId, employeeId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public NotificationPreferenceDto.Response update(String tenantId, UUID id, String currentUser,
                                                     NotificationPreferenceDto.UpdateRequest request) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationPreference", "id", id));
        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getPushEnabled() != null) {
            preference.setPushEnabled(request.getPushEnabled());
        }
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }
        if (request.getSmsEnabled() != null) {
            preference.setSmsEnabled(request.getSmsEnabled());
        }
        preference.setUpdatedBy(currentUser);
        return toResponse(notificationPreferenceRepository.save(preference));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        NotificationPreference preference = notificationPreferenceRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationPreference", "id", id));
        preference.setDeleted(true);
        preference.setUpdatedBy(currentUser);
        notificationPreferenceRepository.save(preference);
    }

    private NotificationPreferenceDto.Response toResponse(NotificationPreference p) {
        return NotificationPreferenceDto.Response.builder()
                .id(p.getId())
                .tenantId(p.getTenantId())
                .employeeId(p.getEmployeeId())
                .notificationType(p.getNotificationType())
                .emailEnabled(p.isEmailEnabled())
                .pushEnabled(p.isPushEnabled())
                .inAppEnabled(p.isInAppEnabled())
                .smsEnabled(p.isSmsEnabled())
                .createdBy(p.getCreatedBy())
                .updatedBy(p.getUpdatedBy())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
