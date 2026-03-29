# HRMS Platform — Known Issues & Fixes

Scanned: All pom.xml, application.yml, Java source files, SQL migrations, docker-compose.yml.

---

## SEVERITY LEGEND
- 🔴 **CRITICAL** — Will prevent startup or cause runtime errors
- 🟡 **WARNING** — Will not break basic functionality but should be fixed before production
- 🟢 **INFO** — Non-blocking, best-practice improvements

---

## ISSUE 1 🟡 — OtpService Does Not Send Emails (Stub Implementation)

**File:** `service-auth/src/main/java/com/hrms/auth/service/OtpService.java`
**Line:** ~55

**What's wrong:**
```java
public void sendOtpEmail(String email, String otp, OtpDto.OtpPurpose purpose) {
    // TODO: replace with actual JavaMailSender call once SMTP is configured
    log.info("OTP for {} (purpose={}): {}", email, purpose, otp);
}
```
`sendOtpEmail` only logs the OTP to console. No actual email is sent.

**Impact:** OTP-based flows (password reset, email verification) will not send emails.

**Fix:** Wire up `JavaMailSender`. Add to `OtpService`:
```java
private final JavaMailSender mailSender;

public void sendOtpEmail(String email, String otp, OtpDto.OtpPurpose purpose) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(email);
    msg.setSubject("Your HRMS OTP");
    msg.setText("Your OTP for " + purpose + " is: " + otp + " (valid 5 minutes)");
    mailSender.send(msg);
    log.info("OTP email sent to {}", email);
}
```
Then set `MAIL_USERNAME` and `MAIL_PASSWORD` env vars (see SETUP_GUIDE.md §2.3).

**Workaround for local dev:** OTP is logged at INFO level. Check `logs/service-auth.log` or console for the line: `OTP for <email> (purpose=LOGIN): 123456`

---

## ISSUE 2 🟡 — 13 Services Have Hardcoded DB Credentials (No Env Var Substitution)

**Files affected (13 services):**
- `service-recruitment/src/main/resources/application.yml`
- `service-performance/src/main/resources/application.yml`
- `service-onboarding/src/main/resources/application.yml`
- `service-document/src/main/resources/application.yml`
- `service-notification/src/main/resources/application.yml`
- `service-expense/src/main/resources/application.yml`
- `service-asset/src/main/resources/application.yml`
- `service-helpdesk/src/main/resources/application.yml`
- `service-social/src/main/resources/application.yml`
- `service-compensation/src/main/resources/application.yml`
- `service-compliance/src/main/resources/application.yml`
- `service-offboarding/src/main/resources/application.yml`
- `service-lms/src/main/resources/application.yml`
- `service-workflow/src/main/resources/application.yml`
- `service-reports/src/main/resources/application.yml`

**What's wrong:**
```yaml
datasource:
  username: hrms_user    # hardcoded
  password: hrms_pass    # hardcoded
```
Unlike service-auth and service-core-hr which use `${DB_USERNAME:hrms_user}`, these services hardcode credentials.

**Impact:** Fine for local dev. Cannot override via environment variables in production/Docker deployment.

**Fix:** Change each affected `application.yml`:
```yaml
datasource:
  username: ${DB_USERNAME:hrms_user}
  password: ${DB_PASSWORD:hrms_pass}
jwt:
  secret: ${JWT_SECRET:hrms-platform-jwt-secret-key-change-this-in-production-256bit!!}
```

---

## ISSUE 3 🟡 — JWT Secret Hardcoded in 15 Services

**Files affected:** Same 15 services listed in Issue 2.

**What's wrong:**
```yaml
jwt:
  secret: hrms-platform-jwt-secret-key-change-this-in-production-256bit!!
```
Hardcoded — cannot be overridden. For production, this must be a secret stored in environment variables or a vault.

**Impact:** None for local dev. Security risk for production deployment.

**Fix:**
```yaml
jwt:
  secret: ${JWT_SECRET:hrms-platform-jwt-secret-key-change-this-in-production-256bit!!}
```

---

## ISSUE 4 🟡 — DepartmentMapper Warning: `active` Field (Now Fixed)

**File:** `service-core-hr/src/main/java/com/hrms/corehr/mapper/DepartmentMapper.java`
**Line:** 18

**Status:** ✅ FIXED — Added `@Mapping(target = "active", constant = "true")` to `toEntity`.

---

## ISSUE 5 🟡 — Onboarding Entities: `@Builder` Ignoring Field Initializers (Now Fixed)

**Files:** 5 onboarding entity files (OnboardingTemplate, BuddyAssignment, OnboardingDocument, ProbationReview, OnboardingTask)

**Status:** ✅ FIXED — Added `@Builder.Default` to all fields with initializers.

---

## ISSUE 6 🟢 — `service-recruitment` Missing `Hikari` Pool Config

**File:** `service-recruitment/src/main/resources/application.yml`

**What's wrong:**
```yaml
datasource:
  url: jdbc:postgresql://localhost:5432/hrms_db
  username: hrms_user
  password: hrms_pass
  # No HikariCP settings
```
Other services (auth, core-hr, leave, payroll) have explicit `hikari.maximum-pool-size`, `minimum-idle`, `connection-timeout`. Recruitment uses defaults.

**Impact:** Under load, connection pool may exhaust. Non-critical for local dev.

**Fix (optional):** Add to application.yml:
```yaml
datasource:
  hikari:
    maximum-pool-size: 10
    minimum-idle: 2
    connection-timeout: 30000
```

---

## ISSUE 7 🟢 — API Gateway: `applications` Route Not Covered

**File:** `api-gateway/src/main/resources/application.yml`

**What's wrong:**
The `service-recruitment` module has an `ApplicationController` at `/api/v1/applications/**` but the gateway's recruitment route only covers:
```yaml
- Path=/api/v1/jobs/**, /api/v1/candidates/**, /api/v1/interviews/**, /api/v1/offers/**
```
`/api/v1/applications/**` is not routed.

**Fix:** Update api-gateway application.yml:
```yaml
- id: recruitment
  uri: lb://service-recruitment
  predicates:
    - Path=/api/v1/jobs/**, /api/v1/candidates/**, /api/v1/applications/**, /api/v1/interviews/**, /api/v1/offers/**
```

---

## ISSUE 8 🟢 — API Gateway: Performance Routes Incomplete

**File:** `api-gateway/src/main/resources/application.yml`

**What's wrong:**
Performance service route covers:
```yaml
- Path=/api/v1/goals/**, /api/v1/reviews/**, /api/v1/feedback/**
```
But `service-performance` also has controllers at:
- `/api/v1/performance/cycles/**` (ReviewCycleController)
- `/api/v1/performance/pip/**` (PipPlanController)
- `/api/v1/performance/one-on-ones/**` (OneOnOneController)
- `/api/v1/competencies/**` (CompetencyController)
- `/api/v1/performance/analytics/**`

**Fix:** Update api-gateway application.yml:
```yaml
- id: performance
  uri: lb://service-performance
  predicates:
    - Path=/api/v1/goals/**, /api/v1/reviews/**, /api/v1/feedback/**, /api/v1/performance/**, /api/v1/competencies/**
```

---

## ISSUE 9 🟢 — Null Safety Warnings in EmployeeService.java

**File:** `service-core-hr/src/main/java/com/hrms/corehr/service/EmployeeService.java`
**Lines:** 237, 241, 245, 249, 262, 266, 270, 282, 286, 290, 299, 304

**What's wrong:**
IDE reports "Null type safety: expression of type UUID needs unchecked conversion to `@NonNull UUID`" for calls like:
```java
if (emp.getDepartmentId() != null) {
    departmentRepository.findById(emp.getDepartmentId())  // IDE warns here
```

**Impact:** None — null guard `if (...!= null)` is already present. Maven compilation succeeds. IDE warning only.

**Fix (if you want to silence the warnings):**
```java
UUID deptId = emp.getDepartmentId();
if (deptId != null) {
    departmentRepository.findById(deptId)
```
Or add `@SuppressWarnings("null")` to the enclosing method.

---

## ISSUE 10 🟢 — Config Server Not Used by Any Service

**File:** `config-server/src/main/resources/application.yml`

**What's wrong:**
The config server is running on port 8888, but no microservice has `spring.config.import=configserver:http://localhost:8888` in its `application.yml`. All services use local `application.yml` directly.

**Impact:** Config server is running but unused. Services don't pull config from it.

**Status:** Non-blocking for current architecture. Config server is available for future use.

---

## ISSUE 11 🟢 — Elasticsearch Not Referenced in Any Service

**docker-compose.yml** starts Elasticsearch on port 9200, but no `application.yml` references `spring.elasticsearch.*`. No service uses Elasticsearch currently.

**Impact:** Elasticsearch runs but nothing connects to it. Minor resource usage (~512MB RAM).

**Fix:** Either remove from docker-compose.yml if not needed, or add to LMS/Reports services when search is implemented.

---

## SUMMARY TABLE

| # | Severity | Issue | Status |
|---|----------|-------|--------|
| 1 | 🟡 Warning | OtpService doesn't send emails (logs only) | Needs SMTP config for prod |
| 2 | 🟡 Warning | 15 services have hardcoded DB credentials | OK for local dev |
| 3 | 🟡 Warning | JWT secret hardcoded in 15 services | OK for local dev |
| 4 | 🟡 Warning | DepartmentMapper missing `active` mapping | ✅ Fixed |
| 5 | 🟡 Warning | Onboarding @Builder missing @Builder.Default | ✅ Fixed |
| 6 | 🟢 Info | service-recruitment missing HikariCP config | Non-critical |
| 7 | 🟢 Info | Gateway missing `/api/v1/applications/**` route | Fix recommended |
| 8 | 🟢 Info | Gateway performance routes incomplete | Fix recommended |
| 9 | 🟢 Info | Null safety warnings in EmployeeService | IDE only, not a real error |
| 10 | 🟢 Info | Config server unused by services | Future use |
| 11 | 🟢 Info | Elasticsearch running but unused | Remove or wire up later |

**No CRITICAL issues found. The platform will start and run correctly for local development.**
