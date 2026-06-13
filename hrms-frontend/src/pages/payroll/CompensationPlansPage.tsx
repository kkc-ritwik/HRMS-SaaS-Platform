import { Wallet, Play } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { compensationService, type CompensationPlan } from '@/services/compensationService'
import { Catalog } from '@/services/catalog'

export function CompensationPlansPage() {
  return (
    <ResourcePage<CompensationPlan & Record<string, unknown>>
      title="Compensation Plans"
      description="Annual comp plans — base, bonus, equity per employee"
      icon={<Wallet className="h-10 w-10" />}
      queryKey={['compensation-plans']}
      fetcher={() => compensationService.listPlans()}
      filters={{ status: ['DRAFT', 'ACTIVE', 'SUPERSEDED'] }}
      columns={[
        { key: 'employeeId', label: 'Employee' },
        { key: 'effectiveFrom', label: 'Effective' },
        { key: 'baseSalary', label: 'Base', align: 'right', render: p => p.baseSalary ? `₹${Number(p.baseSalary).toLocaleString()}` : '—' },
        { key: 'totalCtc', label: 'CTC', align: 'right', render: p => p.totalCtc ? `₹${Number(p.totalCtc).toLocaleString()}` : '—' },
        { key: 'status', label: 'Status', render: p => <Badge variant={p.status === 'ACTIVE' ? 'success' : 'secondary'}>{String(p.status)}</Badge> },
      ]}
      formFields={[
        { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
        { name: 'effectiveFrom', label: 'Effective from', type: 'date', required: true },
        { name: 'baseSalary', label: 'Base salary', type: 'currency', required: true },
        { name: 'bonusTarget', label: 'Bonus target', type: 'currency' },
        { name: 'equityValue', label: 'Equity value', type: 'currency' },
        { name: 'notes', label: 'Notes', type: 'textarea', span: 2 },
      ]}
      onCreate={v => compensationService.createPlan(v)}
      onUpdate={(id, v) => Catalog.compensation.plans.update(id, v)}
      onDelete={id => Catalog.compensation.plans.delete(id)}
      rowActions={p => [
        { label: 'Activate', icon: <Play className="h-3.5 w-3.5" />, show: p.status === 'DRAFT', run: () => Catalog.compensation.plans.update(p.id, { status: 'ACTIVE' }) },
      ]}
    />
  )
}
