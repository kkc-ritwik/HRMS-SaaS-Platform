import { useQuery } from '@tanstack/react-query'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell,
} from 'recharts'
import {
  Users, UserCheck, UserX, Briefcase, TrendingUp, Calendar, ArrowRight, Plus, FileText, Star,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { StatCard } from '@/components/ui/stat-card'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { formatDate } from '@/lib/utils'
import { useAuthStore } from '@/store/authStore'
import { dashboardService } from '@/services/dashboardService'

const BREAKDOWN_COLORS = ['#22c55e', '#f59e0b', '#14b8a6', '#a78bfa', '#f87171']

const quickActions = [
  { label: 'Add Employee', icon: Users, href: '/employees/new', color: 'bg-brand-100 text-brand-700' },
  { label: 'Apply Leave', icon: Calendar, href: '/my-leave', color: 'bg-violet-100 text-violet-700' },
  { label: 'Run Payroll', icon: TrendingUp, href: '/pay-runs', color: 'bg-green-100 text-green-700' },
  { label: 'Post Job', icon: Briefcase, href: '/jobs/new', color: 'bg-amber-100 text-amber-700' },
  { label: 'My Documents', icon: FileText, href: '/documents', color: 'bg-blue-100 text-blue-700' },
  { label: 'Reviews', icon: Star, href: '/reviews', color: 'bg-pink-100 text-pink-700' },
]

export function DashboardPage() {
  const user = useAuthStore(s => s.user)
  const navigate = useNavigate()
  const today = new Date()

  const greeting = () => {
    const h = today.getHours()
    if (h < 12) return 'Good morning'
    if (h < 17) return 'Good afternoon'
    return 'Good evening'
  }

  const stats = useQuery({ queryKey: ['dashboard', 'stats'], queryFn: dashboardService.stats })
  const headcount = useQuery({ queryKey: ['dashboard', 'headcount'], queryFn: dashboardService.headcountByDept })
  const breakdown = useQuery({ queryKey: ['dashboard', 'today-attendance'], queryFn: dashboardService.todayAttendanceBreakdown })

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-bold text-slate-900">
            {greeting()}, {user?.fullName?.split(' ')[0] || 'there'} 👋
          </h1>
          <p className="text-sm text-slate-500 mt-0.5">
            {formatDate(today, 'EEEE, MMMM d, yyyy')} · Here&apos;s what&apos;s happening today
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => navigate('/reports')}>
            <TrendingUp className="h-4 w-4" /> Reports
          </Button>
          <Button size="sm" onClick={() => navigate('/employees/new')}>
            <Plus className="h-4 w-4" /> Add Employee
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.isLoading ? (
          <>{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-28" />)}</>
        ) : (
          <>
            <StatCard title="Total Employees" value={String(stats.data?.totalEmployees ?? '—')}
              icon={<Users className="h-5 w-5 text-brand-600" />} iconBg="bg-brand-100" />
            <StatCard title="Present Today" value={String(stats.data?.presentToday ?? '—')}
              icon={<UserCheck className="h-5 w-5 text-green-600" />} iconBg="bg-green-100"
              subtitle={`${stats.data?.lateToday ?? 0} late`} />
            <StatCard title="On Leave" value={String(stats.data?.onLeave ?? '—')}
              icon={<UserX className="h-5 w-5 text-amber-600" />} iconBg="bg-amber-100"
              subtitle={`${stats.data?.absentToday ?? 0} absent`} />
            <StatCard title="Open Positions" value={String(stats.data?.openRequisitions ?? '—')}
              icon={<Briefcase className="h-5 w-5 text-violet-600" />} iconBg="bg-violet-100" />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Headcount by Department</CardTitle>
                <CardDescription>Current employee distribution</CardDescription>
              </div>
              <Button variant="ghost" size="sm" onClick={() => navigate('/reports')}>
                View all <ArrowRight className="h-3.5 w-3.5" />
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {headcount.isLoading ? (
              <Skeleton className="h-56" />
            ) : (
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={headcount.data || []} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                  <XAxis dataKey="department" tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
                  <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={{ background: '#1e293b', border: 'none', borderRadius: '8px', color: '#f1f5f9', fontSize: '12px' }} />
                  <Bar dataKey="count" fill="#4f46e5" radius={[4, 4, 0, 0]} name="Employees" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Today&apos;s Attendance</CardTitle>
            <CardDescription>Live snapshot</CardDescription>
          </CardHeader>
          <CardContent>
            {breakdown.isLoading ? (
              <Skeleton className="h-56" />
            ) : (
              <>
                <ResponsiveContainer width="100%" height={180}>
                  <BarChart data={breakdown.data || []} layout="vertical" margin={{ top: 5, right: 10, left: 10, bottom: 5 }}>
                    <XAxis type="number" hide />
                    <YAxis type="category" dataKey="name" tick={{ fontSize: 11, fill: '#64748b' }} axisLine={false} tickLine={false} width={70} />
                    <Tooltip contentStyle={{ background: '#1e293b', border: 'none', borderRadius: '8px', color: '#f1f5f9', fontSize: '12px' }} />
                    <Bar dataKey="value" radius={[0, 4, 4, 0]} name="Employees">
                      {(breakdown.data || []).map((_, i) => <Cell key={i} fill={BREAKDOWN_COLORS[i % BREAKDOWN_COLORS.length]} />)}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
                <div className="mt-3 space-y-2">
                  {(breakdown.data || []).map((item, i) => (
                    <div key={item.name} className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <div className="h-2.5 w-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: BREAKDOWN_COLORS[i % BREAKDOWN_COLORS.length] }} />
                        <span className="text-xs text-slate-600">{item.name}</span>
                      </div>
                      <span className="text-xs font-semibold text-slate-800">{item.value}</span>
                    </div>
                  ))}
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader><CardTitle>Quick Actions</CardTitle></CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-2">
            {quickActions.map(action => {
              const Icon = action.icon
              return (
                <button
                  key={action.label}
                  onClick={() => navigate(action.href)}
                  className="flex flex-col items-center gap-2 p-3 rounded-xl hover:bg-slate-50 transition-colors group border border-slate-100 hover:border-slate-200"
                >
                  <div className={`flex h-9 w-9 items-center justify-center rounded-lg ${action.color}`}>
                    <Icon className="h-4 w-4" />
                  </div>
                  <span className="text-xs font-medium text-slate-600 text-center leading-tight group-hover:text-slate-900">
                    {action.label}
                  </span>
                </button>
              )
            })}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
