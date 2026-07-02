package com.hrms.forms.controller;

import com.hrms.forms.entity.*;
import com.hrms.forms.service.FormsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
public class FormsController {

    private final FormsService svc;

    @GetMapping public java.util.List<FormDefinition> all() { return svc.listAll(); }
    @PostMapping public FormDefinition save(@RequestBody FormDefinition d) { return svc.createOrUpdate(d); }
    @PostMapping("/{id}/publish") public FormDefinition publish(@PathVariable UUID id) { return svc.publish(id); }
    @GetMapping("/by-code/{code}") public FormDefinition byCode(@PathVariable String code) { return svc.latestPublished(code); }

    @PostMapping("/{id}/submissions") public FormSubmission submit(@PathVariable UUID id, @RequestBody FormSubmission s) {
        s.setFormId(id); return svc.submit(s);
    }
    @GetMapping("/{id}/submissions") public Page<FormSubmission> list(@PathVariable UUID id, Pageable p) {
        return svc.submissions(id, p);
    }
}
