import { useState } from 'react'
import { GitBranch, PowerOff } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ResourcePage } from '@/components/ui/resource-page'
import { delegationRuleService } from '@/services/extendedServices'
import { Catalog } from '@/services/catalog'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'

interface Rule extends Record<string, unknown> { id: string; delegatorId?: string; delegateId?: string; fromDate?: string; toDate?: string; active?: boolean }
type View = 'all' | 'delegator' | 'delegate'

export function DelegationRulesPage() {
  const [view, setView] = useState<View>('all')
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id || ''
  const fetcher = () => {
    if (view === 'delegator') return Catalog.workflows.delegations.byDelegator(employeeId)
    if (view === 'delegate') return Catalog.workflows.delegations.byDelegate(employeeId)
    return delegationRuleService.list()
  }

  return (
    <>
      <Tabs value={view} onValueChange={v => setView(v as View)} className="mb-2">
        <TabsList>
          <TabsTrigger value="all">All rules</TabsTrigger>
          <TabsTrigger value="delegator">I delegate</TabsTrigger>
          <TabsTrigger value="delegate">Delegated to me</TabsTrigger>
        </TabsList>
      </Tabs>
      <ResourcePage<Rule>
        key={view}
        title="Delegation Rules"
        description="Delegate approvals to another user for a date range"
        icon={<GitBranch className="h-10 w-10" />}
        queryKey={['delegation-rules', view]}
        fetcher={fetcher as () => Promise<unknown>}
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
    </>
  )
}
