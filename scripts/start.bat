@echo off
setlocal EnableDelayedExpansion

echo ============================================================
echo  HRMS Platform - Service Startup Script
echo ============================================================
echo.

set ROOT=%~dp0..
set JAVA_OPTS=-Xms256m -Xmx512m

:: Parse argument: "full" starts all services, default starts minimum set
set MODE=%1
if "%MODE%"=="" set MODE=minimum

if "%MODE%"=="full" (
    echo Mode: FULL ^(all 22 services^)
) else (
    echo Mode: MINIMUM (Eureka + Auth + Core HR + Leave + Payroll)
    echo         Use:  start.bat full   to start all services
)
echo.

:: ── Tier 1: Service Discovery (must be first) ─────────────────────────────────
echo [1] Starting Eureka Service Discovery (port 8761)...
start "service-discovery" /min cmd /c "cd /d "%ROOT%\service-discovery" && mvn spring-boot:run -DskipTests > ..\logs\service-discovery.log 2>&1"
echo     Waiting 25 seconds for Eureka to start...
timeout /t 25 /nobreak >nul
echo     Checking Eureka health...
curl -s http://localhost:8761/actuator/health >nul 2>&1
if %errorlevel% equ 0 (echo     [PASS] Eureka is up.) else (echo     [WARN] Eureka may still be starting - proceeding anyway.)
echo.

:: ── Tier 2: Auth Service ──────────────────────────────────────────────────────
echo [2] Starting Auth Service (port 8081)...
start "service-auth" /min cmd /c "cd /d "%ROOT%\service-auth" && mvn spring-boot:run -DskipTests > ..\logs\service-auth.log 2>&1"
echo     Waiting 20 seconds...
timeout /t 20 /nobreak >nul
echo.

:: ── Tier 3: Core HR ──────────────────────────────────────────────────────────
echo [3] Starting Core HR Service (port 8082)...
start "service-core-hr" /min cmd /c "cd /d "%ROOT%\service-core-hr" && mvn spring-boot:run -DskipTests > ..\logs\service-core-hr.log 2>&1"
echo     Waiting 20 seconds...
timeout /t 20 /nobreak >nul
echo.

if "%MODE%"=="minimum" goto :minimum_check

:: ── Tier 4: All remaining services ───────────────────────────────────────────
echo [4] Starting Leave ^& Attendance Service (port 8083)...
start "service-leave-attendance" /min cmd /c "cd /d "%ROOT%\service-leave-attendance" && mvn spring-boot:run -DskipTests > ..\logs\service-leave-attendance.log 2>&1"

echo [5] Starting Payroll Service (port 8084)...
start "service-payroll" /min cmd /c "cd /d "%ROOT%\service-payroll" && mvn spring-boot:run -DskipTests > ..\logs\service-payroll.log 2>&1"

echo [6] Starting Recruitment Service (port 8085)...
start "service-recruitment" /min cmd /c "cd /d "%ROOT%\service-recruitment" && mvn spring-boot:run -DskipTests > ..\logs\service-recruitment.log 2>&1"

echo [7] Starting Performance Service (port 8086)...
start "service-performance" /min cmd /c "cd /d "%ROOT%\service-performance" && mvn spring-boot:run -DskipTests > ..\logs\service-performance.log 2>&1"

echo     Waiting 20 seconds for Tier 4 services...
timeout /t 20 /nobreak >nul
echo.

echo [8]  Starting Onboarding Service (port 8090)...
start "service-onboarding" /min cmd /c "cd /d "%ROOT%\service-onboarding" && mvn spring-boot:run -DskipTests > ..\logs\service-onboarding.log 2>&1"

echo [9]  Starting Document Service (port 8091)...
start "service-document" /min cmd /c "cd /d "%ROOT%\service-document" && mvn spring-boot:run -DskipTests > ..\logs\service-document.log 2>&1"

echo [10] Starting Notification Service (port 8092)...
start "service-notification" /min cmd /c "cd /d "%ROOT%\service-notification" && mvn spring-boot:run -DskipTests > ..\logs\service-notification.log 2>&1"

echo [11] Starting Expense Service (port 8093)...
start "service-expense" /min cmd /c "cd /d "%ROOT%\service-expense" && mvn spring-boot:run -DskipTests > ..\logs\service-expense.log 2>&1"

echo [12] Starting Asset Service (port 8094)...
start "service-asset" /min cmd /c "cd /d "%ROOT%\service-asset" && mvn spring-boot:run -DskipTests > ..\logs\service-asset.log 2>&1"

echo [13] Starting Helpdesk Service (port 8095)...
start "service-helpdesk" /min cmd /c "cd /d "%ROOT%\service-helpdesk" && mvn spring-boot:run -DskipTests > ..\logs\service-helpdesk.log 2>&1"

echo [14] Starting Social Service (port 8096)...
start "service-social" /min cmd /c "cd /d "%ROOT%\service-social" && mvn spring-boot:run -DskipTests > ..\logs\service-social.log 2>&1"

echo [15] Starting Compensation Service (port 8097)...
start "service-compensation" /min cmd /c "cd /d "%ROOT%\service-compensation" && mvn spring-boot:run -DskipTests > ..\logs\service-compensation.log 2>&1"

echo [16] Starting Compliance Service (port 8098)...
start "service-compliance" /min cmd /c "cd /d "%ROOT%\service-compliance" && mvn spring-boot:run -DskipTests > ..\logs\service-compliance.log 2>&1"

echo [17] Starting Offboarding Service (port 8099)...
start "service-offboarding" /min cmd /c "cd /d "%ROOT%\service-offboarding" && mvn spring-boot:run -DskipTests > ..\logs\service-offboarding.log 2>&1"

echo [18] Starting LMS Service (port 8100)...
start "service-lms" /min cmd /c "cd /d "%ROOT%\service-lms" && mvn spring-boot:run -DskipTests > ..\logs\service-lms.log 2>&1"

echo [19] Starting Workflow Service (port 8101)...
start "service-workflow" /min cmd /c "cd /d "%ROOT%\service-workflow" && mvn spring-boot:run -DskipTests > ..\logs\service-workflow.log 2>&1"

echo [20] Starting Reports Service (port 8102)...
start "service-reports" /min cmd /c "cd /d "%ROOT%\service-reports" && mvn spring-boot:run -DskipTests > ..\logs\service-reports.log 2>&1"

echo     Waiting 20 seconds for Tier 5 services...
timeout /t 20 /nobreak >nul
echo.

echo [21] Starting API Gateway (port 8080)...
start "api-gateway" /min cmd /c "cd /d "%ROOT%\api-gateway" && mvn spring-boot:run -DskipTests > ..\logs\api-gateway.log 2>&1"
echo     Waiting 15 seconds for API Gateway...
timeout /t 15 /nobreak >nul
goto :done

:minimum_check
echo [4] Starting Leave ^& Attendance Service (port 8083)...
start "service-leave-attendance" /min cmd /c "cd /d "%ROOT%\service-leave-attendance" && mvn spring-boot:run -DskipTests > ..\logs\service-leave-attendance.log 2>&1"

echo [5] Starting Payroll Service (port 8084)...
start "service-payroll" /min cmd /c "cd /d "%ROOT%\service-payroll" && mvn spring-boot:run -DskipTests > ..\logs\service-payroll.log 2>&1"

echo [6] Starting API Gateway (port 8080)...
start "api-gateway" /min cmd /c "cd /d "%ROOT%\api-gateway" && mvn spring-boot:run -DskipTests > ..\logs\api-gateway.log 2>&1"
echo     Waiting 25 seconds for services to finish startup...
timeout /t 25 /nobreak >nul

:done
echo.
echo ============================================================
echo  STARTUP COMPLETE
echo ============================================================
echo  Eureka Dashboard : http://localhost:8761
echo  API Gateway      : http://localhost:8080
echo  Auth Swagger UI  : http://localhost:8081/swagger-ui.html
echo  Core HR Swagger  : http://localhost:8082/swagger-ui.html
echo.
echo  Logs are written to: %ROOT%\logs\
echo ============================================================

:: Create logs directory if needed
if not exist "%ROOT%\logs" mkdir "%ROOT%\logs"

endlocal
