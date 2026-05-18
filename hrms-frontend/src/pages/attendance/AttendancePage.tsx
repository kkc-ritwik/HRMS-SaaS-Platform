import React, { useState } from 'react'
import { format, startOfMonth, endOfMonth, eachDayOfInterval, isSameDay, isWeekend, isToday } from 'date-fns'
import { Clock, LogIn, LogOut, ChevronLeft, ChevronRight, CheckCircle } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { StatCard } from '@/components/ui/stat-card'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/store/authStore'

type AttendanceStatus = 'present' | 'absent' | 'leave' | 'holiday' | 'weekend' | 'future'

interface DayRecord {
  date: Date
  status: AttendanceStatus
  punchIn?: string
  punchOut?: string
  hours?: number
}

const generateMonthData = (year: number, month: number): DayRecord[] => {
  const start = startOfMonth(new Date(year, month))
  const end = endOfMonth(new Date(year, month))
  const days = eachDayOfInterval({ start, end })

  return days.map(date => {
    if (isWeekend(date)) return { date, status: 'weekend' }
    if (date > new Date()) return { date, status: 'future' }

    const rand = Math.random()
    if (rand < 0.02) return { date, status: 'holiday' }
    if (rand < 0.06) return { date, status: 'leave' }
    if (rand < 0.1) return { date, status: 'absent' }

    const inHour = 9 + Math.floor(Math.random() * 2)
    const inMin = Math.floor(Math.random() * 60)
    const outHour = 17 + Math.floor(Math.random() * 3)
    const outMin = Math.floor(Math.random() * 60)
    const hours = outHour - inHour + (outMin - inMin) / 60

    return {
      date,
      status: 'present',
      punchIn: `${String(inHour).padStart(2, '0')}:${String(inMin).padStart(2, '0')} AM`.replace(' AM', inHour >= 12 ? ' PM' : ' AM'),
      punchOut: `${String(outHour >= 13 ? outHour - 12 : outHour).padStart(2, '0')}:${String(outMin).padStart(2, '0')} PM`,
      hours: Math.round(hours * 10) / 10,
    }
  })
}

const statusStyles: Record<AttendanceStatus, { bg: string; text: string; dot: string }> = {
  present: { bg: 'bg-green-50 border-green-200', text: 'text-green-700', dot: 'bg-green-500' },
  absent: { bg: 'bg-red-50 border-red-200', text: 'text-red-600', dot: 'bg-red-500' },
  leave: { bg: 'bg-purple-50 border-purple-200', text: 'text-purple-600', dot: 'bg-purple-500' },
  holiday: { bg: 'bg-blue-50 border-blue-200', text: 'text-blue-600', dot: 'bg-blue-400' },
  weekend: { bg: 'bg-slate-50 border-slate-200', text: 'text-slate-400', dot: 'bg-slate-300' },
  future: { bg: 'bg-white border-slate-100', text: 'text-slate-300', dot: 'bg-slate-100' },
}

export function AttendancePage() {
  const user = useAuthStore(s => s.user)
  const today = new Date()
  const [viewDate, setViewDate] = useState(today)
  const [isPunchedIn, setIsPunchedIn] = useState(false)
  const [punchInTime, setPunchInTime] = useState<string | null>(null)

  const year = viewDate.getFullYear()
  const month = viewDate.getMonth()
  const monthData = generateMonthData(year, month)

  const stats = {
    present: monthData.filter(d => d.status === 'present').length,
    absent: monthData.filter(d => d.status === 'absent').length,
    leave: monthData.filter(d => d.status === 'leave').length,
    workdays: monthData.filter(d => !['weekend', 'future', 'holiday'].includes(d.status)).length,
  }

  const handlePunch = () => {
    if (!isPunchedIn) {
      const now = format(new Date(), 'hh:mm a')
      setPunchInTime(now)
      setIsPunchedIn(true)
      toast.success(`Punched in at ${now}`)
    } else {
      const now = format(new Date(), 'hh:mm a')
      setIsPunchedIn(false)
      toast.success(`Punched out at ${now}`)
    }
  }

  const prevMonth = () => setViewDate(new Date(year, month - 1))
  const nextMonth = () => setViewDate(new Date(year, month + 1))

  const firstDayOfMonth = startOfMonth(new Date(year, month)).getDay()
  const blanks = Array.from({ length: firstDayOfMonth })

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Attendance"
        description="Track your daily attendance"
        breadcrumbs={[{ label: 'My Space' }, { label: 'Attendance' }]}
      />

      {/* Today's punch */}
      <Card>
        <CardContent className="pt-5">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
            <div className="flex-1">
              <p className="text-sm text-slate-500 mb-0.5">Today</p>
              <p className="text-xl font-bold text-slate-900">{format(today, 'EEEE, MMMM d, yyyy')}</p>
              {punchInTime && (
                <p className="text-sm text-slate-500 mt-1">
                  <span className="text-green-600 font-medium">Punched in</span> at {punchInTime}
                </p>
              )}
            </div>
            <div className="flex items-center gap-3">
              <div className="text-center">
                <p className="text-xs text-slate-400 mb-1">{isPunchedIn ? 'Time Elapsed' : 'Expected'}</p>
                <p className="text-2xl font-mono font-bold text-slate-900">
                  {isPunchedIn ? '00:32' : '09:00'}
                </p>
                <p className="text-xs text-slate-400">{isPunchedIn ? 'hrs' : 'start time'}</p>
              </div>
              <Button
                size="lg"
                className={cn(
                  'min-w-[140px]',
                  isPunchedIn
                    ? 'bg-red-500 hover:bg-red-600'
                    : 'bg-green-600 hover:bg-green-700'
                )}
                onClick={handlePunch}
              >
                {isPunchedIn ? (
                  <>
                    <LogOut className="h-5 w-5" />
                    Punch Out
                  </>
                ) : (
                  <>
                    <LogIn className="h-5 w-5" />
                    Punch In
                  </>
                )}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatCard title="Present" value={stats.present} icon={<CheckCircle className="h-5 w-5 text-green-600" />} iconBg="bg-green-100" />
        <StatCard title="Absent" value={stats.absent} icon={<Clock className="h-5 w-5 text-red-500" />} iconBg="bg-red-100" />
        <StatCard title="On Leave" value={stats.leave} icon={<Clock className="h-5 w-5 text-purple-500" />} iconBg="bg-purple-100" />
        <StatCard title="Work Days" value={stats.workdays} icon={<Clock className="h-5 w-5 text-brand-500" />} iconBg="bg-brand-100" />
      </div>

      {/* Calendar */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>{format(viewDate, 'MMMM yyyy')}</CardTitle>
            <div className="flex items-center gap-1">
              <Button variant="ghost" size="icon-sm" onClick={prevMonth}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="sm" onClick={() => setViewDate(today)}>
                Today
              </Button>
              <Button variant="ghost" size="icon-sm" onClick={nextMonth}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
          {/* Legend */}
          <div className="flex flex-wrap items-center gap-3 text-xs text-slate-500">
            {(['present', 'absent', 'leave', 'holiday', 'weekend'] as AttendanceStatus[]).map(s => (
              <span key={s} className="flex items-center gap-1">
                <span className={`h-2 w-2 rounded-full ${statusStyles[s].dot}`} />
                {s.charAt(0).toUpperCase() + s.slice(1)}
              </span>
            ))}
          </div>
        </CardHeader>
        <CardContent className="pt-0">
          {/* Day labels */}
          <div className="grid grid-cols-7 mb-2">
            {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(d => (
              <div key={d} className="text-center text-xs font-semibold text-slate-400 py-2">{d}</div>
            ))}
          </div>
          {/* Calendar grid */}
          <div className="grid grid-cols-7 gap-1.5">
            {blanks.map((_, i) => <div key={`blank-${i}`} />)}
            {monthData.map((day) => {
              const style = statusStyles[day.status]
              const todayFlag = isToday(day.date)
              return (
                <div
                  key={day.date.toISOString()}
                  className={cn(
                    'relative rounded-lg border p-2 min-h-[60px] cursor-default transition-all',
                    style.bg,
                    todayFlag && 'ring-2 ring-brand-500',
                    day.status !== 'future' && day.status !== 'weekend' && 'hover:shadow-sm',
                  )}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className={cn(
                      'text-xs font-bold',
                      todayFlag ? 'text-brand-600' : style.text,
                    )}>
                      {format(day.date, 'd')}
                    </span>
                    <span className={cn('h-1.5 w-1.5 rounded-full', style.dot)} />
                  </div>
                  {day.punchIn && (
                    <div className="space-y-0.5">
                      <p className="text-[9px] text-green-600 font-medium leading-none">{day.punchIn}</p>
                      {day.punchOut && (
                        <p className="text-[9px] text-red-500 font-medium leading-none">{day.punchOut}</p>
                      )}
                    </div>
                  )}
                  {day.status === 'leave' && <p className="text-[9px] text-purple-500 font-medium">Leave</p>}
                  {day.status === 'holiday' && <p className="text-[9px] text-blue-500 font-medium">Holiday</p>}
                </div>
              )
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
