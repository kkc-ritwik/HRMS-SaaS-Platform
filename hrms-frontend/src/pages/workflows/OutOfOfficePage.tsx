import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CalendarOff } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { workflowService, type OutOfOffice } from '@/services/workflowService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

export function OutOfOfficePage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const userId = user?.id || ''
  const [form, setForm] = useState({ delegateId: '', startDate: '', endDate: '', reason: '' })
  const { data, isLoading } = useQuery({
    queryKey: ['ooo', userId],
    queryFn: () => workflowService.myOOO(userId),
    enabled: !!userId,
  })
  const create = useMutation({
    mutationFn: () => workflowService.scheduleOOO({ userId, ...form }),
    onSuccess: () => { toast.success('Delegation scheduled'); qc.invalidateQueries({ queryKey: ['ooo'] }) },
  })

  return (
    <div className="space-y-6 max-w-3xl">
      <PageHeader title="Out of Office" description="Delegate approvals while you're away" />

      <Card>
        <CardHeader><CardTitle>Schedule a new delegation</CardTitle></CardHeader>
        <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div><Label>Delegate (employee ID)</Label><Input value={form.delegateId} onChange={e => setForm({ ...form, delegateId: e.target.value })} /></div>
          <div><Label>Reason</Label><Input value={form.reason} onChange={e => setForm({ ...form, reason: e.target.value })} placeholder="vacation, conference..." /></div>
          <div><Label>Start</Label><Input type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} /></div>
          <div><Label>End</Label><Input type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} /></div>
          <Button className="md:col-span-2" onClick={() => create.mutate()} disabled={!form.delegateId || !form.startDate || !form.endDate}>Schedule</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2"><CalendarOff className="h-5 w-5" /> Your delegations</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? <Skeleton className="h-32" /> : (data || []).length === 0 ? (
            <p className="text-sm text-slate-500">None scheduled</p>
          ) : (
            <ul className="space-y-2">
              {(data as OutOfOffice[]).map(o => (
                <li key={o.id} className="flex items-center justify-between p-3 rounded border">
                  <div>
                    <p className="text-sm font-medium">{o.delegateId}</p>
                    <p className="text-xs text-slate-500">{o.startDate} → {o.endDate}</p>
                  </div>
                  <Button size="sm" variant="outline" onClick={() => workflowService.cancelOOO(o.id).then(() => qc.invalidateQueries({ queryKey: ['ooo'] }))}>Cancel</Button>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
