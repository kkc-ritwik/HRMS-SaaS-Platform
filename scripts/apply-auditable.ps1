# Bulk-apply @Auditable + @EntityListeners(AuditEntityListener.class) to every JPA entity
# that doesn't already carry them. Idempotent — safe to re-run.
# Run from repo root:
#   powershell -ExecutionPolicy Bypass -File scripts/apply-auditable.ps1

$ErrorActionPreference = 'Stop'
$root = (Get-Location).Path

$entityFiles = Get-ChildItem -Recurse -Path "$root/service-*","$root/common-*" `
    -Filter "*.java" | Where-Object {
        $c = Get-Content $_.FullName -Raw
        ($c -match '@Entity') -and ($c -notmatch '@Auditable')
    }

$applied = 0
foreach ($f in $entityFiles) {
    $content = Get-Content $f.FullName -Raw
    $className = $f.BaseName

    # Skip @Entity declarations that aren't real classes (rare)
    if ($content -notmatch 'public\s+class\s+' + [regex]::Escape($className)) { continue }

    # Insert imports if missing
    if ($content -notmatch 'import com\.hrms\.audit\.annotation\.Auditable;') {
        $content = $content -replace '(?m)^(package [^;]+;\s*\r?\n)', "`$1`r`nimport com.hrms.audit.annotation.Auditable;`r`nimport com.hrms.audit.listener.AuditEntityListener;`r`nimport jakarta.persistence.EntityListeners;`r`n"
    }

    # Insert @Auditable + @EntityListeners just above the class declaration
    $entityName = $className
    $auditAnno = "@Auditable(`"$entityName`")`r`n@EntityListeners(AuditEntityListener.class)"
    $pattern = '(?ms)(@Entity[^\r\n]*\r?\n(?:@Table[^\r\n]*\r?\n)?(?:@Getter[^\r\n]*\r?\n)?(?:@Setter[^\r\n]*\r?\n)?(?:@NoArgsConstructor[^\r\n]*\r?\n)?(?:@AllArgsConstructor[^\r\n]*\r?\n)?(?:@Builder[^\r\n]*\r?\n)?)(public\s+class\s+' + [regex]::Escape($className) + ')'
    $content = [regex]::Replace($content, $pattern, "`$1$auditAnno`r`n`$2")

    Set-Content -Path $f.FullName -Value $content -NoNewline -Encoding UTF8
    $applied++
}
Write-Host "Applied @Auditable to $applied entity files"
