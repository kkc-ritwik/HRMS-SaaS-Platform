import { useQuery } from '@tanstack/react-query'
import { UserPlus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { DataList } from '@/components/ui/data-list'
import { onboardingService, type OnboardingWorkflow } from '@/services/onboardingService'

export function OnboardingPage() {
  const { data, isLoading } = useQuery({ queryKey: ['onboarding'], queryFn: onboardingService.listWorkflows })
  return (
    <DataList<OnboardingWorkflow>
      title="Onboarding" description="Active and recent new-hire workflows"
      data={data || []} isLoading={isLoading}
      emptyIcon={<UserPlus className="h-10 w-10" />} emptyTitle="No active onboarding"
      columns={[
        { key: 'employeeId', label: 'Employee' },
        { key: 'startDate', label: 'Start date' },
        { key: 'progressPercent', label: 'Progress', render: w => <div className="w-32"><Progress value={w.progressPercent} /></div> },
        { key: 'status', label: 'Status', render: w => <Badge>{w.status}</Badge> },
        { key: 'expectedCompletion', label: 'Expected done' },
      ]}
    />
  )
}
