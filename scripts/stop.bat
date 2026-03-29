@echo off
echo ============================================================
echo  HRMS Platform - Stop All Services
echo ============================================================
echo.

:: Kill all processes running on HRMS service ports
echo Stopping services by port...

for %%p in (8761 8888 8080 8081 8082 8083 8084 8085 8086 8090 8091 8092 8093 8094 8095 8096 8097 8098 8099 8100 8101 8102) do (
    set PORT=%%p
    for /f "tokens=5" %%a in ('netstat -aon 2^>nul ^| findstr ":%%p " ^| findstr "LISTENING"') do (
        echo Killing process on port %%p (PID: %%a)
        taskkill /PID %%a /F >nul 2>&1
    )
)

:: Also kill by window title (started by start.bat)
echo.
echo Stopping named service windows...
for %%s in (
    "service-discovery"
    "service-auth"
    "service-core-hr"
    "service-leave-attendance"
    "service-payroll"
    "service-recruitment"
    "service-performance"
    "service-onboarding"
    "service-document"
    "service-notification"
    "service-expense"
    "service-asset"
    "service-helpdesk"
    "service-social"
    "service-compensation"
    "service-compliance"
    "service-offboarding"
    "service-lms"
    "service-workflow"
    "service-reports"
    "api-gateway"
    "config-server"
) do (
    taskkill /FI "WINDOWTITLE eq %%~s" /F >nul 2>&1
)

echo.
echo All HRMS services stopped.
echo.
echo NOTE: Docker containers (PostgreSQL, Redis, Kafka, etc.) are still running.
echo       To stop them: docker-compose down
echo.
