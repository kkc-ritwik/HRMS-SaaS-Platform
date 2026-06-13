import { Package, Trash2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { assetService, type Asset } from '@/services/assetService'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

export function AssetsPage() {
  return (
    <ResourcePage<Asset & Record<string, unknown>>
      title="Assets"
      description="Company-owned equipment and inventory — register, edit, retire"
      icon={<Package className="h-10 w-10" />}
      queryKey={['assets']}
      fetcher={() => assetService.list()}
      rowHref={a => `/assets/${a.id}`}
      filters={{ status: ['AVAILABLE', 'ASSIGNED', 'IN_REPAIR', 'RETIRED', 'LOST', 'STOLEN'] }}
      columns={[
        { key: 'assetTag', label: 'Tag' },
        { key: 'name', label: 'Name' },
        { key: 'categoryName', label: 'Category' },
        { key: 'assignedToEmployeeName', label: 'Assigned to' },
        { key: 'status', label: 'Status', render: a => <Badge>{String(a.status)}</Badge> },
        { key: 'warrantyExpiry', label: 'Warranty', render: a => a.warrantyExpiry ? formatDate(String(a.warrantyExpiry)) : '—' },
      ]}
      formFields={[
        { name: 'assetTag', label: 'Asset tag', type: 'text', required: true },
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'serialNumber', label: 'Serial number', type: 'text' },
        { name: 'model', label: 'Model', type: 'text' },
        { name: 'manufacturer', label: 'Manufacturer', type: 'text' },
        { name: 'purchaseDate', label: 'Purchase date', type: 'date' },
        { name: 'purchaseCost', label: 'Purchase cost', type: 'currency' },
        { name: 'warrantyExpiry', label: 'Warranty expiry', type: 'date' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      createTitle="Register new asset"
      onCreate={v => assetService.create(v as Partial<Asset>)}
      onUpdate={(id, v) => assetService.update(id, v as Partial<Asset>)}
      rowActions={a => [
        { label: 'Retire', icon: <Trash2 className="h-3.5 w-3.5" />, show: a.status !== 'RETIRED', destructive: true, confirm: 'Retire this asset?', run: () => Catalog.assets.remove(a.id) },
      ]}
    />
  )
}
