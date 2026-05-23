package com.hrms.document.letters;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/letters/employment")
@RequiredArgsConstructor
public class EmploymentLetterController {

    private final EmploymentLetterGenerator gen;

    @PostMapping("/{type}")
    public Map<String, String> generate(@PathVariable EmploymentLetterGenerator.LetterType type,
                                         @RequestBody EmploymentLetterGenerator.LetterRequest body) {
        EmploymentLetterGenerator.LetterRequest req = new EmploymentLetterGenerator.LetterRequest(
                TenantContext.get(), body.employeeId(), type,
                body.employeeName(), body.employeeCode(), body.companyName(),
                body.currentDesignation(), body.newDesignation(),
                body.currentDepartment(), body.newDepartment(),
                body.currentLocation(), body.newLocation(),
                body.currentSalary(), body.newSalary(),
                body.effectiveDate(), body.joinDate(), body.probationEndDate(),
                body.signatoryName(), body.signatoryTitle(), body.reason());
        return Map.of("storageUri", gen.generate(req), "type", type.name());
    }
}
