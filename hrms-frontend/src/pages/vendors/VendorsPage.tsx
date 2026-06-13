import { Building2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { vendorService } from '@/services/extendedServices'

interface Vendor extends Record<string, unknown> { id: string; vendorCode: string; legalName: string; category: string; primaryContactEmail?: string; active?: boolean; rating?: number }

export function VendorsPage() {
  return (
    <ResourcePage<Vendor>
      title="Vendors"
      description="Master list of all external suppliers and partners"
      icon={<Building2 className="h-10 w-10" />}
      queryKey={['vendors']}
      fetcher={() => vendorService.list()}
      filters={{ category: ['ASSET_SUPPLIER', 'RECRUITMENT_AGENCY', 'IT_VENDOR', 'TRAINING_PARTNER', 'AMC_PROVIDER', 'OTHER'] }}
      columns={[
        { key: 'vendorCode', label: 'Code' },
        { key: 'legalName', label: 'Legal name' },
        { key: 'category', label: 'Category', render: v => <Badge>{String(v.category)}</Badge> },
        { key: 'primaryContactEmail', label: 'Contact' },
        { key: 'rating', label: 'Rating', render: v => v.rating ? `${v.rating}/5` : '—' },
        { key: 'active', label: 'Active', render: v => <Badge variant={v.active === false ? 'secondary' : 'success'}>{v.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
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
        { name: 'paymentTermsDays', label: 'Payment terms (days)', type: 'number' },
        { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
      ]}
      onCreate={v => vendorService.create(v)}
      onUpdate={(id, v) => vendorService.update(id, v)}
      onDelete={id => vendorService.remove(id)}
    />
  )
}
