package com.hrms.auth.apikey;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService svc;

    @PostMapping
    public ApiKeyService.IssuedKey issue(@RequestBody IssueRequest req) {
        return svc.issue(req.name(), req.byUser(), req.scopes(), req.allowedIps(), req.ttlDays());
    }

    @GetMapping
    public List<ApiKey> list() { return svc.listActive(); }

    @PostMapping("/{id}/revoke")
    public ApiKey revoke(@PathVariable UUID id) { return svc.revoke(id); }

    @PostMapping("/{id}/rotate")
    public ApiKeyService.IssuedKey rotate(@PathVariable UUID id, @RequestParam UUID byUser) {
        return svc.rotate(id, byUser);
    }

    public record IssueRequest(String name, UUID byUser, Set<String> scopes,
                                Set<String> allowedIps, Integer ttlDays) {}
}
