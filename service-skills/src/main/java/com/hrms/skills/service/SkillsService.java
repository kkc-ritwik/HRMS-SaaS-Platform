package com.hrms.skills.service;

import com.hrms.security.model.TenantContext;
import com.hrms.skills.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SkillsService {

    public interface SkillRepo extends JpaRepository<Skill, UUID> {
        List<Skill> findByTenantIdAndIsActiveTrue(String tenantId);
        Optional<Skill> findByTenantIdAndCode(String tenantId, String code);
    }
    public interface EmpSkillRepo extends JpaRepository<EmployeeSkill, UUID> {
        List<EmployeeSkill> findByTenantIdAndEmployeeId(String tenantId, UUID employeeId);
        List<EmployeeSkill> findByTenantIdAndSkillId(String tenantId, UUID skillId);
    }
    public interface RoleReqRepo extends JpaRepository<RoleSkillRequirement, UUID> {
        List<RoleSkillRequirement> findByTenantIdAndDesignationId(String tenantId, UUID designationId);
    }

    private final SkillRepo skills;
    private final EmpSkillRepo empSkills;
    private final RoleReqRepo roleReqs;

    @Transactional public Skill createSkill(Skill s) { s.setTenantId(TenantContext.get()); s.setIsActive(true); return skills.save(s); }
    public List<Skill> all() { return skills.findByTenantIdAndIsActiveTrue(TenantContext.get()); }

    @Transactional public EmployeeSkill addEmployeeSkill(EmployeeSkill es) {
        es.setTenantId(TenantContext.get()); return empSkills.save(es);
    }
    public List<EmployeeSkill> ofEmployee(UUID employeeId) {
        return empSkills.findByTenantIdAndEmployeeId(TenantContext.get(), employeeId);
    }

    @Transactional public RoleSkillRequirement addRequirement(RoleSkillRequirement r) {
        r.setTenantId(TenantContext.get()); return roleReqs.save(r);
    }

    /** Returns gap as { skillId -> requiredProficiency - currentProficiency } where >0 means deficit. */
    public Map<UUID, Integer> gapAnalysis(UUID employeeId, UUID designationId) {
        Map<UUID, Integer> have = new HashMap<>();
        for (EmployeeSkill es : ofEmployee(employeeId)) have.put(es.getSkillId(), es.getProficiency());
        Map<UUID, Integer> gaps = new HashMap<>();
        for (RoleSkillRequirement r : roleReqs.findByTenantIdAndDesignationId(TenantContext.get(), designationId)) {
            int current = have.getOrDefault(r.getSkillId(), 0);
            int gap = r.getMinProficiency() - current;
            if (gap > 0) gaps.put(r.getSkillId(), gap);
        }
        return gaps;
    }

    /** Find employees who match a skill at the requested proficiency or higher (talent search). */
    public List<EmployeeSkill> findExperts(UUID skillId, int minProficiency) {
        return empSkills.findByTenantIdAndSkillId(TenantContext.get(), skillId).stream()
                .filter(e -> e.getProficiency() != null && e.getProficiency() >= minProficiency).toList();
    }
}
