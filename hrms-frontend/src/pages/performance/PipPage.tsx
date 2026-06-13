import { AlertTriangle, Play } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { pipService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Pip extends Record<string, unknown> { id: string; employeeId?: string; employeeName?: string; managerName?: string; startDate?: string; endDate?: string; status: string }

export function PipPage() {
  return (
    <ResourcePage<Pip>
      title="Performance Improvement Plans"
      description="Active PIPs across the org — create, edit, activate"
      icon={<AlertTriangle className="h-10 w-10" />}
      queryKey={['pip']}
      fetcher={() => pipService.list()}
      filters={{ status: ['DRAFT', 'ACTIVE', 'COMPLETED', 'EXTENDED', 'TERMINATED'] }}
      columns={[
        { key: 'employeeName', label: 'Employee', render: p => String(p.employeeName ?? p.employeeId ?? '—') },
        { key: 'managerName', label: 'Manager' },
        { key: 'startDate', label: 'Start', render: p => p.startDate ? formatDate(String(p.startDate)) : '—' },
        { key: 'endDate', label: 'End', render: p => p.endDate ? formatDate(String(p.endDate)) : '—' },
        { key: 'status', label: 'Status', render: p => <Badge variant={p.status === 'TERMINATED' ? 'destructive' : p.status === 'COMPLETED' ? 'success' : 'warning'}>{String(p.status)}</Badge> },
      ]}
      formFields={[
        { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
        { name: 'managerId', label: 'Manager ID', type: 'text' },
        { name: 'startDate', label: 'Start date', type: 'date', required: true },
        { name: 'endDate', label: 'Target end date', type: 'date', required: true },
        { name: 'reason', label: 'Reason / concern areas', type: 'textarea', span: 2, required: true },
        { name: 'expectedOutcome', label: 'Expected outcome', type: 'textarea', span: 2 },
      ]}
      onCreate={v => pipService.create(v)}
      onUpdate={(id, v) => Catalog.pipPlans.update(id, v)}
      rowActions={p => [
        { label: 'Activate', icon: <Play className="h-3.5 w-3.5" />, show: p.status === 'DRAFT', run: () => Catalog.pipPlans.activate(p.id) },
      ]}
    />
  )
}
