package com.hrms.corehr.customfields;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/custom-fields")
@RequiredArgsConstructor
public class CustomFieldController {

    public interface Repo extends JpaRepository<CustomFieldDefinition, UUID> {
        List<CustomFieldDefinition> findByTenantIdAndEntityNameAndActiveTrueOrderByDisplayOrderAsc(
                String tenantId, String entityName);
    }

    private final Repo repo;

    @PostMapping
    @Transactional
    public CustomFieldDefinition create(@RequestBody CustomFieldDefinition def) {
        def.setTenantId(TenantContext.get()); return repo.save(def);
    }

    @GetMapping("/by-entity/{entityName}")
    public List<CustomFieldDefinition> list(@PathVariable String entityName) {
        return repo.findByTenantIdAndEntityNameAndActiveTrueOrderByDisplayOrderAsc(TenantContext.get(), entityName);
    }

    @PutMapping("/{id}")
    @Transactional
    public CustomFieldDefinition update(@PathVariable UUID id, @RequestBody CustomFieldDefinition updates) {
        updates.setId(id); updates.setTenantId(TenantContext.get()); return repo.save(updates);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable UUID id) {
        CustomFieldDefinition d = repo.findById(id).orElseThrow();
        d.setActive(false); repo.save(d);
    }
}
