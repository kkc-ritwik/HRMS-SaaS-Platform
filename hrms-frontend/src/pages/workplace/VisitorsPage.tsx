import { useQuery } from '@tanstack/react-query'
import { Users2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { assetService } from '@/services/assetService'

interface Visitor {
  id: string
  fullName: string
  company?: string
  hostEmployeeId?: string
  expectedArrival?: string
  status: string
}

export function VisitorsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['visitors', 'today'], queryFn: assetService.visitorsToday })
  const items = (data as Visitor[]) || []
  return (
    <DataList<Visitor>
      title="Visitors Today"
      description="Pre-registered + on-site"
      data={items}
      isLoading={isLoading}
      emptyIcon={<Users2 className="h-10 w-10" />}
      emptyTitle="No visitors today"
      columns={[
        { key: 'fullName', label: 'Name' },
        { key: 'company', label: 'Company' },
        { key: 'hostEmployeeId', label: 'Host' },
        { key: 'expectedArrival', label: 'Expected' },
        { key: 'status', label: 'Status', render: v => <Badge>{v.status}</Badge> },
      ]}
    />
  )
}
