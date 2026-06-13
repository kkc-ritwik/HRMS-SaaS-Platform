import { UserPlus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { ResourcePage } from '@/components/ui/resource-page'
import { onboardingService, type OnboardingWorkflow } from '@/services/onboardingService'
import { formatDate } from '@/lib/utils'

export function OnboardingPage() {
  return (
    <ResourcePage<OnboardingWorkflow & Record<string, unknown>>
      title="Onboarding"
      description="Active and recent new-hire workflows — start, track"
      icon={<UserPlus className="h-10 w-10" />}
      queryKey={['onboarding']}
      fetcher={() => onboardingService.listWorkflows()}
      rowHref={w => `/onboarding/${w.id}`}
      filters={{ status: ['NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'OVERDUE'] }}
      columns={[
        { key: 'employeeId', label: 'Employee' },
        { key: 'startDate', label: 'Start date', render: w => w.startDate ? formatDate(String(w.startDate)) : '—' },
        { key: 'progressPercent', label: 'Progress', render: w => <div className="w-32"><Progress value={Number(w.progressPercent ?? 0)} /></div> },
        { key: 'status', label: 'Status', render: w => <Badge>{String(w.status)}</Badge> },
        { key: 'expectedCompletion', label: 'Expected done', render: w => w.expectedCompletion ? formatDate(String(w.expectedCompletion)) : '—' },
      ]}
      formFields={[
        { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
        { name: 'templateId', label: 'Template ID', type: 'text' },
        { name: 'startDate', label: 'Start date', type: 'date' },
      ]}
      createTitle="Start onboarding"
      onCreate={v => onboardingService.startOnboarding(String(v.employeeId), v.templateId ? String(v.templateId) : undefined)}
    />
  )
}
