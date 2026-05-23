package com.hrms.engagement.stay;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/engagement/stay")
@RequiredArgsConstructor
public class StayInterviewController {

    public interface Repo extends JpaRepository<StayInterview, UUID> {
        @Query("SELECT s FROM StayInterview s WHERE s.tenantId = :t AND s.employeeId = :e ORDER BY s.scheduledDate DESC")
        List<StayInterview> forEmployee(@Param("t") String tenant, @Param("e") UUID emp);

        @Query("SELECT s FROM StayInterview s WHERE s.tenantId = :t AND s.status = 'SCHEDULED' AND s.scheduledDate <= :upto")
        List<StayInterview> dueBy(@Param("t") String tenant, @Param("upto") LocalDate upto);
    }

    public interface QuestionRepo extends JpaRepository<StayQuestion, UUID> {
        @Query("SELECT q FROM StayQuestion q WHERE q.tenantId = :t AND q.active = true ORDER BY q.displayOrder")
        List<StayQuestion> active(@Param("t") String tenant);
    }

    private final Repo repo;
    private final QuestionRepo questions;

    @PostMapping
    @Transactional
    public StayInterview schedule(@RequestBody Map<String, Object> body) {
        StayInterview s = new StayInterview();
        s.setTenantId(TenantContext.get());
        s.setEmployeeId(UUID.fromString((String) body.get("employeeId")));
        s.setInterviewerId(UUID.fromString((String) body.get("interviewerId")));
        s.setTrigger(StayInterview.Trigger.valueOf(
                (String) body.getOrDefault("trigger", "AD_HOC")));
        s.setScheduledDate(LocalDate.parse((String) body.get("scheduledDate")));
        return repo.save(s);
    }

    @PostMapping("/{id}/complete")
    @Transactional
    @SuppressWarnings("unchecked")
    public StayInterview complete(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        StayInterview s = repo.findById(id).orElseThrow();
        s.setStatus(StayInterview.Status.COMPLETED);
        s.setConductedAt(OffsetDateTime.now());
        s.setEngagementScore(body.get("engagementScore") == null ? null
                : ((Number) body.get("engagementScore")).intValue());
        if (body.get("flightRisk") != null) {
            s.setFlightRisk(StayInterview.FlightRisk.valueOf((String) body.get("flightRisk")));
        }
        s.setNotes((String) body.get("notes"));
        s.setResponses((Map<String, Object>) body.get("responses"));
        s.setActionItems((List<Map<String, Object>>) body.get("actionItems"));
        if (body.get("nextReviewDate") != null) {
            s.setNextReviewDate(LocalDate.parse((String) body.get("nextReviewDate")));
        }
        return repo.save(s);
    }

    @GetMapping("/employee/{employeeId}")
    public List<StayInterview> ofEmployee(@PathVariable UUID employeeId) {
        return repo.forEmployee(TenantContext.get(), employeeId);
    }

    @GetMapping("/due")
    public List<StayInterview> due(@RequestParam(required = false) LocalDate upto) {
        return repo.dueBy(TenantContext.get(),
                upto == null ? LocalDate.now().plusDays(14) : upto);
    }

    @GetMapping("/questions")
    public List<StayQuestion> questions() {
        return questions.active(TenantContext.get());
    }

    @PostMapping("/questions")
    public StayQuestion addQuestion(@RequestBody StayQuestion q) {
        q.setTenantId(TenantContext.get());
        if (q.getActive() == null) q.setActive(true);
        return questions.save(q);
    }
}
