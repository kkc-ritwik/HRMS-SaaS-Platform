package com.hrms.notification.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.notification.dto.NotificationDto;
import com.hrms.notification.entity.Notification;
import com.hrms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationDto.Response create(String tenantId, String currentUser,
                                           NotificationDto.CreateRequest request) {
        Notification notification = new Notification();
        notification.setTenantId(tenantId);
        notification.setCreatedBy(currentUser);
        notification.setUpdatedBy(currentUser);
        notification.setEmployeeId(request.getEmployeeId());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setNotificationType(request.getNotificationType());
        notification.setReferenceType(request.getReferenceType());
        notification.setReferenceId(request.getReferenceId());
        notification.setSentAt(request.getSentAt());
        notification.setRead(false);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public NotificationDto.Response getById(String tenantId, UUID id) {
        Notification notification = notificationRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        return toResponse(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Response> list(String tenantId, Pageable pageable) {
        return notificationRepository
                .findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto.Response> listForEmployee(String tenantId, UUID employeeId,
                                                          Pageable pageable) {
        return notificationRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalseOrderByCreatedAtDesc(
                        tenantId, employeeId, pageable)
                .map(this::toResponse);
    }

    public NotificationDto.Response update(String tenantId, UUID id, String currentUser,
                                           NotificationDto.UpdateRequest request) {
        Notification notification = notificationRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        if (request.getRead() != null) {
            notification.setRead(request.getRead());
        }
        if (request.getReadAt() != null) {
            notification.setReadAt(request.getReadAt());
        }
        notification.setUpdatedBy(currentUser);
        return toResponse(notificationRepository.save(notification));
    }

    public NotificationDto.Response markRead(String tenantId, UUID id, String currentUser) {
        Notification notification = notificationRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        notification.setUpdatedBy(currentUser);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(String tenantId, UUID employeeId, String currentUser) {
        List<Notification> unread = notificationRepository
                .findByTenantIdAndEmployeeIdAndReadFalseAndDeletedFalse(tenantId, employeeId);
        Instant now = Instant.now();
        for (Notification n : unread) {
            n.setRead(true);
            n.setReadAt(now);
            n.setUpdatedBy(currentUser);
        }
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        Notification notification = notificationRepository
                .findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));
        notification.setDeleted(true);
        notification.setUpdatedBy(currentUser);
        notificationRepository.save(notification);
    }

    private NotificationDto.Response toResponse(Notification n) {
        return NotificationDto.Response.builder()
                .id(n.getId())
                .tenantId(n.getTenantId())
                .employeeId(n.getEmployeeId())
                .title(n.getTitle())
                .message(n.getMessage())
                .notificationType(n.getNotificationType())
                .referenceType(n.getReferenceType())
                .referenceId(n.getReferenceId())
                .read(n.isRead())
                .readAt(n.getReadAt())
                .sentAt(n.getSentAt())
                .createdBy(n.getCreatedBy())
                .updatedBy(n.getUpdatedBy())
                .createdAt(n.getCreatedAt())
                .updatedAt(n.getUpdatedAt())
                .build();
    }
}
