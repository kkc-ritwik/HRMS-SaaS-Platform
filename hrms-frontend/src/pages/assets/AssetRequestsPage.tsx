import { ClipboardList, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { assetRequestService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface AssetRequest extends Record<string, unknown> { id: string; requestedByName?: string; requestedBy?: string; category?: string; assetType?: string; reason?: string; status: string; requestedAt?: string }

export function AssetRequestsPage() {
  return (
    <ResourcePage<AssetRequest>
      title="Asset Requests"
      description="New asset requests awaiting fulfilment — approve, reject, raise"
      icon={<ClipboardList className="h-10 w-10" />}
      queryKey={['asset-requests']}
      fetcher={() => assetRequestService.list()}
      filters={{ status: ['PENDING', 'APPROVED', 'REJECTED', 'FULFILLED'] }}
      columns={[
        { key: 'requestedByName', label: 'Requested by', render: r => String(r.requestedByName ?? r.requestedBy ?? '—') },
        { key: 'assetType', label: 'Type', render: r => String(r.assetType ?? r.category ?? '—') },
        { key: 'reason', label: 'Reason' },
        { key: 'status', label: 'Status', render: r => <Badge variant={r.status === 'REJECTED' ? 'destructive' : r.status === 'APPROVED' || r.status === 'FULFILLED' ? 'success' : 'warning'}>{String(r.status)}</Badge> },
        { key: 'requestedAt', label: 'Date', render: r => r.requestedAt ? formatDate(String(r.requestedAt)) : '—' },
      ]}
      formFields={[
        { name: 'assetType', label: 'Asset type', type: 'text', required: true },
        { name: 'reason', label: 'Reason', type: 'textarea', required: true, span: 2 },
        { name: 'neededBy', label: 'Needed by', type: 'date' },
      ]}
      onCreate={v => assetRequestService.raise(v)}
      onUpdate={(id, v) => Catalog.assets.requests.update(id, v)}
      onDelete={id => Catalog.assets.requests.delete(id)}
      rowActions={r => [
        { label: 'Approve', icon: <Check className="h-3.5 w-3.5" />, show: r.status === 'PENDING', run: () => assetRequestService.approve(r.id) },
        { label: 'Reject', icon: <X className="h-3.5 w-3.5" />, show: r.status === 'PENDING', destructive: true, confirm: 'Reject this asset request?', run: () => Catalog.assets.requests.reject(r.id, 'Rejected') },
      ]}
    />
  )
}
