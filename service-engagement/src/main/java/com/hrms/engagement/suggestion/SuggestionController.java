package com.hrms.engagement.suggestion;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * Anonymous Suggestion Box — Zoho People's "Employee Voice" parity.
 *
 *  POST   /api/engagement/suggestions                  — submit (anonymous unless includeIdentity=true)
 *  GET    /api/engagement/suggestions                  — list (paginated, filtered)
 *  POST   /api/engagement/suggestions/{id}/vote        — up/down
 *  POST   /api/engagement/suggestions/{id}/resolve     — HR resolution
 *  POST   /api/engagement/suggestions/{id}/de-anonymise  — privileged
 */
@RestController
@RequestMapping("/api/engagement/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    public interface Repo extends JpaRepository<Suggestion, UUID> {
        @Query("SELECT s FROM Suggestion s WHERE s.tenantId = :t " +
                "AND (:cat IS NULL OR s.category = :cat) " +
                "AND (:st IS NULL OR s.status = :st)")
        Page<Suggestion> search(@Param("t") String tenant,
                                @Param("cat") Suggestion.Category category,
                                @Param("st") Suggestion.Status status,
                                org.springframework.data.domain.Pageable pageable);

        @Modifying
        @Query("UPDATE Suggestion s SET s.votesUp = s.votesUp + :uDelta, s.votesDown = s.votesDown + :dDelta WHERE s.id = :id")
        void bumpVotes(@Param("id") UUID id, @Param("uDelta") int uDelta, @Param("dDelta") int dDelta);
    }

    public interface VoteRepo extends JpaRepository<SuggestionVote, UUID> {
        Optional<SuggestionVote> findBySuggestionIdAndVoterEmployeeId(UUID s, UUID e);
    }

    private final Repo repo;
    private final VoteRepo voteRepo;

    @PostMapping
    @Transactional
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) {
        String tenant = TenantContext.get();
        Suggestion s = new Suggestion();
        s.setTenantId(tenant);
        s.setTitle((String) body.get("title"));
        s.setDescription((String) body.get("description"));
        s.setCategory(Suggestion.Category.valueOf(
                (String) body.getOrDefault("category", "IDEA")));
        if (Boolean.TRUE.equals(body.get("includeIdentity")) && body.get("employeeId") != null) {
            s.setSubmittedByEmployeeIdEncrypted((String) body.get("employeeId"));
        }
        repo.save(s);
        return Map.of("id", s.getId(), "status", s.getStatus().name());
    }

    @GetMapping
    public Page<Suggestion> list(@RequestParam(required = false) Suggestion.Category category,
                                 @RequestParam(required = false) Suggestion.Status status,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size) {
        return repo.search(TenantContext.get(), category, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @PostMapping("/{id}/vote")
    @Transactional
    public Map<String, Object> vote(@PathVariable UUID id,
                                    @RequestBody Map<String, Object> body) {
        UUID voter = UUID.fromString((String) body.get("voterEmployeeId"));
        String dir = ((String) body.get("direction")).toUpperCase(Locale.ROOT);
        if (!dir.equals("UP") && !dir.equals("DOWN")) {
            throw new IllegalArgumentException("direction must be UP or DOWN");
        }

        Optional<SuggestionVote> prior = voteRepo.findBySuggestionIdAndVoterEmployeeId(id, voter);
        if (prior.isPresent()) {
            SuggestionVote v = prior.get();
            if (v.getDirection().equals(dir)) {
                return Map.of("status", "ALREADY_VOTED", "direction", dir);
            }
            // Flip — undo old, apply new
            repo.bumpVotes(id, dir.equals("UP") ? 1 : -1, dir.equals("DOWN") ? 1 : -1);
            v.setDirection(dir);
            voteRepo.save(v);
        } else {
            SuggestionVote v = new SuggestionVote();
            v.setTenantId(TenantContext.get());
            v.setSuggestionId(id);
            v.setVoterEmployeeId(voter);
            v.setDirection(dir);
            voteRepo.save(v);
            repo.bumpVotes(id, dir.equals("UP") ? 1 : 0, dir.equals("DOWN") ? 1 : 0);
        }
        return Map.of("status", "RECORDED");
    }

    @PostMapping("/{id}/resolve")
    @Transactional
    public Suggestion resolve(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        Suggestion s = repo.findById(id).orElseThrow();
        s.setStatus(Suggestion.Status.valueOf((String) body.getOrDefault("status", "IMPLEMENTED")));
        s.setResolution((String) body.get("resolution"));
        s.setResolvedAt(OffsetDateTime.now());
        if (body.get("assignedReviewerId") != null) {
            s.setAssignedReviewerId(UUID.fromString((String) body.get("assignedReviewerId")));
        }
        return repo.save(s);
    }

    /** Returns the original submitter id — caller must have HR_ADMIN role (enforced by Spring Security upstream). */
    @PostMapping("/{id}/de-anonymise")
    public Map<String, Object> deAnonymise(@PathVariable UUID id) {
        Suggestion s = repo.findById(id).orElseThrow();
        return Map.of(
                "id", id,
                "submittedByEmployeeId", s.getSubmittedByEmployeeIdEncrypted(),
                "anonymous", s.getSubmittedByEmployeeIdEncrypted() == null
        );
    }
}
