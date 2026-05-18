package com.hrms.recruitment.jobboards;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes a JobRequisition to external job boards (LinkedIn Jobs, Indeed, Naukri).
 * Each provider has its own API contract; this class fans out and collects per-board
 * external posting IDs so we can manage / close them later.
 *
 * Credentials live in env per provider. When missing → logs the request (dev mode).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobBoardPublisher {

    @Value("${hrms.recruitment.linkedin.client-id:}") private String linkedinClientId;
    @Value("${hrms.recruitment.linkedin.client-secret:}") private String linkedinClientSecret;
    @Value("${hrms.recruitment.linkedin.organization-id:}") private String linkedinOrgId;

    @Value("${hrms.recruitment.indeed.publisher-id:}") private String indeedPublisherId;
    @Value("${hrms.recruitment.indeed.api-key:}") private String indeedApiKey;

    @Value("${hrms.recruitment.naukri.client-id:}") private String naukriClientId;
    @Value("${hrms.recruitment.naukri.api-key:}") private String naukriApiKey;

    private final RestTemplate rest = new RestTemplate();

    public Map<String, String> publishToAll(JobPosting posting) {
        Map<String, String> externalIds = new HashMap<>();
        externalIds.put("linkedin", publishLinkedIn(posting));
        externalIds.put("indeed",   publishIndeed(posting));
        externalIds.put("naukri",   publishNaukri(posting));
        return externalIds;
    }

    public String publishLinkedIn(JobPosting p) {
        if (linkedinClientId == null || linkedinClientId.isBlank() || linkedinClientId.startsWith("REPLACE")) {
            log.warn("LinkedIn not configured — logging posting: {}", p.title());
            return "dev-linkedin-" + UUID.randomUUID();
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(getLinkedinAccessToken());
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "companyApplyUrl", p.applyUrl(),
                    "description", p.description(),
                    "employmentStatus", "PROFESSIONALS." + mapEmploymentType(p.employmentType()),
                    "externalJobPostingId", p.requisitionId().toString(),
                    "listedAt", System.currentTimeMillis(),
                    "jobPostingOperationType", "CREATE",
                    "title", p.title(),
                    "location", p.location(),
                    "workplaceTypes", p.remote() ? "urn:li:workplaceType:2" : "urn:li:workplaceType:1",
                    "companyName", p.companyName());
            ResponseEntity<Map> resp = rest.exchange(
                    "https://api.linkedin.com/v2/simpleJobPostings",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Object id = resp.getBody() == null ? null : resp.getBody().get("id");
            return id == null ? null : id.toString();
        } catch (Exception e) {
            log.error("LinkedIn publish failed: {}", e.getMessage());
            return null;
        }
    }

    public String publishIndeed(JobPosting p) {
        if (indeedPublisherId == null || indeedPublisherId.isBlank() || indeedPublisherId.startsWith("REPLACE")) {
            log.warn("Indeed not configured — logging posting: {}", p.title());
            return "dev-indeed-" + UUID.randomUUID();
        }
        try {
            // Indeed accepts an XML feed at a publisher URL; the alternative is the Indeed Apply
            // API which uses POST. We use the API form here for simplicity.
            HttpHeaders h = new HttpHeaders();
            h.set("Authorization", "Bearer " + indeedApiKey);
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "publisherId", indeedPublisherId,
                    "jobTitle", p.title(),
                    "jobDescription", p.description(),
                    "jobLocation", p.location(),
                    "jobUrl", p.applyUrl(),
                    "salaryFrom", p.salaryMin() == null ? 0 : p.salaryMin(),
                    "salaryTo", p.salaryMax() == null ? 0 : p.salaryMax(),
                    "currency", p.currency() == null ? "INR" : p.currency(),
                    "externalRefNumber", p.requisitionId().toString());
            ResponseEntity<Map> resp = rest.exchange(
                    "https://api.indeed.com/v1/jobs",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Object id = resp.getBody() == null ? null : resp.getBody().get("id");
            return id == null ? null : id.toString();
        } catch (Exception e) {
            log.error("Indeed publish failed: {}", e.getMessage());
            return null;
        }
    }

    public String publishNaukri(JobPosting p) {
        if (naukriApiKey == null || naukriApiKey.isBlank() || naukriApiKey.startsWith("REPLACE")) {
            log.warn("Naukri not configured — logging posting: {}", p.title());
            return "dev-naukri-" + UUID.randomUUID();
        }
        try {
            HttpHeaders h = new HttpHeaders();
            h.set("X-Client-Id", naukriClientId);
            h.set("X-API-Key", naukriApiKey);
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = Map.of(
                    "jobTitle", p.title(),
                    "jobDescription", p.description(),
                    "jobLocation", p.location(),
                    "applyUrl", p.applyUrl(),
                    "minSalary", p.salaryMin() == null ? 0 : p.salaryMin(),
                    "maxSalary", p.salaryMax() == null ? 0 : p.salaryMax(),
                    "experience", p.minExperienceYears() == null ? 0 : p.minExperienceYears(),
                    "externalId", p.requisitionId().toString());
            ResponseEntity<Map> resp = rest.exchange(
                    "https://www.naukri.com/jobapi/v1/jobs",
                    HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
            Object id = resp.getBody() == null ? null : resp.getBody().get("jobId");
            return id == null ? null : id.toString();
        } catch (Exception e) {
            log.error("Naukri publish failed: {}", e.getMessage());
            return null;
        }
    }

    private String getLinkedinAccessToken() {
        // Production: 2-legged OAuth using clientId+clientSecret.
        return "stub-linkedin-token";
    }

    private String mapEmploymentType(String t) {
        if (t == null) return "FULL_TIME";
        return switch (t.toUpperCase()) {
            case "PART_TIME" -> "PART_TIME";
            case "CONTRACT" -> "CONTRACT";
            case "INTERN", "INTERNSHIP" -> "INTERNSHIP";
            case "TEMPORARY" -> "TEMPORARY";
            default -> "FULL_TIME";
        };
    }

    public record JobPosting(UUID requisitionId, String title, String description, String location,
                              String applyUrl, String employmentType, String companyName,
                              Boolean remote, Integer minExperienceYears,
                              Long salaryMin, Long salaryMax, String currency) {}
}
