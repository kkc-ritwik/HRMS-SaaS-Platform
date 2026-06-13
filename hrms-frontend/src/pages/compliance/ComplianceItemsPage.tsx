import { CheckCircle2, ShieldCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { complianceItemService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface ComplianceItem extends Record<string, unknown> { id: string; title: string; category?: string; dueDate?: string; status: string; assignee?: string; ownerName?: string }

export function ComplianceItemsPage() {
  return (
    <ResourcePage<ComplianceItem>
      title="Compliance Checklist"
      description="Statutory + internal tasks with deadlines — create, edit, mark compliant"
      icon={<ShieldCheck className="h-10 w-10" />}
      queryKey={['compliance-items']}
      fetcher={() => complianceItemService.list()}
      filters={{ status: ['PENDING', 'IN_PROGRESS', 'COMPLIANT', 'NON_COMPLIANT', 'OVERDUE'] }}
      columns={[
        { key: 'title', label: 'Item' },
        { key: 'category', label: 'Category' },
        { key: 'dueDate', label: 'Due', render: c => c.dueDate ? formatDate(String(c.dueDate)) : '—' },
        { key: 'ownerName', label: 'Owner', render: c => String(c.ownerName ?? c.assignee ?? '—') },
        { key: 'status', label: 'Status', render: c => <Badge variant={c.status === 'NON_COMPLIANT' || c.status === 'OVERDUE' ? 'destructive' : c.status === 'COMPLIANT' ? 'success' : 'warning'}>{String(c.status)}</Badge> },
      ]}
      formFields={[
        { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'select', required: true, options: [
          { value: 'STATUTORY', label: 'Statutory' }, { value: 'TAX', label: 'Tax' }, { value: 'LABOUR', label: 'Labour law' },
          { value: 'POSH', label: 'POSH' }, { value: 'DATA_PRIVACY', label: 'Data privacy' }, { value: 'INTERNAL', label: 'Internal' },
        ] },
        { name: 'ownerId', label: 'Owner employee ID', type: 'text' },
        { name: 'dueDate', label: 'Due date', type: 'date', required: true },
        { name: 'frequency', label: 'Frequency', type: 'select', options: [
          { value: 'ONE_TIME', label: 'One-time' }, { value: 'MONTHLY', label: 'Monthly' },
          { value: 'QUARTERLY', label: 'Quarterly' }, { value: 'ANNUAL', label: 'Annual' },
        ] },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => complianceItemService.create(v)}
      onUpdate={(id, v) => Catalog.compliance.items.update(id, v)}
      onDelete={id => Catalog.compliance.items.delete(id)}
      rowActions={c => [
        { label: 'Mark compliant', icon: <CheckCircle2 className="h-3.5 w-3.5" />, show: c.status !== 'COMPLIANT', run: () => Catalog.compliance.items.markCompliant(c.id, {}) },
      ]}
    />
  )
}
