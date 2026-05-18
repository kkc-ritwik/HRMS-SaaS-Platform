package com.hrms.corehr.company;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/legal-entities")
@RequiredArgsConstructor
public class LegalEntityController {

    public interface Repo extends JpaRepository<LegalEntity, UUID> {
        List<LegalEntity> findByTenantIdAndActiveTrue(String tenantId);
        List<LegalEntity> findByTenantIdAndParentId(String tenantId, UUID parentId);
    }
    private final Repo repo;

    @PostMapping
    @Transactional
    public LegalEntity create(@RequestBody LegalEntity le) {
        le.setTenantId(TenantContext.get()); return repo.save(le);
    }

    @GetMapping
    public List<LegalEntity> list() { return repo.findByTenantIdAndActiveTrue(TenantContext.get()); }

    @GetMapping("/{id}")
    public LegalEntity get(@PathVariable UUID id) { return repo.findById(id).orElseThrow(); }

    @GetMapping("/{parentId}/children")
    public List<LegalEntity> children(@PathVariable UUID parentId) {
        return repo.findByTenantIdAndParentId(TenantContext.get(), parentId);
    }

    @PutMapping("/{id}")
    @Transactional
    public LegalEntity update(@PathVariable UUID id, @RequestBody LegalEntity updates) {
        LegalEntity le = repo.findById(id).orElseThrow();
        updates.setId(id); updates.setTenantId(le.getTenantId());
        return repo.save(updates);
    }
}
