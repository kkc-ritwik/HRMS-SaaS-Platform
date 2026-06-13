/**
 * Manager / Team workspace — pulls /api/v1/me/team, /api/v1/me/dashboard and the
 * leave team-calendar to give a people-manager a single operational view:
 *   team roster · who's out today · pending approvals · team leave calendar.
 */
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  Users, CheckSquare, CalendarDays, UserCircle, TrendingUp, Clock,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { StatCard } from '@/components/ui/stat-card'
import { Catalog } from '@/services/catalog'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const obj = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!obj) return []
  if (Array.isArray(obj.content)) return obj.content
  if (Array.isArray(obj.data)) return obj.data as T[]
  return []
}

export function MyTeamPage() {
  const navigate = useNavigate()
  const teamQ = useQuery({ queryKey: ['me-team'], queryFn: () => Catalog.me.team() })
  const dashQ = useQuery({ queryKey: ['me-dashboard'], queryFn: () => Catalog.me.dashboard() })
  const approvalsQ = useQuery({ queryKey: ['me-approvals-count'], queryFn: () => Catalog.me.approvals() })
  const calendarQ = useQuery({ queryKey: ['team-calendar'], queryFn: () => Catalog.leaves.teamCalendar() })

  const team = rows<AnyObj>(teamQ.data)
  const approvals = rows<AnyObj>(approvalsQ.data)
  const calendar = rows<AnyObj>(calendarQ.data)
  const today = new Date().toISOString().slice(0, 10)
  const outToday = calendar.filter(c => {
    const s = String(c.startDate || c.date || ''), e = String(c.endDate || c.date || '')
    return s <= today && today <= e
  })

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="My Team" description="People-manager workspace" breadcrumbs={[{ label: 'My Space' }, { label: 'My Team' }]} />

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <StatCard title="Team Size" value={team.length} icon={<Users className="h-5 w-5 text-brand-600" />} loading={teamQ.isLoading} />
        <StatCard title="Pending Approvals" value={approvals.length} icon={<CheckSquare className="h-5 w-5 text-brand-600" />} loading={approvalsQ.isLoading} />
        <StatCard title="Out Today" value={outToday.length} icon={<CalendarDays className="h-5 w-5 text-brand-600" />} loading={calendarQ.isLoading} />
        <StatCard title="On Leave (period)" value={calendar.length} icon={<Clock className="h-5 w-5 text-brand-600" />} loading={calendarQ.isLoading} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Roster */}
        <Card className="lg:col-span-2">
          <CardHeader><div className="flex items-center justify-between"><CardTitle className="text-sm flex items-center gap-2"><Users className="h-4 w-4" /> Direct Reports</CardTitle><Button size="sm" variant="ghost" onClick={() => navigate('/employees')}>View all</Button></div></CardHeader>
          <CardContent>
            {teamQ.isLoading ? <Skeleton className="h-40" /> : team.length === 0 ? (
              <EmptyState icon={<Users className="h-6 w-6" />} title="No direct reports" description="You aren't a reporting manager for anyone yet." />
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                {team.map((m: AnyObj) => (
                  <button key={String(m.id)} onClick={() => navigate(`/employees/${m.id}`)} className="flex items-center gap-3 p-2 rounded-lg border hover:bg-slate-50 text-left">
                    <Avatar name={String(m.fullName || '')} size="sm" />
                    <div className="min-w-0 flex-1">
                      <p className="text-sm font-medium truncate">{String(m.fullName || '')}</p>
                      <p className="text-xs text-slate-500 truncate">{String(m.designationName || '')}</p>
                    </div>
                    <Badge variant={String(m.status) === 'ACTIVE' ? 'success' : 'secondary'}>{String(m.status || '').replace('_', ' ')}</Badge>
                  </button>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Who's out today */}
        <Card>
          <CardHeader><CardTitle className="text-sm flex items-center gap-2"><CalendarDays className="h-4 w-4" /> Out Today</CardTitle></CardHeader>
          <CardContent>
            {calendarQ.isLoading ? <Skeleton className="h-32" /> : outToday.length === 0 ? (
              <EmptyState icon={<CalendarDays className="h-6 w-6" />} title="Everyone's in" />
            ) : outToday.map((c: AnyObj, i) => (
              <div key={String(c.id) || `${i}`} className="flex items-center gap-2 py-2 border-b border-slate-100 last:border-0">
                <Avatar name={String(c.employeeName || '')} size="xs" />
                <div className="min-w-0 flex-1">
                  <p className="text-sm truncate">{String(c.employeeName || '')}</p>
                  <p className="text-xs text-slate-500">{String(c.leaveTypeName || 'Leave')} · until {formatDate(String(c.endDate || c.date))}</p>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      {/* Pending approvals quick list */}
      <Card>
        <CardHeader><div className="flex items-center justify-between"><CardTitle className="text-sm flex items-center gap-2"><CheckSquare className="h-4 w-4" /> Approvals awaiting you</CardTitle><Button size="sm" variant="ghost" onClick={() => navigate('/approvals')}>Open inbox</Button></div></CardHeader>
        <CardContent>
          {approvalsQ.isLoading ? <Skeleton className="h-24" /> : approvals.length === 0 ? (
            <EmptyState icon={<CheckSquare className="h-6 w-6" />} title="Inbox zero" />
          ) : approvals.slice(0, 6).map((a: AnyObj) => (
            <div key={String(a.id)} className="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
              <div className="flex items-center gap-2">
                <Badge variant="secondary">{String(a.entityType || a.type || 'REQUEST').replace(/_/g, ' ')}</Badge>
                <span className="text-sm">{String(a.title || a.description || 'Approval request')}</span>
              </div>
              <span className="text-xs text-slate-500">{a.createdAt ? formatDate(String(a.createdAt)) : ''}</span>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Manager dashboard metrics passthrough */}
      {dashQ.data ? (
        <Card>
          <CardHeader><CardTitle className="text-sm flex items-center gap-2"><TrendingUp className="h-4 w-4" /> Team Metrics</CardTitle></CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
              {Object.entries(dashQ.data as AnyObj).filter(([, v]) => typeof v === 'number' || typeof v === 'string').slice(0, 8).map(([k, v]) => (
                <div key={k} className="p-3 rounded-lg bg-slate-50">
                  <p className="text-xs text-slate-500 capitalize">{k.replace(/([A-Z])/g, ' $1')}</p>
                  <p className="text-lg font-bold">{String(v)}</p>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      ) : dashQ.isLoading ? <Skeleton className="h-24" /> : (
        <Card><CardContent className="py-6"><EmptyState icon={<UserCircle className="h-6 w-6" />} title="No team metrics available" /></CardContent></Card>
      )}
    </div>
  )
}
