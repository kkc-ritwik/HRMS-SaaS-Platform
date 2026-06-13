import { CalendarRange } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { Catalog } from '@/services/catalog'

interface Policy extends Record<string, unknown> { id: string; name: string; leaveTypeName?: string; accrualRate?: number; carryForwardLimit?: number; active?: boolean }

export function LeavePoliciesPage() {
  return (
    <ResourcePage<Policy>
      title="Leave Policies"
      description="Accrual rules, carry-forward and encashment per leave type"
      icon={<CalendarRange className="h-10 w-10" />}
      queryKey={['leave-policies']}
      fetcher={() => Catalog.leaves.policies.list()}
      columns={[
        { key: 'name', label: 'Policy' },
        { key: 'leaveTypeName', label: 'Leave type' },
        { key: 'accrualRate', label: 'Accrual / month', align: 'right' },
        { key: 'carryForwardLimit', label: 'Carry-forward cap', align: 'right' },
        { key: 'active', label: 'Active', render: p => <Badge variant={p.active === false ? 'secondary' : 'success'}>{p.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Policy name', type: 'text', required: true, span: 2 },
        { name: 'leaveTypeId', label: 'Leave type ID', type: 'text' },
        { name: 'accrualRate', label: 'Accrual per month', type: 'number' },
        { name: 'maxBalance', label: 'Max balance', type: 'number' },
        { name: 'carryForwardLimit', label: 'Carry-forward cap', type: 'number' },
        { name: 'encashable', label: 'Encashable', type: 'switch' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => Catalog.leaves.policies.create(v)}
      onUpdate={(id, v) => Catalog.leaves.policies.update(id, v)}
      onDelete={id => Catalog.leaves.policies.delete(id)}
    />
  )
}
