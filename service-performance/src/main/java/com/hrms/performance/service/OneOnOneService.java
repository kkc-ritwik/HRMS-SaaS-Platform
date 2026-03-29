package com.hrms.performance.service;

import com.hrms.common.dto.PaginationMeta;
import com.hrms.common.exception.BusinessException;
import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.performance.dto.OneOnOneDto;
import com.hrms.performance.entity.OneOnOne;
import com.hrms.performance.repository.OneOnOneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OneOnOneService {

    private final OneOnOneRepository oneOnOneRepository;

    @Transactional
    public OneOnOneDto.Response schedule(String tenantId, UUID managerId,
                                          OneOnOneDto.ScheduleRequest req, String currentUser) {
        OneOnOne m = new OneOnOne();
        m.setTenantId(tenantId);
        m.setManagerId(managerId);
        m.setEmployeeId(req.getEmployeeId());
        m.setScheduledAt(req.getScheduledAt());
        m.setDurationMinutes(req.getDurationMinutes());
        m.setAgenda(req.getAgenda());
        m.setStatus(OneOnOne.MeetingStatus.SCHEDULED);
        m.setNextMeetingDate(req.getNextMeetingDate());
        m.setCreatedBy(currentUser);
        return toResponse(oneOnOneRepository.save(m));
    }

    @Transactional
    public OneOnOneDto.Response update(String tenantId, UUID id,
                                        OneOnOneDto.UpdateRequest req, String currentUser) {
        OneOnOne m = getEntity(tenantId, id);
        if (m.getStatus() == OneOnOne.MeetingStatus.COMPLETED ||
            m.getStatus() == OneOnOne.MeetingStatus.CANCELLED) {
            throw new BusinessException("MEETING_TERMINAL", "Meeting is already " + m.getStatus());
        }
        if (req.getScheduledAt() != null)   m.setScheduledAt(req.getScheduledAt());
        if (req.getDurationMinutes() > 0)   m.setDurationMinutes(req.getDurationMinutes());
        if (req.getStatus() != null)        m.setStatus(req.getStatus());
        if (req.getAgenda() != null)        m.setAgenda(req.getAgenda());
        if (req.getManagerNotes() != null)  m.setManagerNotes(req.getManagerNotes());
        if (req.getEmployeeNotes() != null) m.setEmployeeNotes(req.getEmployeeNotes());
        if (req.getActionItems() != null)   m.setActionItems(req.getActionItems());
        if (req.getNextMeetingDate() != null) m.setNextMeetingDate(req.getNextMeetingDate());
        m.setUpdatedBy(currentUser);
        return toResponse(oneOnOneRepository.save(m));
    }

    @Transactional
    public OneOnOneDto.Response complete(String tenantId, UUID id, String currentUser) {
        OneOnOne m = getEntity(tenantId, id);
        m.setStatus(OneOnOne.MeetingStatus.COMPLETED);
        m.setUpdatedBy(currentUser);
        return toResponse(oneOnOneRepository.save(m));
    }

    @Transactional(readOnly = true)
    public OneOnOneDto.Response get(String tenantId, UUID id) {
        return toResponse(getEntity(tenantId, id));
    }

    @Transactional(readOnly = true)
    public Page<OneOnOneDto.Response> listForEmployee(String tenantId, UUID employeeId,
                                                       Pageable pageable) {
        return oneOnOneRepository
                .findByTenantIdAndEmployeeIdAndDeletedFalseOrderByScheduledAtDesc(
                        tenantId, employeeId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OneOnOneDto.Response> listForManager(String tenantId, UUID managerId,
                                                      Pageable pageable) {
        return oneOnOneRepository
                .findByTenantIdAndManagerIdAndDeletedFalseOrderByScheduledAtDesc(
                        tenantId, managerId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OneOnOneDto.Response> listBetween(String tenantId, UUID managerId, UUID employeeId,
                                                   Pageable pageable) {
        return oneOnOneRepository
                .findByTenantIdAndManagerIdAndEmployeeIdAndDeletedFalseOrderByScheduledAtDesc(
                        tenantId, managerId, employeeId, pageable)
                .map(this::toResponse);
    }

    public PaginationMeta buildMeta(Page<?> page) {
        return PaginationMeta.builder()
                .page(page.getNumber()).size(page.getSize())
                .total(page.getTotalElements()).totalPages(page.getTotalPages())
                .hasNext(page.hasNext()).hasPrevious(page.hasPrevious())
                .build();
    }

    private OneOnOne getEntity(String tenantId, UUID id) {
        return oneOnOneRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("OneOnOne", "id", id));
    }

    private OneOnOneDto.Response toResponse(OneOnOne m) {
        return OneOnOneDto.Response.builder()
                .id(m.getId()).managerId(m.getManagerId()).employeeId(m.getEmployeeId())
                .scheduledAt(m.getScheduledAt()).durationMinutes(m.getDurationMinutes())
                .status(m.getStatus()).agenda(m.getAgenda())
                .managerNotes(m.getManagerNotes()).employeeNotes(m.getEmployeeNotes())
                .actionItems(m.getActionItems()).nextMeetingDate(m.getNextMeetingDate())
                .createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt())
                .build();
    }
}
