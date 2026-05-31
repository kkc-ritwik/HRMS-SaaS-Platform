import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Clock } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { PageHeader } from '@/components/ui/page-header'
import { timesheetService, type TimesheetEntry } from '@/services/timesheetService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'

function startOfWeek(d = new Date()): Date {
  const date = new Date(d)
  const day = (date.getDay() + 6) % 7 // Monday = 0
  date.setDate(date.getDate() - day)
  date.setHours(0, 0, 0, 0)
  return date
}
const iso = (d: Date) => d.toISOString().slice(0, 10)

const DAYS = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']

export function TimesheetPage() {
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  const weekStart = startOfWeek()
  const weekEnd = new Date(weekStart); weekEnd.setDate(weekStart.getDate() + 6)

  const { data, isLoading } = useQuery({
    queryKey: ['timesheet', 'entries', empId, iso(weekStart)],
    queryFn: () => timesheetService.entries(empId, iso(weekStart), iso(weekEnd)),
    enabled: !!empId,
  })

  const submit = useMutation({
    mutationFn: () => timesheetService.submitWeek(empId, iso(weekStart)),
    onSuccess: () => { toast.success('Week submitted'); qc.invalidateQueries({ queryKey: ['timesheet'] }) },
  })

  const entries = (data as TimesheetEntry[]) || []
  const hoursByDay = DAYS.map((_, i) => {
    const day = new Date(weekStart); day.setDate(weekStart.getDate() + i)
    const key = iso(day)
    return entries.filter(e => e.workDate === key).reduce((s, e) => s + (e.hours || 0), 0)
  })
  const total = entries.reduce((s, e) => s + (e.hours || 0), 0)
  const billable = entries.filter(e => e.isBillable).reduce((s, e) => s + (e.hours || 0), 0)

  if (isLoading) return <Skeleton className="h-64" />

  return (
    <div className="space-y-6">
      <PageHeader title="My Timesheet" description="Log hours per project per day" action={
        <Button size="sm" onClick={() => submit.mutate()} disabled={submit.isPending || entries.length === 0}>Submit week</Button>
      } />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2"><Clock className="h-5 w-5" /> Week of {iso(weekStart)}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-7 gap-2 text-center text-xs">
            {DAYS.map((d, i) => (
              <div key={d}>
                <div className="font-medium text-slate-500">{d}</div>
                <div className="mt-1 h-20 rounded border border-dashed border-slate-200 flex items-center justify-center text-slate-600 font-medium">
                  {hoursByDay[i]}h
                </div>
              </div>
            ))}
          </div>
          <p className="text-xs text-slate-500 mt-4">Total: {total}h · Billable: {billable}h</p>
        </CardContent>
      </Card>
    </div>
  )
}
