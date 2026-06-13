import { MessageCircleHeart, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { stayInterviewService } from '@/services/extendedServices'
import { formatDate } from '@/lib/utils'

interface StayInterview extends Record<string, unknown> { id: string; employeeId?: string; employeeName?: string; interviewerId?: string; scheduledDate?: string; status: string; trigger?: string; flightRisk?: string }

export function StayInterviewsPage() {
  return (
    <ResourcePage<StayInterview>
      title="Stay Interviews"
      description="Proactive retention conversations — schedule, complete"
      icon={<MessageCircleHeart className="h-10 w-10" />}
      queryKey={['stay-interviews']}
      fetcher={() => stayInterviewService.list()}
      filters={{ status: ['SCHEDULED', 'COMPLETED', 'CANCELLED'], flightRisk: ['LOW', 'MEDIUM', 'HIGH'] }}
      columns={[
        { key: 'employeeName', label: 'Employee', render: s => String(s.employeeName ?? s.employeeId ?? '—') },
        { key: 'interviewerId', label: 'Interviewer' },
        { key: 'scheduledDate', label: 'When', render: s => s.scheduledDate ? formatDate(String(s.scheduledDate)) : '—' },
        { key: 'trigger', label: 'Trigger', render: s => s.trigger ? <Badge>{String(s.trigger)}</Badge> : '—' },
        { key: 'flightRisk', label: 'Flight risk', render: s => s.flightRisk ? <Badge variant={s.flightRisk === 'HIGH' ? 'destructive' : s.flightRisk === 'MEDIUM' ? 'warning' : 'secondary'}>{String(s.flightRisk)}</Badge> : '—' },
        { key: 'status', label: 'Status', render: s => <Badge>{String(s.status)}</Badge> },
      ]}
      formFields={[
        { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
        { name: 'scheduledDate', label: 'Scheduled date', type: 'date', required: true },
        { name: 'trigger', label: 'Trigger', type: 'select', options: [
          { value: 'TENURE_MILESTONE', label: 'Tenure milestone' }, { value: 'MANAGER_CHANGE', label: 'Manager change' },
          { value: 'FLIGHT_RISK', label: 'Flight risk' }, { value: 'POST_PROMOTION', label: 'Post promotion' }, { value: 'ADHOC', label: 'Ad-hoc' },
        ] },
      ]}
      onCreate={v => stayInterviewService.schedule(v)}
      rowActions={s => [
        { label: 'Complete', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: s.status === 'SCHEDULED', run: () => stayInterviewService.complete(s.id, { completedAt: new Date().toISOString() }) },
      ]}
    />
  )
}
