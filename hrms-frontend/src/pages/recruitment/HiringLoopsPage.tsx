import { useQuery } from '@tanstack/react-query'
import { Users } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { hiringLoopService } from '@/services/extendedServices'

interface HiringLoop {
  id: string
  candidateId: string
  loopDate?: string
  status: string
  outcome?: string
  format?: string
}

export function HiringLoopsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['hiring-loops'], queryFn: () => hiringLoopService.list() })
  const items: HiringLoop[] = (data as { content?: HiringLoop[] } | undefined)?.content
    || (Array.isArray(data) ? data as HiringLoop[] : [])
  return (
    <DataList<HiringLoop>
      title="Hiring Loops" description="Coordinated on-site/remote interview panels"
      data={items} isLoading={isLoading}
      emptyIcon={<Users className="h-10 w-10" />} emptyTitle="No loops scheduled"
      columns={[
        { key: 'candidateId', label: 'Candidate' },
        { key: 'loopDate', label: 'Date' },
        { key: 'format', label: 'Format' },
        { key: 'status', label: 'Status', render: h => <Badge>{h.status}</Badge> },
        { key: 'outcome', label: 'Outcome', render: h => h.outcome ? <Badge>{h.outcome}</Badge> : '—' },
      ]}
    />
  )
}
