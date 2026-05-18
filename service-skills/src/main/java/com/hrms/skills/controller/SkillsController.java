package com.hrms.skills.controller;

import com.hrms.skills.entity.*;
import com.hrms.skills.service.SkillsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillsController {

    private final SkillsService svc;

    @PostMapping public Skill create(@RequestBody Skill s) { return svc.createSkill(s); }
    @GetMapping public List<Skill> all() { return svc.all(); }

    @PostMapping("/employees") public EmployeeSkill addEmpSkill(@RequestBody EmployeeSkill es) { return svc.addEmployeeSkill(es); }
    @GetMapping("/employees/{employeeId}") public List<EmployeeSkill> ofEmployee(@PathVariable UUID employeeId) {
        return svc.ofEmployee(employeeId);
    }

    @PostMapping("/role-requirements") public RoleSkillRequirement addReq(@RequestBody RoleSkillRequirement r) {
        return svc.addRequirement(r);
    }

    @GetMapping("/gap") public Map<UUID, Integer> gap(@RequestParam UUID employeeId, @RequestParam UUID designationId) {
        return svc.gapAnalysis(employeeId, designationId);
    }
    @GetMapping("/experts") public List<EmployeeSkill> experts(@RequestParam UUID skillId, @RequestParam(defaultValue = "3") int minProficiency) {
        return svc.findExperts(skillId, minProficiency);
    }
}
