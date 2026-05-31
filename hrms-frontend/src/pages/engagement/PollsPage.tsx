import { useQuery } from '@tanstack/react-query'
import { BarChart3 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { pollService } from '@/services/extendedServices'

interface Poll { id: string; question: string; totalVotes: number; isActive: boolean; endsAt: string }

export function PollsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['polls'], queryFn: () => pollService.list() })
  const items: Poll[] = (data as { content?: Poll[] } | undefined)?.content
    || (Array.isArray(data) ? data as Poll[] : [])
  return (
    <DataList<Poll>
      title="Polls" description="Active and past quick polls"
      data={items} isLoading={isLoading}
      emptyIcon={<BarChart3 className="h-10 w-10" />} emptyTitle="No active polls"
      columns={[
        { key: 'question', label: 'Question' },
        { key: 'totalVotes', label: 'Votes' },
        { key: 'endsAt', label: 'Ends' },
        { key: 'isActive', label: 'Active', render: p => <Badge>{p.isActive ? 'Open' : 'Closed'}</Badge> },
      ]}
    />
  )
}
