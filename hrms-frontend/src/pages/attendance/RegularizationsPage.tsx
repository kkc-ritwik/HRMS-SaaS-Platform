import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Clock, Plus, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { attendanceService, type Regularization } from '@/services/attendanceService'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

function rows<T = Regularization>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function RegularizationsPage() {
  const qc = useQueryClient()
  const [tab, setTab] = useState<'mine' | 'pending'>('mine')
  const [creating, setCreating] = useState(false)

  const mineQ = useQuery({ queryKey: ['regularizations', 'mine'], queryFn: () => attendanceService.myRegularizations(), enabled: tab === 'mine' })
  const pendingQ = useQuery({ queryKey: ['regularizations', 'pending'], queryFn: () => attendanceService.pendingRegularizations(), enabled: tab === 'pending' })

  const invalidate = () => qc.invalidateQueries({ queryKey: ['regularizations'] })
  const request = useMutation({ mutationFn: (v: Record<string, unknown>) => attendanceService.requestRegularization(v), onSuccess: () => { toast.success('Submitted'); invalidate(); setCreating(false) } })
  const approve = useMutation({ mutationFn: (id: string) => attendanceService.approveRegularization(id), onSuccess: () => { toast.success('Approved'); invalidate() } })
  const reject = useMutation({ mutationFn: (id: string) => attendanceService.rejectRegularization(id, 'Rejected'), onSuccess: () => { toast.success('Rejected'); invalidate() } })

  const data = tab === 'mine' ? mineQ.data : pendingQ.data
  const loading = tab === 'mine' ? mineQ.isLoading : pendingQ.isLoading

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Tabs value={tab} onValueChange={v => setTab(v as 'mine' | 'pending')}>
          <TabsList>
            <TabsTrigger value="mine">My Requests</TabsTrigger>
            <TabsTrigger value="pending">Pending Approval</TabsTrigger>
          </TabsList>
        </Tabs>
        <Button size="sm" onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> Request Regularization</Button>
      </div>

      <DataList<Regularization & Record<string, unknown>>
        title="Attendance Regularizations"
        description="Correct missed punches and attendance discrepancies"
        data={rows(data)} isLoading={loading}
        emptyIcon={<Clock className="h-10 w-10" />} emptyTitle="No regularization requests"
        columns={[
          { key: 'attendanceDate', label: 'Date', render: r => r.attendanceDate ? formatDate(String(r.attendanceDate)) : '—' },
          { key: 'employeeId', label: 'Employee' },
          { key: 'reason', label: 'Reason' },
          { key: 'status', label: 'Status', render: r => <Badge variant={r.status === 'REJECTED' ? 'destructive' : r.status === 'APPROVED' ? 'success' : 'warning'}>{String(r.status)}</Badge> },
          { key: 'id', label: '', align: 'right', sortable: false, render: r => tab === 'pending' && r.status === 'PENDING' ? (
            <div className="flex justify-end gap-1">
              <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-green-600" onClick={() => approve.mutate(String(r.id))}><Check className="h-4 w-4" /></Button>
              <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-red-500" onClick={() => reject.mutate(String(r.id))}><X className="h-4 w-4" /></Button>
            </div>
          ) : null },
        ]}
      />

      <FormDialog
        open={creating} onOpenChange={setCreating} title="Request regularization" submitLabel="Submit"
        fields={[
          { name: 'attendanceDate', label: 'Date', type: 'date', required: true },
          { name: 'requestedCheckIn', label: 'Correct check-in', type: 'time' },
          { name: 'requestedCheckOut', label: 'Correct check-out', type: 'time' },
          { name: 'reason', label: 'Reason', type: 'textarea', required: true, span: 2 },
        ]}
        onSubmit={v => request.mutateAsync(v)}
      />
    </div>
  )
}
