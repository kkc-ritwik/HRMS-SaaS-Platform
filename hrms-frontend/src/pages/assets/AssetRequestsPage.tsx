import { useQuery } from '@tanstack/react-query'
import { ClipboardList } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { assetRequestService } from '@/services/extendedServices'

interface AssetRequest { id: string; requestedBy: string; category: string; reason: string; status: string; requestedAt: string }

export function AssetRequestsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['asset-requests'], queryFn: () => assetRequestService.list() })
  const items: AssetRequest[] = (data as { content?: AssetRequest[] } | undefined)?.content
    || (Array.isArray(data) ? data as AssetRequest[] : [])
  return (
    <DataList<AssetRequest>
      title="Asset Requests" description="New asset requests awaiting fulfilment"
      data={items} isLoading={isLoading}
      emptyIcon={<ClipboardList className="h-10 w-10" />} emptyTitle="No requests"
      columns={[
        { key: 'requestedBy', label: 'Requested by' },
        { key: 'category', label: 'Category' },
        { key: 'reason', label: 'Reason' },
        { key: 'status', label: 'Status', render: r => <Badge>{r.status}</Badge> },
        { key: 'requestedAt', label: 'Date' },
      ]}
    />
  )
}
