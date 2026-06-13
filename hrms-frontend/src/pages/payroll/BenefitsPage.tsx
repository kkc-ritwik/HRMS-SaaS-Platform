import { Heart } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'
import type { Benefit } from '@/services/compensationService'

export function BenefitsPage() {
  return (
    <ResourcePage<Benefit & Record<string, unknown>>
      title="Benefits"
      description="Health, life, accident and other employee benefits"
      icon={<Heart className="h-10 w-10" />}
      queryKey={['benefits']}
      fetcher={() => Catalog.compensation.benefits.list()}
      filters={{ category: ['HEALTH', 'LIFE', 'ACCIDENT', 'RETIREMENT', 'WELLNESS', 'OTHER'] }}
      columns={[
        { key: 'code', label: 'Code' },
        { key: 'name', label: 'Name' },
        { key: 'category', label: 'Category', render: b => <Badge>{String(b.category ?? '')}</Badge> },
        { key: 'premiumAmount', label: 'Premium', align: 'right', render: b => b.premiumAmount ? `₹${Number(b.premiumAmount).toLocaleString()}` : '—' },
        { key: 'premiumPaidBy', label: 'Paid by', render: b => <Badge>{String(b.premiumPaidBy ?? '')}</Badge> },
        { key: 'active', label: 'Active', render: b => <Badge variant={b.active === false ? 'secondary' : 'success'}>{b.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'code', label: 'Code', type: 'text', required: true },
        { name: 'name', label: 'Name', type: 'text', required: true },
        { name: 'category', label: 'Category', type: 'select', required: true, options: [
          { value: 'HEALTH', label: 'Health' }, { value: 'LIFE', label: 'Life' }, { value: 'ACCIDENT', label: 'Accident' },
          { value: 'RETIREMENT', label: 'Retirement' }, { value: 'WELLNESS', label: 'Wellness' }, { value: 'OTHER', label: 'Other' },
        ] },
        { name: 'premiumAmount', label: 'Premium amount', type: 'currency' },
        { name: 'premiumPaidBy', label: 'Premium paid by', type: 'select', options: [
          { value: 'EMPLOYER', label: 'Employer' }, { value: 'EMPLOYEE', label: 'Employee' }, { value: 'SHARED', label: 'Shared' },
        ] },
        { name: 'coverageAmount', label: 'Coverage amount', type: 'currency' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => Catalog.compensation.benefits.create(v)}
      onUpdate={(id, v) => Catalog.compensation.benefits.update(id, v)}
      onDelete={id => Catalog.compensation.benefits.delete(id)}
    />
  )
}
