package com.hrms.workflow.delegation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workflow/ooo")
@RequiredArgsConstructor
public class OutOfOfficeController {

    private final OutOfOfficeService svc;

    @PostMapping public OutOfOffice set(@RequestBody OutOfOffice o) { return svc.set(o); }
    @PostMapping("/{id}/cancel") public OutOfOffice cancel(@PathVariable UUID id) { return svc.cancel(id); }
    @GetMapping("/user/{userId}") public List<OutOfOffice> history(@PathVariable UUID userId) { return svc.history(userId); }
    @GetMapping("/user/{userId}/delegate")
    public Map<String, Object> delegate(@PathVariable UUID userId) {
        return Map.of("delegateUserId", svc.resolveDelegate(userId).map(Object::toString).orElse(""));
    }
}
