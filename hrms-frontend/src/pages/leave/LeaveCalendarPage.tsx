import { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ChevronLeft, ChevronRight, CalendarDays } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { leaveService } from '@/services/leaveService'
import { holidayService } from '@/services/holidayService'
import { cn } from '@/lib/utils'

interface DayCell { date: Date; isCurrentMonth: boolean; isToday: boolean; events: Array<{ type: 'leave' | 'holiday'; label: string; color: string }> }

function startOfMonth(d: Date) { const x = new Date(d); x.setDate(1); return x }
function addMonths(d: Date, m: number) { const x = new Date(d); x.setMonth(x.getMonth() + m); return x }
function daysOfMonth(anchor: Date): DayCell[] {
  const first = startOfMonth(anchor)
  const offset = (first.getDay() + 6) % 7 // Monday start
  const start = new Date(first); start.setDate(first.getDate() - offset)
  const days: DayCell[] = []
  const today = new Date()
  for (let i = 0; i < 42; i++) {
    const d = new Date(start); d.setDate(start.getDate() + i)
    days.push({
      date: d,
      isCurrentMonth: d.getMonth() === anchor.getMonth(),
      isToday: d.toDateString() === today.toDateString(),
      events: [],
    })
  }
  return days
}

export function LeaveCalendarPage() {
  const [anchor, setAnchor] = useState(new Date())
  const monthLabel = anchor.toLocaleString('default', { month: 'long', year: 'numeric' })
  const year = anchor.getFullYear()

  const leaves = useQuery({ queryKey: ['leaves', 'cal'], queryFn: () => leaveService.listApplications({ status: 'APPROVED' }) })
  const holidays = useQuery({ queryKey: ['holidays', year], queryFn: () => holidayService.list(year) })

  const cells = useMemo(() => {
    const days = daysOfMonth(anchor)
    const leaveItems = ((leaves.data as { data?: Array<{ startDate: string; endDate: string; employeeName?: string }> } | undefined)?.data
      || (Array.isArray(leaves.data) ? leaves.data as Array<{ startDate: string; endDate: string; employeeName?: string }> : []))
    const holidayItems = (holidays.data as Array<{ date: string; name: string; type: string }> | undefined) || []

    days.forEach(c => {
      const iso = c.date.toISOString().slice(0, 10)
      holidayItems.forEach(h => {
        if (h.date === iso) c.events.push({ type: 'holiday', label: h.name, color: 'bg-amber-100 text-amber-800' })
      })
      leaveItems.forEach(l => {
        if (l.startDate <= iso && iso <= l.endDate) {
          c.events.push({ type: 'leave', label: l.employeeName || 'On leave', color: 'bg-violet-100 text-violet-700' })
        }
      })
    })
    return days
  }, [anchor, leaves.data, holidays.data])

  const isLoading = leaves.isLoading || holidays.isLoading

  return (
    <div className="space-y-6">
      <PageHeader
        title="Team Calendar"
        description="Approved leaves + holidays this month"
        action={
          <div className="flex items-center gap-2">
            <Button variant="outline" size="icon" onClick={() => setAnchor(addMonths(anchor, -1))}><ChevronLeft className="h-4 w-4" /></Button>
            <span className="font-medium text-sm min-w-[150px] text-center">{monthLabel}</span>
            <Button variant="outline" size="icon" onClick={() => setAnchor(addMonths(anchor, 1))}><ChevronRight className="h-4 w-4" /></Button>
            <Button variant="outline" size="sm" onClick={() => setAnchor(new Date())}>Today</Button>
          </div>
        }
      />

      {isLoading ? <Skeleton className="h-96" /> : (
        <Card>
          <CardContent className="p-0">
            <div className="grid grid-cols-7 border-b text-xs uppercase text-slate-500 bg-slate-50">
              {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(d => (
                <div key={d} className="p-2 text-center font-medium">{d}</div>
              ))}
            </div>
            <div className="grid grid-cols-7">
              {cells.map((c, i) => (
                <div key={i} className={cn(
                  'min-h-[100px] border-r border-b p-1.5',
                  !c.isCurrentMonth && 'bg-slate-50/50 text-slate-400',
                  c.isToday && 'bg-violet-50',
                )}>
                  <div className={cn('text-xs font-medium mb-1', c.isToday && 'text-violet-700')}>
                    {c.date.getDate()}
                  </div>
                  <div className="space-y-1">
                    {c.events.slice(0, 3).map((e, ix) => (
                      <div key={ix} className={cn('text-[10px] px-1.5 py-0.5 rounded truncate', e.color)} title={e.label}>
                        {e.label}
                      </div>
                    ))}
                    {c.events.length > 3 && <div className="text-[10px] text-slate-400">+{c.events.length - 3}</div>}
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex items-center gap-4 text-xs text-slate-500">
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded bg-violet-100" /> Leave</span>
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded bg-amber-100" /> Holiday</span>
        <span className="flex items-center gap-1"><CalendarDays className="h-3 w-3 text-violet-500" /> Today</span>
      </div>
    </div>
  )
}
