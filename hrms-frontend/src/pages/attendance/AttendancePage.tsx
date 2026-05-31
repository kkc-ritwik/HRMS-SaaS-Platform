import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { format, startOfMonth, eachDayOfInterval, endOfMonth, isWeekend, isToday, parseISO } from 'date-fns'
import { Clock, LogIn, LogOut, ChevronLeft, ChevronRight, CheckCircle } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { StatCard } from '@/components/ui/stat-card'
import { cn } from '@/lib/utils'
import { attendanceService, type AttendanceRecord, type AttendanceStatus } from '@/services/attendanceService'

type CellStatus = 'present' | 'absent' | 'leave' | 'holiday' | 'weekend' | 'half' | 'future' | 'none'

const statusStyles: Record<CellStatus, { bg: string; text: string; dot: string }> = {
  present: { bg: 'bg-green-50 border-green-200', text: 'text-green-700', dot: 'bg-green-500' },
  half: { bg: 'bg-teal-50 border-teal-200', text: 'text-teal-700', dot: 'bg-teal-500' },
  absent: { bg: 'bg-red-50 border-red-200', text: 'text-red-600', dot: 'bg-red-500' },
  leave: { bg: 'bg-purple-50 border-purple-200', text: 'text-purple-600', dot: 'bg-purple-500' },
  holiday: { bg: 'bg-blue-50 border-blue-200', text: 'text-blue-600', dot: 'bg-blue-400' },
  weekend: { bg: 'bg-slate-50 border-slate-200', text: 'text-slate-400', dot: 'bg-slate-300' },
  future: { bg: 'bg-white border-slate-100', text: 'text-slate-300', dot: 'bg-slate-100' },
  none: { bg: 'bg-white border-slate-100', text: 'text-slate-400', dot: 'bg-slate-200' },
}

const STATUS_MAP: Record<AttendanceStatus, CellStatus> = {
  PRESENT: 'present', HALF_DAY: 'half', ABSENT: 'absent',
  ON_LEAVE: 'leave', HOLIDAY: 'holiday', WEEK_OFF: 'weekend',
}

function fmtTime(iso?: string) { return iso ? format(parseISO(iso), 'hh:mm a') : undefined }

export function AttendancePage() {
  const qc = useQueryClient()
  const today = new Date()
  const [viewDate, setViewDate] = useState(today)
  const year = viewDate.getFullYear()
  const month = viewDate.getMonth()

  const { data, isLoading } = useQuery({
    queryKey: ['attendance', 'my-log', year, month + 1],
    queryFn: () => attendanceService.myLog(year, month + 1),
  })

  const records: AttendanceRecord[] = data?.records || []
  const byDate = new Map(records.map(r => [r.attendanceDate, r]))
  const todayKey = format(today, 'yyyy-MM-dd')
  const todayRecord = byDate.get(todayKey)
  const isPunchedIn = !!todayRecord?.firstCheckIn && !todayRecord?.lastCheckOut

  const punch = useMutation({
    mutationFn: () => attendanceService.punch(),
    onSuccess: (res) => {
      toast.success(res?.message || `${res?.type === 'CHECK_OUT' ? 'Punched out' : 'Punched in'} at ${fmtTime(res?.punchTime) || format(new Date(), 'hh:mm a')}`)
      qc.invalidateQueries({ queryKey: ['attendance'] })
    },
    onError: () => toast.error('Punch failed'),
  })

  const stats = {
    present: data?.presentDays ?? 0,
    absent: data?.absentDays ?? 0,
    leave: data?.leaveDays ?? 0,
    workdays: (data?.presentDays ?? 0) + (data?.halfDays ?? 0) + (data?.absentDays ?? 0),
  }

  const days = eachDayOfInterval({ start: startOfMonth(viewDate), end: endOfMonth(viewDate) })
  const cellStatus = (date: Date): CellStatus => {
    const key = format(date, 'yyyy-MM-dd')
    const rec = byDate.get(key)
    if (rec) return STATUS_MAP[rec.status]
    if (date > today) return 'future'
    if (isWeekend(date)) return 'weekend'
    return 'none'
  }

  const prevMonth = () => setViewDate(new Date(year, month - 1))
  const nextMonth = () => setViewDate(new Date(year, month + 1))
  const firstDayOfMonth = startOfMonth(viewDate).getDay()
  const blanks = Array.from({ length: firstDayOfMonth })

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Attendance"
        description="Track your daily attendance"
        breadcrumbs={[{ label: 'My Space' }, { label: 'Attendance' }]}
      />

      <Card>
        <CardContent className="pt-5">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
            <div className="flex-1">
              <p className="text-sm text-slate-500 mb-0.5">Today</p>
              <p className="text-xl font-bold text-slate-900">{format(today, 'EEEE, MMMM d, yyyy')}</p>
              {todayRecord?.firstCheckIn && (
                <p className="text-sm text-slate-500 mt-1">
                  <span className="text-green-600 font-medium">Checked in</span> at {fmtTime(todayRecord.firstCheckIn)}
                  {todayRecord.lastCheckOut && <> · <span className="text-red-500 font-medium">out</span> at {fmtTime(todayRecord.lastCheckOut)}</>}
                </p>
              )}
            </div>
            <div className="flex items-center gap-3">
              <div className="text-center">
                <p className="text-xs text-slate-400 mb-1">Today's hours</p>
                <p className="text-2xl font-mono font-bold text-slate-900">{(todayRecord?.effectiveHours ?? 0).toFixed(1)}</p>
                <p className="text-xs text-slate-400">hrs</p>
              </div>
              <Button
                size="lg"
                disabled={punch.isPending}
                className={cn('min-w-[140px]', isPunchedIn ? 'bg-red-500 hover:bg-red-600' : 'bg-green-600 hover:bg-green-700')}
                onClick={() => punch.mutate()}
              >
                {isPunchedIn ? <><LogOut className="h-5 w-5" /> Punch Out</> : <><LogIn className="h-5 w-5" /> Punch In</>}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard title="Present" value={stats.present} icon={<CheckCircle className="h-5 w-5 text-green-600" />} iconBg="bg-green-100" />
        <StatCard title="Absent" value={stats.absent} icon={<Clock className="h-5 w-5 text-red-500" />} iconBg="bg-red-100" />
        <StatCard title="On Leave" value={stats.leave} icon={<Clock className="h-5 w-5 text-purple-500" />} iconBg="bg-purple-100" />
        <StatCard title="Work Days" value={stats.workdays} icon={<Clock className="h-5 w-5 text-brand-500" />} iconBg="bg-brand-100" />
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>{format(viewDate, 'MMMM yyyy')}</CardTitle>
            <div className="flex items-center gap-1">
              <Button variant="ghost" size="icon-sm" onClick={prevMonth}><ChevronLeft className="h-4 w-4" /></Button>
              <Button variant="ghost" size="sm" onClick={() => setViewDate(today)}>Today</Button>
              <Button variant="ghost" size="icon-sm" onClick={nextMonth}><ChevronRight className="h-4 w-4" /></Button>
            </div>
          </div>
          <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
            {(['present', 'absent', 'leave', 'holiday', 'weekend'] as CellStatus[]).map(s => (
              <span key={s} className="flex items-center gap-1">
                <span className={`h-2 w-2 rounded-full ${statusStyles[s].dot}`} />
                {s.charAt(0).toUpperCase() + s.slice(1)}
              </span>
            ))}
          </div>
        </CardHeader>
        <CardContent className="pt-0">
          {isLoading ? <Skeleton className="h-72" /> : (
            <>
              <div className="grid grid-cols-7 mb-2">
                {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
                  <div key={d} className="text-center text-xs font-semibold text-slate-400 py-2">{d}</div>
                ))}
              </div>
              <div className="grid grid-cols-7 gap-1.5">
                {blanks.map((_, i) => <div key={`blank-${i}`} />)}
                {days.map((date) => {
                  const status = cellStatus(date)
                  const style = statusStyles[status]
                  const todayFlag = isToday(date)
                  const rec = byDate.get(format(date, 'yyyy-MM-dd'))
                  return (
                    <div key={date.toISOString()} className={cn('relative rounded-lg border p-2 min-h-[60px] transition-all', style.bg, todayFlag && 'ring-2 ring-brand-500')}>
                      <div className="flex items-center justify-between mb-1">
                        <span className={cn('text-xs font-bold', todayFlag ? 'text-brand-600' : style.text)}>{format(date, 'd')}</span>
                        <span className={cn('h-1.5 w-1.5 rounded-full', style.dot)} />
                      </div>
                      {rec?.firstCheckIn && (
                        <div className="space-y-0.5">
                          <p className="text-[9px] text-green-600 font-medium leading-none">{fmtTime(rec.firstCheckIn)}</p>
                          {rec.lastCheckOut && <p className="text-[9px] text-red-500 font-medium leading-none">{fmtTime(rec.lastCheckOut)}</p>}
                        </div>
                      )}
                      {status === 'leave' && <p className="text-[9px] text-purple-500 font-medium">Leave</p>}
                      {status === 'holiday' && <p className="text-[9px] text-blue-500 font-medium">Holiday</p>}
                    </div>
                  )
                })}
              </div>
            </>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
