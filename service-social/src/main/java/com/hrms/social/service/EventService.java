package com.hrms.social.service;

import com.hrms.common.exception.ResourceNotFoundException;
import com.hrms.social.dto.EventDto;
import com.hrms.social.entity.Event;
import com.hrms.social.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventDto.Response create(String tenantId, EventDto.CreateRequest request, String currentUser) {
        Event event = new Event();
        event.setTenantId(tenantId);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setOrganizerId(request.getOrganizerId());
        event.setEventType(request.getEventType() != null ? request.getEventType() : Event.EventType.SOCIAL);
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setVirtualLink(request.getVirtualLink());
        event.setRsvpCount(0);
        event.setGroupId(request.getGroupId());
        event.setCreatedBy(currentUser);
        event.setUpdatedBy(currentUser);
        return toResponse(eventRepository.save(event));
    }

    public EventDto.Response getById(String tenantId, UUID id) {
        return toResponse(findOrThrow(tenantId, id));
    }

    /** RSVP to an event — increments (attending) or decrements (not attending) the count. */
    public EventDto.Response rsvp(String tenantId, UUID id, boolean attending, String currentUser) {
        Event event = findOrThrow(tenantId, id);
        int next = event.getRsvpCount() + (attending ? 1 : -1);
        event.setRsvpCount(Math.max(0, next));
        event.setUpdatedBy(currentUser);
        return toResponse(eventRepository.save(event));
    }

    public Page<EventDto.Response> list(String tenantId, Pageable pageable) {
        return eventRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId, pageable)
                .map(this::toResponse);
    }

    public List<EventDto.Response> listAll(String tenantId) {
        return eventRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtDesc(tenantId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EventDto.Response> listByOrganizer(String tenantId, UUID organizerId) {
        return eventRepository.findByTenantIdAndOrganizerIdAndDeletedFalse(tenantId, organizerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EventDto.Response> listByGroup(String tenantId, UUID groupId) {
        return eventRepository.findByTenantIdAndGroupIdAndDeletedFalse(tenantId, groupId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public EventDto.Response update(String tenantId, UUID id, EventDto.UpdateRequest request, String currentUser) {
        Event event = findOrThrow(tenantId, id);
        if (request.getTitle() != null) event.setTitle(request.getTitle());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getEventType() != null) event.setEventType(request.getEventType());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());
        if (request.getLocation() != null) event.setLocation(request.getLocation());
        if (request.getVirtualLink() != null) event.setVirtualLink(request.getVirtualLink());
        if (request.getGroupId() != null) event.setGroupId(request.getGroupId());
        event.setUpdatedBy(currentUser);
        return toResponse(eventRepository.save(event));
    }

    public void delete(String tenantId, UUID id, String currentUser) {
        Event event = findOrThrow(tenantId, id);
        event.setDeleted(true);
        event.setUpdatedBy(currentUser);
        eventRepository.save(event);
    }

    private Event findOrThrow(String tenantId, UUID id) {
        return eventRepository.findByIdAndTenantIdAndDeletedFalse(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));
    }

    private EventDto.Response toResponse(Event event) {
        return EventDto.Response.builder()
                .id(event.getId())
                .tenantId(event.getTenantId())
                .title(event.getTitle())
                .description(event.getDescription())
                .organizerId(event.getOrganizerId())
                .eventType(event.getEventType())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .virtualLink(event.getVirtualLink())
                .rsvpCount(event.getRsvpCount())
                .groupId(event.getGroupId())
                .createdBy(event.getCreatedBy())
                .updatedBy(event.getUpdatedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
