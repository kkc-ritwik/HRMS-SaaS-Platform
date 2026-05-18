# Idempotently add missing common-* deps to every service pom.
# Modules that already have the dep are skipped (string match on artifactId).
# Run from repo root:
#   powershell -ExecutionPolicy Bypass -File scripts/bulk-add-commons.ps1

$ErrorActionPreference = 'Stop'
$root = (Get-Location).Path

# (dep, services-to-add-to) -- skip api-gateway (reactive), service-discovery, config-server.
$additions = @(
    @{ dep = 'common-mail';    services = @('service-asset','service-cases','service-compensation','service-compliance','service-core-hr','service-engagement','service-expense','service-files','service-forms','service-integrations','service-leave-attendance','service-lms','service-performance','service-skills','service-social','service-timesheet','service-travel','service-workflow','service-workplace') },
    @{ dep = 'common-storage'; services = @('service-auth','service-core-hr','service-engagement','service-forms','service-helpdesk','service-integrations','service-leave-attendance','service-notification','service-onboarding','service-performance','service-skills','service-social','service-timesheet','service-travel','service-workflow','service-workplace') }
)

$applied = 0
foreach ($a in $additions) {
    foreach ($s in $a.services) {
        $pomPath = Join-Path $root "$s/pom.xml"
        if (-not (Test-Path $pomPath)) { continue }
        $content = [System.IO.File]::ReadAllText($pomPath)
        if ($content -match ("<artifactId>" + [regex]::Escape($a.dep) + "</artifactId>")) { continue }
        # Insert the new dep just after common-tenant
        $newLine = "        <dependency><groupId>com.hrms</groupId><artifactId>$($a.dep)</artifactId></dependency>"
        $content = $content -replace '(<dependency><groupId>com\.hrms</groupId><artifactId>common-tenant</artifactId></dependency>)', "`$1`r`n$newLine"
        [System.IO.File]::WriteAllText($pomPath, $content)
        $applied++
        Write-Host "Added $($a.dep) to $s"
    }
}
Write-Output ("Total dep additions: " + $applied)
