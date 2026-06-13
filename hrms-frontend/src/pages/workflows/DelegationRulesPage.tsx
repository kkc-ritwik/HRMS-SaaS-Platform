import { GitBranch, PowerOff } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { delegationRuleService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

interface Rule extends Record<string, unknown> { id: string; delegatorId?: string; delegateId?: string; fromDate?: string; toDate?: string; active?: boolean }

export function DelegationRulesPage() {
  return (
    <ResourcePage<Rule>
      title="Delegation Rules"
      description="Delegate approvals to another user for a date range"
      icon={<GitBranch className="h-10 w-10" />}
      queryKey={['delegation-rules']}
      fetcher={() => delegationRuleService.list() as Promise<unknown>}
      columns={[
        { key: 'delegatorId', label: 'Delegator' },
        { key: 'delegateId', label: 'Delegate' },
        { key: 'fromDate', label: 'From', render: r => r.fromDate ? formatDate(String(r.fromDate)) : '—' },
        { key: 'toDate', label: 'To', render: r => r.toDate ? formatDate(String(r.toDate)) : '—' },
        { key: 'active', label: 'Active', render: r => <Badge variant={r.active === false ? 'secondary' : 'success'}>{r.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'delegatorId', label: 'Delegator employee ID', type: 'text', required: true },
        { name: 'delegateId', label: 'Delegate employee ID', type: 'text', required: true },
        { name: 'fromDate', label: 'From date', type: 'date', required: true },
        { name: 'toDate', label: 'To date', type: 'date', required: true },
        { name: 'entityType', label: 'Scope (blank = all)', type: 'text' },
      ]}
      onCreate={v => delegationRuleService.create(v)}
      onUpdate={(id, v) => Catalog.workflows.delegations.update(id, v)}
      onDelete={id => Catalog.workflows.delegations.delete(id)}
      rowActions={r => [
        { label: 'Deactivate', icon: <PowerOff className="h-3.5 w-3.5" />, show: r.active !== false, run: () => Catalog.workflows.delegations.deactivate(r.id) },
      ]}
    />
  )
}
