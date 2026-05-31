import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Building2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { vendorService } from '@/services/extendedServices'

interface Vendor { id: string; vendorCode: string; legalName: string; category: string; primaryContactEmail?: string; active: boolean; rating?: number }

export function VendorsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['vendors'], queryFn: () => vendorService.list() })
  const items: Vendor[] = (data as { content?: Vendor[] } | undefined)?.content || (Array.isArray(data) ? data as Vendor[] : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => vendorService.create(v),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['vendors'] }),
  })

  return (
    <>
      <DataList<Vendor>
        title="Vendors" description="Master list of all external suppliers and partners"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Vendor</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Building2 className="h-10 w-10" />} emptyTitle="No vendors"
        filters={{ category: ['ASSET_SUPPLIER', 'RECRUITMENT_AGENCY', 'IT_VENDOR', 'TRAINING_PARTNER', 'AMC_PROVIDER', 'OTHER'] }}
        columns={[
          { key: 'vendorCode', label: 'Code' },
          { key: 'legalName', label: 'Legal name' },
          { key: 'category', label: 'Category', render: v => <Badge>{v.category}</Badge> },
          { key: 'primaryContactEmail', label: 'Contact' },
          { key: 'rating', label: 'Rating', render: v => v.rating ? `${v.rating}/5` : '—' },
          { key: 'active', label: 'Active', render: v => v.active ? '✓' : '—' },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Add vendor"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'vendorCode', label: 'Vendor code', type: 'text', required: true },
          { name: 'legalName', label: 'Legal name', type: 'text', required: true },
          { name: 'displayName', label: 'Display name', type: 'text' },
          { name: 'category', label: 'Category', type: 'select', required: true, options: [
            { value: 'ASSET_SUPPLIER', label: 'Asset supplier' }, { value: 'RECRUITMENT_AGENCY', label: 'Recruitment agency' },
            { value: 'AMC_PROVIDER', label: 'AMC provider' }, { value: 'TRAINING_PARTNER', label: 'Training partner' },
            { value: 'IT_VENDOR', label: 'IT vendor' }, { value: 'INSURANCE_BROKER', label: 'Insurance broker' },
            { value: 'CATERING', label: 'Catering' }, { value: 'SECURITY', label: 'Security' },
            { value: 'CONSULTANT', label: 'Consultant' }, { value: 'OTHER', label: 'Other' },
          ] },
          { name: 'primaryContactName', label: 'Contact name', type: 'text' },
          { name: 'primaryContactEmail', label: 'Contact email', type: 'email' },
          { name: 'primaryContactPhone', label: 'Contact phone', type: 'tel' },
          { name: 'gstin', label: 'GSTIN', type: 'text' },
          { name: 'panNumber', label: 'PAN', type: 'text' },
          { name: 'tdsRatePercent', label: 'TDS %', type: 'number' },
          { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
        ]}
      />
    </>
  )
}
