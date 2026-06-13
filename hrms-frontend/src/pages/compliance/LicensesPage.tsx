import { BadgeCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { licenseService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface License extends Record<string, unknown> { id: string; name?: string; licenseType?: string; licenseNumber?: string; issuingAuthority?: string; issueDate?: string; expiryDate?: string; status: string }

export function LicensesPage() {
  return (
    <ResourcePage<License>
      title="Licenses & Registrations"
      description="Corporate licenses with renewal alerts — create, edit, delete"
      icon={<BadgeCheck className="h-10 w-10" />}
      queryKey={['licenses']}
      fetcher={() => licenseService.list()}
      filters={{ status: ['ACTIVE', 'EXPIRING', 'EXPIRED', 'RENEWED'] }}
      columns={[
        { key: 'name', label: 'License', render: l => String(l.name ?? l.licenseType ?? '—') },
        { key: 'licenseNumber', label: 'Number' },
        { key: 'issuingAuthority', label: 'Authority' },
        { key: 'issueDate', label: 'Issued', render: l => l.issueDate ? formatDate(String(l.issueDate)) : '—' },
        { key: 'expiryDate', label: 'Expires', render: l => l.expiryDate ? formatDate(String(l.expiryDate)) : '—' },
        { key: 'status', label: 'Status', render: l => <Badge variant={l.status === 'EXPIRED' ? 'destructive' : l.status === 'EXPIRING' ? 'warning' : 'success'}>{String(l.status)}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'License name', type: 'text', required: true, span: 2 },
        { name: 'licenseNumber', label: 'License number', type: 'text', required: true },
        { name: 'issuingAuthority', label: 'Issuing authority', type: 'text' },
        { name: 'issueDate', label: 'Issue date', type: 'date' },
        { name: 'expiryDate', label: 'Expiry date', type: 'date', required: true },
        { name: 'renewalReminderDays', label: 'Remind before (days)', type: 'number', defaultValue: 30 },
      ]}
      onCreate={v => Catalog.compliance.licenses.create(v)}
      onUpdate={(id, v) => Catalog.compliance.licenses.update(id, v)}
      onDelete={id => Catalog.compliance.licenses.delete(id)}
    />
  )
}
