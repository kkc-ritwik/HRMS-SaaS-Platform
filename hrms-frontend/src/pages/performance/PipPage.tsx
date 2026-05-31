import { useQuery } from '@tanstack/react-query'
import { AlertTriangle } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { pipService } from '@/services/extendedServices'

interface Pip { id: string; employeeName?: string; managerName?: string; startDate: string; endDate?: string; status: string }

export function PipPage() {
  const { data, isLoading } = useQuery({ queryKey: ['pip'], queryFn: () => pipService.list() })
  const items: Pip[] = (data as { content?: Pip[] } | undefined)?.content
    || (Array.isArray(data) ? data as Pip[] : [])
  return (
    <DataList<Pip>
      title="Performance Improvement Plans" description="Active PIPs across the org"
      data={items} isLoading={isLoading}
      emptyIcon={<AlertTriangle className="h-10 w-10" />} emptyTitle="No active PIPs"
      columns={[
        { key: 'employeeName', label: 'Employee' },
        { key: 'managerName', label: 'Manager' },
        { key: 'startDate', label: 'Start' },
        { key: 'endDate', label: 'End' },
        { key: 'status', label: 'Status', render: p => <Badge>{p.status}</Badge> },
      ]}
    />
  )
}
