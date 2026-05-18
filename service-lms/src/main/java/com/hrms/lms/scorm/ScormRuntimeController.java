package com.hrms.lms.scorm;

import com.hrms.security.model.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * SCORM RTE shim. Front-end loads the SCORM player which exposes the standard SCORM
 * runtime JavaScript API; on every LMSCommit call the player POSTs the cmi data here.
 *
 * Supported endpoints (SCORM 1.2 RTE):
 *   POST /initialize       → start/resume attempt, return cmi snapshot
 *   POST /commit           → persist current cmi data
 *   POST /finish           → finalize, mark completed
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/lms/scorm/runtime")
@RequiredArgsConstructor
public class ScormRuntimeController {

    public interface AttemptRepo extends JpaRepository<ScormAttempt, UUID> {
        Optional<ScormAttempt> findFirstByTenantIdAndPackageIdAndEmployeeIdOrderByAttemptNumberDesc(
                String tenantId, UUID packageId, UUID employeeId);
    }

    private final AttemptRepo attempts;

    @PostMapping("/initialize")
    @Transactional
    public ScormAttempt initialize(@RequestParam UUID packageId, @RequestParam UUID employeeId) {
        Optional<ScormAttempt> last = attempts.findFirstByTenantIdAndPackageIdAndEmployeeIdOrderByAttemptNumberDesc(
                TenantContext.get(), packageId, employeeId);
        if (last.isPresent() && !"completed".equalsIgnoreCase(last.get().getLessonStatus())) {
            return last.get();        // resume existing in-flight attempt
        }
        ScormAttempt a = new ScormAttempt();
        a.setTenantId(TenantContext.get());
        a.setPackageId(packageId);
        a.setEmployeeId(employeeId);
        a.setAttemptNumber(last.map(x -> x.getAttemptNumber() + 1).orElse(1));
        a.setLessonStatus("not_attempted");
        a.setStartedAt(Instant.now());
        a.setCmiData(new HashMap<>());
        return attempts.save(a);
    }

    @PostMapping("/commit")
    @Transactional
    public ScormAttempt commit(@RequestParam UUID attemptId, @RequestBody Map<String, Object> cmiData) {
        ScormAttempt a = attempts.findById(attemptId).orElseThrow();
        Map<String, Object> merged = a.getCmiData() == null ? new HashMap<>() : new HashMap<>(a.getCmiData());
        merged.putAll(cmiData);
        a.setCmiData(merged);

        Object status = cmiData.get("cmi.core.lesson_status");
        if (status == null) status = cmiData.get("cmi.completion_status");
        if (status != null) a.setLessonStatus(status.toString());

        Object scoreRaw = cmiData.get("cmi.core.score.raw");
        if (scoreRaw == null) scoreRaw = cmiData.get("cmi.score.raw");
        if (scoreRaw != null) try { a.setScoreRaw(new BigDecimal(scoreRaw.toString())); }
        catch (NumberFormatException ignored) {}

        Object sessionTime = cmiData.get("cmi.core.session_time");
        if (sessionTime == null) sessionTime = cmiData.get("cmi.session_time");
        if (sessionTime != null) a.setSessionTimeSeconds(parseScormDuration(sessionTime.toString()));

        a.setLastCommittedAt(Instant.now());
        return attempts.save(a);
    }

    @PostMapping("/finish")
    @Transactional
    public ScormAttempt finish(@RequestParam UUID attemptId) {
        ScormAttempt a = attempts.findById(attemptId).orElseThrow();
        a.setLastCommittedAt(Instant.now());
        if ("incomplete".equals(a.getLessonStatus()) || "not_attempted".equals(a.getLessonStatus())) {
            a.setLessonStatus("completed");
        }
        a.setCompletedAt(Instant.now());
        log.info("SCORM attempt {} finished: status={}", attemptId, a.getLessonStatus());
        return attempts.save(a);
    }

    /** "HHHH:MM:SS.SS" or PT-style. Returns seconds. */
    private long parseScormDuration(String s) {
        try {
            if (s.startsWith("PT")) {
                java.time.Duration d = java.time.Duration.parse(s);
                return d.toSeconds();
            }
            String[] parts = s.split(":");
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            double sec = Double.parseDouble(parts[2]);
            return (long) (h * 3600L + m * 60L + sec);
        } catch (Exception e) { return 0; }
    }
}
