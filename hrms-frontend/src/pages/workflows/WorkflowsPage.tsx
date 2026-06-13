import { GitBranch, Power, PowerOff } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { workflowDefinitionService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface WorkflowDef extends Record<string, unknown> { id: string; name: string; module?: string; entityType?: string; version?: number; status: string; active?: boolean; createdAt?: string }

export function WorkflowsPage() {
  return (
    <ResourcePage<WorkflowDef>
      title="Workflow Definitions"
      description="Approval flows + automations — create, edit, activate/deactivate"
      icon={<GitBranch className="h-10 w-10" />}
      queryKey={['workflow-defs']}
      fetcher={() => workflowDefinitionService.list()}
      columns={[
        { key: 'name', label: 'Name' },
        { key: 'module', label: 'Module', render: w => String(w.module ?? w.entityType ?? '—') },
        { key: 'version', label: 'v', align: 'right' },
        { key: 'status', label: 'Status', render: w => <Badge variant={w.active === false || w.status === 'INACTIVE' ? 'secondary' : 'success'}>{String(w.status)}</Badge> },
        { key: 'createdAt', label: 'Created', render: w => w.createdAt ? formatDate(String(w.createdAt)) : '—' },
      ]}
      formFields={[
        { name: 'name', label: 'Workflow name', type: 'text', required: true, span: 2 },
        { name: 'entityType', label: 'Entity type', type: 'select', required: true, options: [
          { value: 'LEAVE', label: 'Leave' }, { value: 'EXPENSE', label: 'Expense' }, { value: 'TRAVEL', label: 'Travel' },
          { value: 'TIMESHEET', label: 'Timesheet' }, { value: 'ASSET_REQUEST', label: 'Asset request' },
          { value: 'SEPARATION', label: 'Separation' }, { value: 'REQUISITION', label: 'Requisition' },
        ] },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => workflowDefinitionService.create(v)}
      onUpdate={(id, v) => Catalog.workflows.definitions.update(id, v)}
      onDelete={id => Catalog.workflows.definitions.delete(id)}
      rowActions={w => [
        { label: 'Activate', icon: <Power className="h-3.5 w-3.5" />, show: w.active === false || w.status === 'INACTIVE' || w.status === 'DRAFT', run: () => Catalog.workflows.definitions.activate(w.id) },
        { label: 'Deactivate', icon: <PowerOff className="h-3.5 w-3.5" />, show: w.active !== false && w.status === 'ACTIVE', run: () => Catalog.workflows.definitions.deactivate(w.id) },
      ]}
    />
  )
}
