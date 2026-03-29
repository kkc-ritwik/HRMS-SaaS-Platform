@echo off
setlocal EnableDelayedExpansion

echo ============================================================
echo  HRMS Backend Integration Test
echo ============================================================
echo.

set PASS=0
set FAIL=0
set GATEWAY=http://localhost:8080
set AUTH_URL=http://localhost:8081
set COREHR_URL=http://localhost:8082
set TENANT_ID=demo

:: ── Start infrastructure check ────────────────────────────────────────────────
echo [PRE] Checking Docker containers...
docker exec hrms-postgres pg_isready -U hrms_user -d hrms_db >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] PostgreSQL not running. Run scripts\verify-infra.bat first.
    exit /b 1
)
echo [OK] PostgreSQL running.
echo.

:: ── Start Eureka ─────────────────────────────────────────────────────────────
echo [1] Starting Eureka Service Discovery...
if not exist "%~dp0..\logs" mkdir "%~dp0..\logs"
start "service-discovery" /min cmd /c "cd /d "%~dp0..\service-discovery" && mvn spring-boot:run -DskipTests > "%~dp0..\logs\service-discovery.log" 2>&1"
echo     Waiting 25 seconds for Eureka...
timeout /t 25 /nobreak >nul

curl -s http://localhost:8761/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Eureka is UP
    set /a PASS+=1
) else (
    echo [FAIL] Eureka health check failed
    set /a FAIL+=1
)
echo.

:: ── Start Auth Service ────────────────────────────────────────────────────────
echo [2] Starting Auth Service...
start "service-auth" /min cmd /c "cd /d "%~dp0..\service-auth" && mvn spring-boot:run -DskipTests > "%~dp0..\logs\service-auth.log" 2>&1"
echo     Waiting 20 seconds for Auth...
timeout /t 20 /nobreak >nul

curl -s %AUTH_URL%/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Auth Service is UP
    set /a PASS+=1
) else (
    echo [FAIL] Auth Service health check failed
    set /a FAIL+=1
)
echo.

:: ── Start Core HR Service ─────────────────────────────────────────────────────
echo [3] Starting Core HR Service...
start "service-core-hr" /min cmd /c "cd /d "%~dp0..\service-core-hr" && mvn spring-boot:run -DskipTests > "%~dp0..\logs\service-core-hr.log" 2>&1"
echo     Waiting 20 seconds for Core HR...
timeout /t 20 /nobreak >nul

curl -s %COREHR_URL%/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Core HR Service is UP
    set /a PASS+=1
) else (
    echo [FAIL] Core HR Service health check failed
    set /a FAIL+=1
)
echo.

:: ── Test: Signup ──────────────────────────────────────────────────────────────
echo [TEST] Signup a test admin user...
curl -s -X POST %AUTH_URL%/api/v1/auth/signup ^
  -H "Content-Type: application/json" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  -d "{\"email\":\"test.admin@hrms.com\",\"password\":\"Admin@1234\",\"fullName\":\"Test Admin\",\"tenantId\":\"%TENANT_ID%\"}" ^
  > tmp_signup.json 2>nul

findstr "accessToken" tmp_signup.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Signup succeeded
    set /a PASS+=1
) else (
    echo [INFO] Signup may have failed (user may already exist) - trying login
)
echo.

:: ── Test: Login ───────────────────────────────────────────────────────────────
echo [TEST] Login as test admin...
curl -s -X POST %AUTH_URL%/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  -d "{\"email\":\"test.admin@hrms.com\",\"password\":\"Admin@1234\",\"tenantId\":\"%TENANT_ID%\"}" ^
  > tmp_login.json 2>nul

findstr "accessToken" tmp_login.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Login succeeded
    set /a PASS+=1
    :: Extract token (basic extraction - full solution uses PowerShell or jq)
    for /f "tokens=2 delims=:," %%a in ('findstr "accessToken" tmp_login.json') do (
        set TOKEN_RAW=%%a
    )
    set TOKEN=!TOKEN_RAW:"=!
    set TOKEN=!TOKEN: =!
) else (
    echo [FAIL] Login failed - check service-auth log
    set /a FAIL+=1
    set TOKEN=INVALID
)
echo.

:: ── Test: Get Me ──────────────────────────────────────────────────────────────
echo [TEST] GET /api/v1/users/me...
curl -s -X GET %AUTH_URL%/api/v1/users/me ^
  -H "Authorization: Bearer !TOKEN!" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  > tmp_me.json 2>nul

findstr "email" tmp_me.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] GET /me succeeded
    set /a PASS+=1
) else (
    echo [FAIL] GET /me failed
    set /a FAIL+=1
)
echo.

:: ── Test: Create Department ───────────────────────────────────────────────────
echo [TEST] Create Department...
curl -s -X POST %COREHR_URL%/api/v1/departments ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer !TOKEN!" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  -d "{\"name\":\"Engineering\",\"code\":\"ENG\",\"description\":\"Software Engineering Department\"}" ^
  > tmp_dept.json 2>nul

findstr "\"id\"" tmp_dept.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Create Department succeeded
    set /a PASS+=1
) else (
    echo [FAIL] Create Department failed
    set /a FAIL+=1
)
echo.

:: ── Test: List Departments ────────────────────────────────────────────────────
echo [TEST] List Departments...
curl -s -X GET %COREHR_URL%/api/v1/departments ^
  -H "Authorization: Bearer !TOKEN!" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  > tmp_depts.json 2>nul

findstr "data" tmp_depts.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] List Departments succeeded
    set /a PASS+=1
) else (
    echo [FAIL] List Departments failed
    set /a FAIL+=1
)
echo.

:: ── Test: Create Designation ──────────────────────────────────────────────────
echo [TEST] Create Designation...
curl -s -X POST %COREHR_URL%/api/v1/designations ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer !TOKEN!" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  -d "{\"name\":\"Senior Software Engineer\",\"code\":\"SSE\",\"level\":\"L3\"}" ^
  > tmp_desig.json 2>nul

findstr "\"id\"" tmp_desig.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Create Designation succeeded
    set /a PASS+=1
) else (
    echo [FAIL] Create Designation failed
    set /a FAIL+=1
)
echo.

:: ── Test: Create Location ─────────────────────────────────────────────────────
echo [TEST] Create Location...
curl -s -X POST %COREHR_URL%/api/v1/locations ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer !TOKEN!" ^
  -H "X-Tenant-ID: %TENANT_ID%" ^
  -d "{\"name\":\"Mumbai HQ\",\"code\":\"MUM\",\"city\":\"Mumbai\",\"state\":\"Maharashtra\",\"country\":\"India\"}" ^
  > tmp_loc.json 2>nul

findstr "\"id\"" tmp_loc.json >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Create Location succeeded
    set /a PASS+=1
) else (
    echo [FAIL] Create Location failed
    set /a FAIL+=1
)
echo.

:: ── Cleanup temp files ────────────────────────────────────────────────────────
del tmp_signup.json tmp_login.json tmp_me.json tmp_dept.json tmp_depts.json tmp_desig.json tmp_loc.json >nul 2>&1

:: ── Summary ───────────────────────────────────────────────────────────────────
echo.
echo ============================================================
echo  TEST RESULTS
echo ============================================================
echo  PASSED: !PASS!
echo  FAILED: !FAIL!
echo.
if !FAIL! equ 0 (
    echo  STATUS: ALL TESTS PASSED
) else (
    echo  STATUS: SOME TESTS FAILED - check logs\ directory
)
echo ============================================================
echo.
echo NOTE: Services are still running. Use scripts\stop.bat to stop them.

endlocal
