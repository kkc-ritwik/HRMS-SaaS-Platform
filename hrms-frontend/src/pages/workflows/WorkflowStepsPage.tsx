import { ListOrdered } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'

interface Step extends Record<string, unknown> { id: string; name: string; workflowId?: string; stepOrder?: number; approverType?: string }

export function WorkflowStepsPage() {
  return (
    <ResourcePage<Step>
      title="Workflow Steps"
      description="Approval steps that make up workflow definitions"
      icon={<ListOrdered className="h-10 w-10" />}
      queryKey={['workflow-steps']}
      fetcher={() => Catalog.workflows.steps.list()}
      columns={[
        { key: 'stepOrder', label: '#', align: 'right' },
        { key: 'name', label: 'Step' },
        { key: 'workflowId', label: 'Workflow' },
        { key: 'approverType', label: 'Approver', render: s => s.approverType ? <Badge>{String(s.approverType)}</Badge> : '—' },
      ]}
      formFields={[
        { name: 'workflowId', label: 'Workflow definition ID', type: 'text', required: true, span: 2 },
        { name: 'name', label: 'Step name', type: 'text', required: true },
        { name: 'stepOrder', label: 'Order', type: 'number', required: true },
        { name: 'approverType', label: 'Approver type', type: 'select', options: [
          { value: 'MANAGER', label: 'Reporting manager' }, { value: 'ROLE', label: 'Role' },
          { value: 'USER', label: 'Specific user' }, { value: 'DEPARTMENT_HEAD', label: 'Department head' },
        ] },
        { name: 'approverValue', label: 'Approver value (role/user id)', type: 'text' },
        { name: 'slaHours', label: 'SLA (hours)', type: 'number' },
      ]}
      onCreate={v => Catalog.workflows.steps.create(v)}
      onUpdate={(id, v) => Catalog.workflows.steps.update(id, v)}
      onDelete={id => Catalog.workflows.steps.delete(id)}
    />
  )
}
