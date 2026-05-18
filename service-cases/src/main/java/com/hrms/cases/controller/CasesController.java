package com.hrms.cases.controller;

import com.hrms.cases.entity.CaseNote;
import com.hrms.cases.entity.HrCase;
import com.hrms.cases.service.CasesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@RequiredArgsConstructor
public class CasesController {

    private final CasesService svc;

    @PostMapping public HrCase file(@RequestBody HrCase c) { return svc.fileCase(c); }
    @GetMapping public Page<HrCase> list(@RequestParam(required = false) HrCase.Status status, Pageable p) {
        return svc.list(status, p);
    }
    @PostMapping("/{id}/status") public HrCase setStatus(@PathVariable UUID id,
                                                         @RequestParam HrCase.Status status,
                                                         @RequestParam(required = false) String resolution) {
        return svc.updateStatus(id, status, resolution);
    }
    @PostMapping("/{id}/notes") public CaseNote addNote(@PathVariable UUID id, @RequestBody CaseNote n) {
        n.setCaseId(id); return svc.addNote(n);
    }
    @GetMapping("/{id}/notes") public List<CaseNote> notes(@PathVariable UUID id) { return svc.listNotes(id); }
}
