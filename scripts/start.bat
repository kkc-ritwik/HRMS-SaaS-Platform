@echo off
setlocal EnableDelayedExpansion

echo ============================================================
echo  HRMS Platform - Full Service Startup (32 services)
echo ============================================================
echo.

set ROOT=%~dp0..
set JAVA_OPTS=-Xms256m -Xmx512m
set MVN=%USERPROFILE%\apache-maven-3.9.6\bin\mvn.cmd

:: Allow shared common-* auto-config beans (i18n localeResolver, etc.) to coexist
:: with Spring Boot's defaults. Inherited by all child service windows.
set SPRING_MAIN_ALLOW_BEAN_DEFINITION_OVERRIDING=true

:: Dev convenience: let Hibernate reconcile entity/table gaps for common-module
:: platform tables that lack standalone Flyway migrations. Flyway still runs first.
set SPRING_JPA_HIBERNATE_DDL_AUTO=update

:: Point common-mail at the local MailHog SMTP (docker-compose) so JavaMailSender wires up.
set SPRING_MAIL_HOST=localhost
set SPRING_MAIL_PORT=1025

:: MailHog's SMTP health probe is flaky and would otherwise mark the whole service
:: actuator health DOWN even though the service is fully functional. Disable that probe.
set MANAGEMENT_HEALTH_MAIL_ENABLED=false

:: Unified JWT signing secret. The newer services don't set jwt.secret in their yml and would
:: otherwise fall back to a different built-in default, rejecting auth-issued tokens (401).
:: Setting it here makes every service validate the same key.
set JWT_SECRET=hrms-platform-jwt-secret-key-change-this-in-production-256bit!!

:: Shared common-* migrations apply at high versions (V100+), so a service's own new
:: lower-numbered migration is "out of order". Allow it so Flyway still applies it.
set SPRING_FLYWAY_OUT_OF_ORDER=true

:: Ensure logs directory exists
if not exist "%ROOT%\logs" mkdir "%ROOT%\logs"

:: ── Tier 1: Service Discovery (must be first) ─────────────────────────────
echo [1/32] Starting Eureka Service Discovery (port 8761)...
start "service-discovery" /min cmd /c "cd /d "%ROOT%\service-discovery" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-discovery.log" 2>&1"
echo       Waiting 30s for Eureka...
timeout /t 30 /nobreak >nul
curl -s http://localhost:8761/actuator/health >nul 2>&1
if %errorlevel% equ 0 (echo       [OK] Eureka is up.) else (echo       [WARN] Eureka may still be starting.)
echo.

:: ── Tier 2: Config Server ─────────────────────────────────────────────────
echo [2/32] Starting Config Server (port 8888)...
start "config-server" /min cmd /c "cd /d "%ROOT%\config-server" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\config-server.log" 2>&1"
echo       Waiting 20s for Config Server...
timeout /t 20 /nobreak >nul
echo.

:: ── Tier 3: Auth Service (other services may depend on it) ────────────────
echo [3/32] Starting Auth Service (port 8081)...
start "service-auth" /min cmd /c "cd /d "%ROOT%\service-auth" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-auth.log" 2>&1"
echo       Waiting 25s for Auth Service...
timeout /t 25 /nobreak >nul
echo.

:: ── Tier 4: Core business services (batch 1) ──────────────────────────────
echo [4/32]  Starting Core HR Service (port 8082)...
start "service-core-hr" /min cmd /c "cd /d "%ROOT%\service-core-hr" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-core-hr.log" 2>&1"

echo [5/32]  Starting Leave and Attendance Service (port 8083)...
start "service-leave-attendance" /min cmd /c "cd /d "%ROOT%\service-leave-attendance" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-leave-attendance.log" 2>&1"

echo [6/32]  Starting Payroll Service (port 8084)...
start "service-payroll" /min cmd /c "cd /d "%ROOT%\service-payroll" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-payroll.log" 2>&1"

echo [7/32]  Starting Recruitment Service (port 8085)...
start "service-recruitment" /min cmd /c "cd /d "%ROOT%\service-recruitment" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-recruitment.log" 2>&1"

echo [8/32]  Starting Performance Service (port 8086)...
start "service-performance" /min cmd /c "cd /d "%ROOT%\service-performance" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-performance.log" 2>&1"

echo [9/32]  Starting Onboarding Service (port 8112)...
start "service-onboarding" /min cmd /c "cd /d "%ROOT%\service-onboarding" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-onboarding.log" 2>&1"

echo [10/32] Starting Document Service (port 8091)...
start "service-document" /min cmd /c "cd /d "%ROOT%\service-document" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-document.log" 2>&1"

echo [11/32] Starting Notification Service (port 8092)...
start "service-notification" /min cmd /c "cd /d "%ROOT%\service-notification" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-notification.log" 2>&1"

echo [12/32] Starting Expense Service (port 8093)...
start "service-expense" /min cmd /c "cd /d "%ROOT%\service-expense" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-expense.log" 2>&1"

echo [13/32] Starting Asset Service (port 8094)...
start "service-asset" /min cmd /c "cd /d "%ROOT%\service-asset" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-asset.log" 2>&1"

echo       Waiting 25s for batch 1...
timeout /t 25 /nobreak >nul
echo.

:: ── Tier 5: Core business services (batch 2) ──────────────────────────────
echo [14/32] Starting Helpdesk Service (port 8095)...
start "service-helpdesk" /min cmd /c "cd /d "%ROOT%\service-helpdesk" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-helpdesk.log" 2>&1"

echo [15/32] Starting Social Service (port 8096)...
start "service-social" /min cmd /c "cd /d "%ROOT%\service-social" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-social.log" 2>&1"

echo [16/32] Starting Compensation Service (port 8097)...
start "service-compensation" /min cmd /c "cd /d "%ROOT%\service-compensation" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-compensation.log" 2>&1"

echo [17/32] Starting Compliance Service (port 8098)...
start "service-compliance" /min cmd /c "cd /d "%ROOT%\service-compliance" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-compliance.log" 2>&1"

echo [18/32] Starting Offboarding Service (port 8099)...
start "service-offboarding" /min cmd /c "cd /d "%ROOT%\service-offboarding" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-offboarding.log" 2>&1"

echo [19/32] Starting LMS Service (port 8100)...
start "service-lms" /min cmd /c "cd /d "%ROOT%\service-lms" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-lms.log" 2>&1"

echo [20/32] Starting Workflow Service (port 8101)...
start "service-workflow" /min cmd /c "cd /d "%ROOT%\service-workflow" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-workflow.log" 2>&1"

echo [21/32] Starting Reports Service (port 8102)...
start "service-reports" /min cmd /c "cd /d "%ROOT%\service-reports" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-reports.log" 2>&1"

echo       Waiting 25s for batch 2...
timeout /t 25 /nobreak >nul
echo.

:: ── Tier 6: New services ──────────────────────────────────────────────────
echo [22/32] Starting Travel Service (port 8109)...
start "service-travel" /min cmd /c "cd /d "%ROOT%\service-travel" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-travel.log" 2>&1"

echo [23/32] Starting Timesheet Service (port 8110)...
start "service-timesheet" /min cmd /c "cd /d "%ROOT%\service-timesheet" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-timesheet.log" 2>&1"

echo [24/32] Starting Cases Service (port 8111)...
start "service-cases" /min cmd /c "cd /d "%ROOT%\service-cases" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-cases.log" 2>&1"

echo [25/32] Starting Files Service (port 8103)...
start "service-files" /min cmd /c "cd /d "%ROOT%\service-files" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-files.log" 2>&1"

echo [26/32] Starting Forms Service (port 8104)...
start "service-forms" /min cmd /c "cd /d "%ROOT%\service-forms" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-forms.log" 2>&1"

echo [27/32] Starting Engagement Service (port 8105)...
start "service-engagement" /min cmd /c "cd /d "%ROOT%\service-engagement" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-engagement.log" 2>&1"

echo [28/32] Starting Skills Service (port 8106)...
start "service-skills" /min cmd /c "cd /d "%ROOT%\service-skills" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-skills.log" 2>&1"

echo [29/32] Starting Integrations Service (port 8107)...
start "service-integrations" /min cmd /c "cd /d "%ROOT%\service-integrations" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-integrations.log" 2>&1"

echo [30/32] Starting Workplace Service (port 8108)...
start "service-workplace" /min cmd /c "cd /d "%ROOT%\service-workplace" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\service-workplace.log" 2>&1"

echo       Waiting 25s for batch 3...
timeout /t 25 /nobreak >nul
echo.

:: ── Tier 7: API Gateway (last, routes to all services) ───────────────────
echo [31/32] Starting API Gateway (port 8080)...
start "api-gateway" /min cmd /c "cd /d "%ROOT%\api-gateway" && "%MVN%" spring-boot:run -DskipTests > "%ROOT%\logs\api-gateway.log" 2>&1"
echo       Waiting 20s for API Gateway...
timeout /t 20 /nobreak >nul
echo.

echo ============================================================
echo  STARTUP COMPLETE - 32 services launched
echo ============================================================
echo  Eureka Dashboard   : http://localhost:8761
echo  API Gateway        : http://localhost:8080
echo  Config Server      : http://localhost:8888
echo.
echo  Auth               : http://localhost:8081/swagger-ui.html
echo  Core HR            : http://localhost:8082/swagger-ui.html
echo  Leave/Attendance   : http://localhost:8083/swagger-ui.html
echo  Payroll            : http://localhost:8084/swagger-ui.html
echo  Recruitment        : http://localhost:8085/swagger-ui.html
echo  Performance        : http://localhost:8086/swagger-ui.html
echo  Onboarding         : http://localhost:8112/swagger-ui.html
echo  Document           : http://localhost:8091/swagger-ui.html
echo  Notification       : http://localhost:8092/swagger-ui.html
echo  Expense            : http://localhost:8093/swagger-ui.html
echo  Asset              : http://localhost:8094/swagger-ui.html
echo  Helpdesk           : http://localhost:8095/swagger-ui.html
echo  Social             : http://localhost:8096/swagger-ui.html
echo  Compensation       : http://localhost:8097/swagger-ui.html
echo  Compliance         : http://localhost:8098/swagger-ui.html
echo  Offboarding        : http://localhost:8099/swagger-ui.html
echo  LMS                : http://localhost:8100/swagger-ui.html
echo  Workflow           : http://localhost:8101/swagger-ui.html
echo  Reports            : http://localhost:8102/swagger-ui.html
echo  Files              : http://localhost:8103/swagger-ui.html
echo  Forms              : http://localhost:8104/swagger-ui.html
echo  Engagement         : http://localhost:8105/swagger-ui.html
echo  Skills             : http://localhost:8106/swagger-ui.html
echo  Integrations       : http://localhost:8107/swagger-ui.html
echo  Workplace          : http://localhost:8108/swagger-ui.html
echo  Travel             : http://localhost:8109/swagger-ui.html
echo  Timesheet          : http://localhost:8110/swagger-ui.html
echo  Cases              : http://localhost:8111/swagger-ui.html
echo.
echo  Logs: %ROOT%\logs\
echo ============================================================

endlocal
