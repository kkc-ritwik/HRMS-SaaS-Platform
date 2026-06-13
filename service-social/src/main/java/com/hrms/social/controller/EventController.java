package com.hrms.social.controller;

import com.hrms.common.dto.ApiResponse;
import com.hrms.security.model.TenantContext;
import com.hrms.security.model.UserPrincipal;
import com.hrms.social.dto.EventDto;
import com.hrms.social.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<EventDto.Response>> create(
            @Valid @RequestBody EventDto.CreateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        EventDto.Response response = eventService.create(tenantId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<EventDto.Response>> getById(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(eventService.getById(tenantId, id)));
    }

    @PostMapping("/{id}/rsvp")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<EventDto.Response>> rsvp(
            @PathVariable UUID id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        boolean attending = body == null || !body.containsKey("attending")
                || Boolean.TRUE.equals(body.get("attending"));
        return ResponseEntity.ok(ApiResponse.ok(eventService.rsvp(tenantId, id, attending, currentUser)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<Page<EventDto.Response>>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(eventService.list(tenantId, pageable)));
    }

    @GetMapping("/organizer/{organizerId}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<List<EventDto.Response>>> listByOrganizer(@PathVariable UUID organizerId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(eventService.listByOrganizer(tenantId, organizerId)));
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAuthority('SOCIAL:READ')")
    public ResponseEntity<ApiResponse<List<EventDto.Response>>> listByGroup(@PathVariable UUID groupId) {
        String tenantId = TenantContext.get();
        return ResponseEntity.ok(ApiResponse.ok(eventService.listByGroup(tenantId, groupId)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<EventDto.Response>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EventDto.UpdateRequest request) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        return ResponseEntity.ok(ApiResponse.ok(eventService.update(tenantId, id, request, currentUser)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SOCIAL:WRITE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        String tenantId = TenantContext.get();
        String currentUser = ((UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
        eventService.delete(tenantId, id, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
