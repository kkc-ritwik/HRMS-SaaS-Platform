import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Package } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { assetService, type Asset } from '@/services/assetService'

export function AssetsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['assets'], queryFn: () => assetService.list() })
  const items: Asset[] = (data as { content?: Asset[] } | undefined)?.content || (Array.isArray(data) ? data : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => assetService.create(v as Partial<Asset>),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['assets'] }),
  })

  return (
    <>
      <DataList<Asset>
        title="Assets" description="Company-owned equipment and inventory"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Asset</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Package className="h-10 w-10" />} emptyTitle="No assets registered"
        filters={{ status: ['AVAILABLE', 'ASSIGNED', 'IN_REPAIR', 'RETIRED', 'LOST', 'STOLEN'] }}
        columns={[
          { key: 'assetTag', label: 'Tag' },
          { key: 'name', label: 'Name' },
          { key: 'categoryName', label: 'Category' },
          { key: 'assignedToEmployeeName', label: 'Assigned to' },
          { key: 'status', label: 'Status', render: a => <Badge>{a.status}</Badge> },
          { key: 'warrantyExpiry', label: 'Warranty' },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Register new asset"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
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
      />
    </>
  )
}
