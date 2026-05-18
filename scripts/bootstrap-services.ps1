# Bootstrap script — used once to scaffold the 8 new services.
# Run from the repo root: powershell -ExecutionPolicy Bypass -File scripts/bootstrap-services.ps1
# Each generated service is later expanded with real entities/services/controllers.

$ErrorActionPreference = 'Stop'
$root = (Get-Location).Path

$services = @(
  @{ name='service-travel';       port=8100; pkg='travel' },
  @{ name='service-timesheet';    port=8101; pkg='timesheet' },
  @{ name='service-cases';        port=8102; pkg='cases' },
  @{ name='service-files';        port=8103; pkg='files' },
  @{ name='service-forms';        port=8104; pkg='forms' },
  @{ name='service-engagement';   port=8105; pkg='engagement' },
  @{ name='service-skills';       port=8106; pkg='skills' },
  @{ name='service-integrations'; port=8107; pkg='integrations' }
)

foreach ($s in $services) {
  $svc  = Join-Path $root $s.name
  $java = Join-Path $svc  "src/main/java/com/hrms/$($s.pkg)"
  $res  = Join-Path $svc  "src/main/resources"
  $mig  = Join-Path $res  "db/migration"
  New-Item -ItemType Directory -Force -Path $java | Out-Null
  New-Item -ItemType Directory -Force -Path $mig  | Out-Null
  Write-Host "Bootstrapped $($s.name)"
}
