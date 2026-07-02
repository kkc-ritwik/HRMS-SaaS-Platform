import { useState } from 'react'
import { Trophy, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ResourcePage } from '@/components/ui/resource-page'
import { awardService } from '@/services/extendedServices'
import { useAuthStore } from '@/store/authStore'

interface Award extends Record<string, unknown> {
  id: string; awardType: string; nomineeId?: string; title: string;
  status: string; period?: string; monetaryValue?: number; currency?: string; points?: number
}
type View = 'all' | 'mine'

export function AwardsPage() {
  const [view, setView] = useState<View>('all')
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id || ''
  const fetcher = () => view === 'mine' ? awardService.forNominee(employeeId) : awardService.list()

  return (
    <>
    <Tabs value={view} onValueChange={v => setView(v as View)} className="mb-2">
      <TabsList>
        <TabsTrigger value="all">All awards</TabsTrigger>
        <TabsTrigger value="mine">Received by me</TabsTrigger>
      </TabsList>
    </Tabs>
    <ResourcePage<Award>
      key={view}
      title="Awards"
      description="Recognition awards — nominate, approve, grant"
      icon={<Trophy className="h-10 w-10" />}
      queryKey={['awards', view]}
      fetcher={fetcher}
      filters={{
        awardType: ['SPOT_AWARD', 'EMPLOYEE_OF_THE_MONTH', 'EMPLOYEE_OF_THE_YEAR', 'LONG_SERVICE', 'TEAM_AWARD', 'INNOVATION', 'LEADERSHIP', 'CUSTOMER_HERO', 'VALUES_CHAMPION', 'OTHER'],
        status: ['NOMINATED', 'APPROVED', 'REJECTED', 'AWARDED'],
      }}
      columns={[
        { key: 'title', label: 'Title' },
        { key: 'awardType', label: 'Type', render: a => <Badge>{String(a.awardType).replace(/_/g, ' ')}</Badge> },
        { key: 'nomineeId', label: 'Nominee' },
        { key: 'monetaryValue', label: 'Value', render: a => a.monetaryValue ? `${a.currency || '₹'} ${Number(a.monetaryValue).toLocaleString()}` : '—' },
        { key: 'status', label: 'Status', render: a => <Badge variant={a.status === 'REJECTED' ? 'destructive' : a.status === 'AWARDED' ? 'success' : 'warning'}>{String(a.status)}</Badge> },
      ]}
      formFields={[
        { name: 'title', label: 'Award title', type: 'text', required: true, span: 2 },
        { name: 'awardType', label: 'Award type', type: 'select', required: true, options: [
          { value: 'SPOT_AWARD', label: 'Spot award' }, { value: 'EMPLOYEE_OF_THE_MONTH', label: 'Employee of the month' },
          { value: 'EMPLOYEE_OF_THE_YEAR', label: 'Employee of the year' }, { value: 'LONG_SERVICE', label: 'Long service' },
          { value: 'TEAM_AWARD', label: 'Team award' }, { value: 'INNOVATION', label: 'Innovation' },
          { value: 'LEADERSHIP', label: 'Leadership' }, { value: 'CUSTOMER_HERO', label: 'Customer hero' },
          { value: 'VALUES_CHAMPION', label: 'Values champion' }, { value: 'OTHER', label: 'Other' },
        ] },
        { name: 'nomineeId', label: 'Nominee (employee ID)', type: 'text', required: true },
        { name: 'period', label: 'Period', type: 'text', placeholder: 'e.g. 2026-Q2' },
        { name: 'monetaryValue', label: 'Monetary value', type: 'currency' },
        { name: 'currency', label: 'Currency', type: 'text', defaultValue: 'INR' },
        { name: 'points', label: 'Points', type: 'number' },
        { name: 'reason', label: 'Citation / reason', type: 'textarea', span: 2 },
      ]}
      createTitle="Nominate for award"
      onCreate={v => awardService.nominate(v)}
      onDelete={id => awardService.remove(id)}
      rowActions={a => [
        { label: 'Approve / grant', icon: <Check className="h-3.5 w-3.5" />, show: a.status === 'NOMINATED', run: () => awardService.approve(a.id) },
        { label: 'Reject', icon: <X className="h-3.5 w-3.5" />, show: a.status === 'NOMINATED', destructive: true, confirm: 'Reject this nomination?', run: () => awardService.reject(a.id) },
      ]}
    />
    </>
  )
}
