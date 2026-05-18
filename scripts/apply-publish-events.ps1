# Apply @PublishEvents + EntityLifecyclePublisher listener to the high-traffic entities.
# Idempotent. Run from repo root:
#   powershell -ExecutionPolicy Bypass -File scripts/apply-publish-events.ps1

$ErrorActionPreference = 'Stop'
$root = (Get-Location).Path

# (relative path, Topics constant, namespace)
$entities = @(
    @{ path = 'service-leave-attendance/src/main/java/com/hrms/leave/entity/LeaveApplication.java'; topic = 'LEAVE';        ns = 'leave.application' },
    @{ path = 'service-leave-attendance/src/main/java/com/hrms/leave/entity/AttendancePunch.java';  topic = 'ATTENDANCE';   ns = 'attendance.punch' },
    @{ path = 'service-leave-attendance/src/main/java/com/hrms/leave/entity/ShiftSwapRequest.java'; topic = 'LEAVE';        ns = 'leave.shift_swap' },
    @{ path = 'service-leave-attendance/src/main/java/com/hrms/leave/entity/WfhRequest.java';       topic = 'LEAVE';        ns = 'leave.wfh' },
    @{ path = 'service-payroll/src/main/java/com/hrms/payroll/entity/Payslip.java';                  topic = 'PAYROLL';      ns = 'payroll.payslip' },
    @{ path = 'service-payroll/src/main/java/com/hrms/payroll/entity/PayrollRun.java';               topic = 'PAYROLL';      ns = 'payroll.run' },
    @{ path = 'service-payroll/src/main/java/com/hrms/payroll/entity/Loan.java';                     topic = 'PAYROLL';      ns = 'payroll.loan' },
    @{ path = 'service-expense/src/main/java/com/hrms/expense/entity/ExpenseReport.java';            topic = 'EXPENSE';      ns = 'expense.report' },
    @{ path = 'service-expense/src/main/java/com/hrms/expense/entity/Advance.java';                  topic = 'EXPENSE';      ns = 'expense.advance' },
    @{ path = 'service-asset/src/main/java/com/hrms/asset/entity/Asset.java';                        topic = 'ASSET';        ns = 'asset' },
    @{ path = 'service-asset/src/main/java/com/hrms/asset/entity/AssetAssignment.java';              topic = 'ASSET';        ns = 'asset.assignment' },
    @{ path = 'service-document/src/main/java/com/hrms/document/entity/Document.java';               topic = 'DOCUMENT';     ns = 'document' },
    @{ path = 'service-document/src/main/java/com/hrms/document/entity/CompanyPolicy.java';          topic = 'DOCUMENT';     ns = 'document.policy' },
    @{ path = 'service-recruitment/src/main/java/com/hrms/recruitment/entity/JobRequisition.java';   topic = 'RECRUITMENT';  ns = 'recruitment.requisition' },
    @{ path = 'service-recruitment/src/main/java/com/hrms/recruitment/entity/Candidate.java';        topic = 'RECRUITMENT';  ns = 'recruitment.candidate' },
    @{ path = 'service-recruitment/src/main/java/com/hrms/recruitment/entity/Application.java';      topic = 'RECRUITMENT';  ns = 'recruitment.application' },
    @{ path = 'service-recruitment/src/main/java/com/hrms/recruitment/entity/OfferLetter.java';      topic = 'RECRUITMENT';  ns = 'recruitment.offer' },
    @{ path = 'service-performance/src/main/java/com/hrms/performance/entity/Review.java';           topic = 'PERFORMANCE';  ns = 'performance.review' },
    @{ path = 'service-performance/src/main/java/com/hrms/performance/entity/Goal.java';             topic = 'PERFORMANCE';  ns = 'performance.goal' },
    @{ path = 'service-performance/src/main/java/com/hrms/performance/entity/PipPlan.java';          topic = 'PERFORMANCE';  ns = 'performance.pip' },
    @{ path = 'service-helpdesk/src/main/java/com/hrms/helpdesk/entity/Ticket.java';                 topic = 'HELPDESK';     ns = 'helpdesk.ticket' },
    @{ path = 'service-offboarding/src/main/java/com/hrms/offboarding/entity/Separation.java';       topic = 'OFFBOARDING';  ns = 'offboarding.separation' },
    @{ path = 'service-onboarding/src/main/java/com/hrms/onboarding/entity/OnboardingTask.java';     topic = 'ONBOARDING';   ns = 'onboarding.task' },
    @{ path = 'service-onboarding/src/main/java/com/hrms/onboarding/entity/ProbationReview.java';    topic = 'ONBOARDING';   ns = 'onboarding.probation' },
    @{ path = 'service-compensation/src/main/java/com/hrms/compensation/equity/StockGrant.java';     topic = 'PAYROLL';      ns = 'compensation.stock_grant' },
    @{ path = 'service-compensation/src/main/java/com/hrms/compensation/increment/IncrementProposal.java'; topic = 'PAYROLL'; ns = 'compensation.increment' },
    @{ path = 'service-cases/src/main/java/com/hrms/cases/entity/HrCase.java';                       topic = 'CASES';        ns = 'cases' },
    @{ path = 'service-engagement/src/main/java/com/hrms/engagement/awards/Award.java';              topic = 'ENGAGEMENT';   ns = 'engagement.award' },
    @{ path = 'service-engagement/src/main/java/com/hrms/engagement/entity/Survey.java';             topic = 'ENGAGEMENT';   ns = 'engagement.survey' },
    @{ path = 'service-timesheet/src/main/java/com/hrms/timesheet/entity/WeeklyTimesheet.java';      topic = 'TIMESHEET';    ns = 'timesheet.week' }
)

$applied = 0; $skipped = 0
foreach ($e in $entities) {
    $file = Join-Path $root $e.path
    if (-not (Test-Path $file)) { Write-Host "SKIP missing: $($e.path)"; continue }
    $raw = [System.IO.File]::ReadAllText($file)
    if ($raw -match '@PublishEvents') { $skipped++; continue }

    # Add the imports if missing
    if ($raw -notmatch 'import com\.hrms\.events\.annotation\.PublishEvents;') {
        $raw = $raw -replace '(?m)^(import com\.hrms\.tenant\.entity\.BaseEntity;)',
            "import com.hrms.events.annotation.PublishEvents;`r`nimport com.hrms.events.listener.EntityLifecyclePublisher;`r`nimport com.hrms.events.model.Topics;`r`n`$1"
    }

    $pubAnno = "@PublishEvents(topic = Topics.$($e.topic), namespace = `"$($e.ns)`")"

    # Insert @PublishEvents above class declaration; extend any existing @EntityListeners.
    if ($raw -match '@EntityListeners\(AuditEntityListener\.class\)') {
        $raw = $raw -replace '@EntityListeners\(AuditEntityListener\.class\)',
            "$pubAnno`r`n@EntityListeners({AuditEntityListener.class, EntityLifecyclePublisher.class})"
    } elseif ($raw -match '@EntityListeners\(\{([^}]+)\}\)') {
        $existing = $matches[1]
        if ($existing -notmatch 'EntityLifecyclePublisher') {
            $raw = $raw -replace '@EntityListeners\(\{([^}]+)\}\)',
                "$pubAnno`r`n@EntityListeners({`$1, EntityLifecyclePublisher.class})"
        }
    } else {
        # No existing listener — insert annotation + listener above class
        $raw = $raw -replace '(?m)^(public\s+(?:abstract\s+)?class\s+\w+)',
            "$pubAnno`r`n@EntityListeners(EntityLifecyclePublisher.class)`r`n`$1"
    }
    [System.IO.File]::WriteAllText($file, $raw, [System.Text.Encoding]::UTF8)
    $applied++
    Write-Host "Applied to $($e.path)"
}
Write-Output ("Applied: " + $applied + " | Skipped (already has it): " + $skipped)
