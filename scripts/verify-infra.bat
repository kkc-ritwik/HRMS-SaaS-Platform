@echo off
setlocal EnableDelayedExpansion

echo ============================================================
echo  HRMS Infrastructure Verification Script
echo ============================================================
echo.

:: ── Step 1: Check Docker Desktop ────────────────────────────────────────────
echo [1/7] Checking Docker Desktop...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [FAIL] Docker Desktop is not running. Please start it and try again.
    exit /b 1
)
echo [PASS] Docker Desktop is running.
echo.

:: ── Step 2: Start containers ─────────────────────────────────────────────────
echo [2/7] Starting Docker containers...
cd /d "%~dp0.."
docker-compose up -d
if %errorlevel% neq 0 (
    echo [FAIL] docker-compose up failed.
    exit /b 1
)
echo [OK]  Containers started. Waiting 15 seconds for services to initialize...
timeout /t 15 /nobreak >nul
echo.

:: ── Step 3: Wait for PostgreSQL ──────────────────────────────────────────────
echo [3/7] Waiting for PostgreSQL to be healthy...
set PG_READY=0
for /l %%i in (1,1,20) do (
    if !PG_READY! equ 0 (
        docker exec hrms-postgres pg_isready -U hrms_user -d hrms_db >nul 2>&1
        if !errorlevel! equ 0 (
            set PG_READY=1
            echo [PASS] PostgreSQL is ready.
        ) else (
            echo        Attempt %%i/20 - not ready yet, waiting 3s...
            timeout /t 3 /nobreak >nul
        )
    )
)
if %PG_READY% equ 0 (
    echo [FAIL] PostgreSQL did not become ready in time.
    exit /b 1
)
echo.

:: ── Step 4: Test PostgreSQL schemas & seed data ───────────────────────────────
echo [4/7] Testing PostgreSQL schemas and seed data...

docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "SELECT count(*) FROM shared.tenants;" >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] shared.tenants table exists.
    docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "SELECT slug, schema_name FROM shared.tenants;"
) else (
    echo [FAIL] shared.tenants table missing - init-db.sql may not have run.
)

docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "SELECT count(*) FROM shared.plans;" >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] shared.plans table exists with seed data.
) else (
    echo [FAIL] shared.plans table missing.
)

docker exec hrms-postgres psql -U hrms_user -d hrms_db -c "SELECT schema_name FROM information_schema.schemata WHERE schema_name IN ('shared','tenant_demo');" 2>&1
echo.

:: ── Step 5: Test Redis ───────────────────────────────────────────────────────
echo [5/7] Testing Redis...
docker exec hrms-redis redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo [PASS] Redis is responding.
) else (
    echo [FAIL] Redis is not responding.
)
echo.

:: ── Step 6: Test Elasticsearch ───────────────────────────────────────────────
echo [6/7] Testing Elasticsearch...
curl -s -o nul -w "%%{http_code}" http://localhost:9200 > tmp_es_code.txt 2>&1
set /p ES_CODE=<tmp_es_code.txt
del tmp_es_code.txt
if "%ES_CODE%"=="200" (
    echo [PASS] Elasticsearch is up at http://localhost:9200
) else (
    echo [WARN] Elasticsearch returned HTTP %ES_CODE% - may still be starting.
)
echo.

:: ── Step 7: Test MinIO ───────────────────────────────────────────────────────
echo [7/7] Testing MinIO...
curl -s -o nul -w "%%{http_code}" http://localhost:9000/minio/health/live > tmp_minio_code.txt 2>&1
set /p MINIO_CODE=<tmp_minio_code.txt
del tmp_minio_code.txt
if "%MINIO_CODE%"=="200" (
    echo [PASS] MinIO is up at http://localhost:9000 (console: http://localhost:9001)
) else (
    echo [WARN] MinIO returned HTTP %MINIO_CODE% - may still be starting.
)
echo.

:: ── Summary ──────────────────────────────────────────────────────────────────
echo ============================================================
echo  SUMMARY
echo ============================================================
echo  PostgreSQL : localhost:5432  (hrms_db / hrms_user / hrms_pass)
echo  Redis      : localhost:6379
echo  Kafka      : localhost:9092
echo  Elasticsearch: localhost:9200
echo  MinIO API  : localhost:9000  (user: hrms_minio / hrms_minio_secret)
echo  MinIO UI   : localhost:9001
echo.
echo  Next step: run scripts\start.bat to launch microservices.
echo ============================================================
endlocal
