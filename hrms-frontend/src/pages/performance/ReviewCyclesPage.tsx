import { useQuery } from '@tanstack/react-query'
import { Repeat } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { reviewCycleService } from '@/services/extendedServices'

interface ReviewCycle { id: string; name: string; period: string; status: string; startDate?: string; endDate?: string }

export function ReviewCyclesPage() {
  const { data, isLoading } = useQuery({ queryKey: ['review-cycles'], queryFn: () => reviewCycleService.list() })
  const items: ReviewCycle[] = (data as { content?: ReviewCycle[] } | undefined)?.content
    || (Array.isArray(data) ? data as ReviewCycle[] : [])
  return (
    <DataList<ReviewCycle>
      title="Review Cycles" description="Annual/half-yearly performance cycles"
      data={items} isLoading={isLoading}
      emptyIcon={<Repeat className="h-10 w-10" />} emptyTitle="No cycles configured"
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'period', label: 'Period' },
        { key: 'startDate', label: 'Start' },
        { key: 'endDate', label: 'End' },
        { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
      ]}
    />
  )
}
