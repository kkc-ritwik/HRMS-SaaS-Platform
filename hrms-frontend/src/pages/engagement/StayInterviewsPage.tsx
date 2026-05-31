import { useQuery } from '@tanstack/react-query'
import { MessageCircleHeart } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { stayInterviewService } from '@/services/extendedServices'

interface StayInterview { id: string; employeeId: string; interviewerId: string; scheduledDate?: string; status: string; trigger: string; flightRisk?: string }

export function StayInterviewsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['stay-interviews'], queryFn: () => stayInterviewService.list() })
  const items: StayInterview[] = (data as { content?: StayInterview[] } | undefined)?.content
    || (Array.isArray(data) ? data as StayInterview[] : [])
  return (
    <DataList<StayInterview>
      title="Stay Interviews" description="Proactive retention conversations"
      data={items} isLoading={isLoading}
      emptyIcon={<MessageCircleHeart className="h-10 w-10" />} emptyTitle="No stay interviews scheduled"
      columns={[
        { key: 'employeeId', label: 'Employee' },
        { key: 'interviewerId', label: 'Interviewer' },
        { key: 'scheduledDate', label: 'When' },
        { key: 'trigger', label: 'Trigger', render: s => <Badge>{s.trigger}</Badge> },
        { key: 'flightRisk', label: 'Flight risk', render: s => s.flightRisk ? <Badge>{s.flightRisk}</Badge> : '—' },
        { key: 'status', label: 'Status', render: s => <Badge>{s.status}</Badge> },
      ]}
    />
  )
}
