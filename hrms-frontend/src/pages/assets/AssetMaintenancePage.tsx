import { Wrench, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { assetMaintenanceService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Maintenance extends Record<string, unknown> { id: string; assetId?: string; assetName?: string; scheduledOn?: string; scheduledAt?: string; type?: string; maintenanceType?: string; status: string; vendor?: string; cost?: number }

export function AssetMaintenancePage() {
  return (
    <ResourcePage<Maintenance>
      title="Asset Maintenance"
      description="Scheduled and completed servicing — schedule, complete, edit"
      icon={<Wrench className="h-10 w-10" />}
      queryKey={['asset-maint']}
      fetcher={() => assetMaintenanceService.list()}
      filters={{ status: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'] }}
      columns={[
        { key: 'assetName', label: 'Asset', render: m => String(m.assetName ?? m.assetId ?? '—') },
        { key: 'type', label: 'Type', render: m => String(m.maintenanceType ?? m.type ?? '—') },
        { key: 'scheduledOn', label: 'Date', render: m => { const d = m.scheduledAt ?? m.scheduledOn; return d ? formatDate(String(d)) : '—' } },
        { key: 'vendor', label: 'Vendor' },
        { key: 'cost', label: 'Cost', align: 'right', render: m => m.cost ? `₹${Number(m.cost).toLocaleString()}` : '—' },
        { key: 'status', label: 'Status', render: m => <Badge variant={m.status === 'COMPLETED' ? 'success' : 'warning'}>{String(m.status)}</Badge> },
      ]}
      formFields={[
        { name: 'assetId', label: 'Asset ID', type: 'text', required: true },
        { name: 'maintenanceType', label: 'Type', type: 'select', required: true, options: [
          { value: 'PREVENTIVE', label: 'Preventive' }, { value: 'CORRECTIVE', label: 'Corrective' },
          { value: 'INSPECTION', label: 'Inspection' }, { value: 'REPAIR', label: 'Repair' },
        ] },
        { name: 'scheduledAt', label: 'Scheduled date', type: 'date', required: true },
        { name: 'vendor', label: 'Vendor', type: 'text' },
        { name: 'cost', label: 'Estimated cost', type: 'currency' },
        { name: 'notes', label: 'Notes', type: 'textarea', span: 2 },
      ]}
      onCreate={v => assetMaintenanceService.schedule(v)}
      onUpdate={(id, v) => Catalog.assets.maintenance.update(id, v)}
      onDelete={id => Catalog.assets.maintenance.delete(id)}
      rowActions={m => [
        { label: 'Mark complete', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: m.status !== 'COMPLETED', run: () => assetMaintenanceService.complete(m.id, 'Completed') },
      ]}
    />
  )
}
