import { useQuery } from '@tanstack/react-query'
import { Wrench } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { assetMaintenanceService } from '@/services/extendedServices'

interface Maintenance { id: string; assetId: string; scheduledOn: string; type: string; status: string; vendor?: string; cost?: number }

export function AssetMaintenancePage() {
  const { data, isLoading } = useQuery({ queryKey: ['asset-maint'], queryFn: () => assetMaintenanceService.list() })
  const items: Maintenance[] = (data as { content?: Maintenance[] } | undefined)?.content
    || (Array.isArray(data) ? data as Maintenance[] : [])
  return (
    <DataList<Maintenance>
      title="Asset Maintenance" description="Scheduled and completed servicing"
      data={items} isLoading={isLoading}
      emptyIcon={<Wrench className="h-10 w-10" />} emptyTitle="No maintenance scheduled"
      columns={[
        { key: 'assetId', label: 'Asset' },
        { key: 'type', label: 'Type' },
        { key: 'scheduledOn', label: 'Date' },
        { key: 'vendor', label: 'Vendor' },
        { key: 'cost', label: 'Cost', align: 'right' },
        { key: 'status', label: 'Status', render: m => <Badge>{m.status}</Badge> },
      ]}
    />
  )
}
