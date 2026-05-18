import React from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts'
import {
  Users,
  UserCheck,
  UserX,
  Briefcase,
  TrendingUp,
  Calendar,
  Clock,
  PartyPopper,
  ArrowRight,
  Plus,
  FileText,
  Gift,
  Star,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { StatCard } from '@/components/ui/stat-card'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { PageHeader } from '@/components/ui/page-header'
import { formatDate } from '@/lib/utils'
import { useAuthStore } from '@/store/authStore'

// Mock data for demo
const headcountData = [
  { dept: 'Engineering', count: 42 },
  { dept: 'Product', count: 18 },
  { dept: 'Marketing', count: 14 },
  { dept: 'Sales', count: 28 },
  { dept: 'HR', count: 8 },
  { dept: 'Finance', count: 12 },
  { dept: 'Operations', count: 22 },
]

const attendanceTrend = [
  { date: 'Mon', present: 118, absent: 6, leave: 8 },
  { date: 'Tue', present: 122, absent: 4, leave: 6 },
  { date: 'Wed', present: 115, absent: 8, leave: 9 },
  { date: 'Thu', present: 124, absent: 3, leave: 5 },
  { date: 'Fri', present: 110, absent: 10, leave: 12 },
]

const leaveDistribution = [
  { name: 'Annual Leave', value: 45, color: '#4f46e5' },
  { name: 'Sick Leave', value: 28, color: '#7c3aed' },
  { name: 'Casual Leave', value: 18, color: '#a78bfa' },
  { name: 'Unpaid', value: 9, color: '#c4b5fd' },
]

const recentActivities = [
  { id: 1, type: 'leave', message: 'Priya Sharma applied for Annual Leave', time: '10 min ago', avatar: undefined, name: 'Priya Sharma' },
  { id: 2, type: 'hire', message: 'New employee Rahul Verma joined Engineering', time: '1 hr ago', avatar: undefined, name: 'Rahul Verma' },
  { id: 3, type: 'review', message: 'Q1 performance review started for 24 employees', time: '2 hrs ago', avatar: undefined, name: 'System' },
  { id: 4, type: 'payroll', message: 'March payroll processed successfully', time: '3 hrs ago', avatar: undefined, name: 'Finance' },
  { id: 5, type: 'leave', message: 'Amit Kumar leave request approved', time: '5 hrs ago', avatar: undefined, name: 'Amit Kumar' },
]

const upcomingBirthdays = [
  { name: 'Anika Patel', date: 'Apr 7', dept: 'Engineering' },
  { name: 'Rohit Singh', date: 'Apr 10', dept: 'Marketing' },
  { name: 'Sneha Gupta', date: 'Apr 14', dept: 'HR' },
]

const quickActions = [
  { label: 'Add Employee', icon: Users, href: '/employees/new', color: 'bg-brand-100 text-brand-700' },
  { label: 'Apply Leave', icon: Calendar, href: '/my-leave', color: 'bg-violet-100 text-violet-700' },
  { label: 'Run Payroll', icon: TrendingUp, href: '/pay-runs', color: 'bg-green-100 text-green-700' },
  { label: 'Post Job', icon: Briefcase, href: '/jobs/new', color: 'bg-amber-100 text-amber-700' },
  { label: 'Add Document', icon: FileText, href: '/documents', color: 'bg-blue-100 text-blue-700' },
  { label: 'Schedule Review', icon: Star, href: '/reviews', color: 'bg-pink-100 text-pink-700' },
]

const activityTypeColors: Record<string, string> = {
  leave: 'bg-violet-100 text-violet-700',
  hire: 'bg-green-100 text-green-700',
  review: 'bg-amber-100 text-amber-700',
  payroll: 'bg-blue-100 text-blue-700',
}

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

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
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
            <TrendingUp className="h-4 w-4" />
            Reports
          </Button>
          <Button size="sm" onClick={() => navigate('/employees/new')}>
            <Plus className="h-4 w-4" />
            Add Employee
          </Button>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Employees"
          value="144"
          icon={<Users className="h-5 w-5 text-brand-600" />}
          iconBg="bg-brand-100"
          trend={3.2}
          trendLabel="vs last month"
        />
        <StatCard
          title="Present Today"
          value="124"
          icon={<UserCheck className="h-5 w-5 text-green-600" />}
          iconBg="bg-green-100"
          subtitle="86% attendance rate"
        />
        <StatCard
          title="On Leave"
          value="12"
          icon={<UserX className="h-5 w-5 text-amber-600" />}
          iconBg="bg-amber-100"
          subtitle="3 pending approval"
        />
        <StatCard
          title="Open Positions"
          value="8"
          icon={<Briefcase className="h-5 w-5 text-violet-600" />}
          iconBg="bg-violet-100"
          trend={-12.5}
          trendLabel="vs last month"
        />
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Headcount by Dept */}
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
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={headcountData} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis
                  dataKey="dept"
                  tick={{ fontSize: 11, fill: '#94a3b8' }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  tick={{ fontSize: 11, fill: '#94a3b8' }}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  contentStyle={{
                    background: '#1e293b',
                    border: 'none',
                    borderRadius: '8px',
                    color: '#f1f5f9',
                    fontSize: '12px',
                  }}
                />
                <Bar
                  dataKey="count"
                  fill="#4f46e5"
                  radius={[4, 4, 0, 0]}
                  name="Employees"
                />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        {/* Leave Distribution */}
        <Card>
          <CardHeader>
            <CardTitle>Leave Distribution</CardTitle>
            <CardDescription>This month</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={180}>
              <PieChart>
                <Pie
                  data={leaveDistribution}
                  cx="50%"
                  cy="50%"
                  innerRadius={50}
                  outerRadius={75}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {leaveDistribution.map((entry, index) => (
                    <Cell key={index} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    background: '#1e293b',
                    border: 'none',
                    borderRadius: '8px',
                    color: '#f1f5f9',
                    fontSize: '12px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
            <div className="mt-3 space-y-2">
              {leaveDistribution.map(item => (
                <div key={item.name} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="h-2.5 w-2.5 rounded-full flex-shrink-0" style={{ backgroundColor: item.color }} />
                    <span className="text-xs text-slate-600">{item.name}</span>
                  </div>
                  <span className="text-xs font-semibold text-slate-800">{item.value}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Attendance trend */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>Attendance Trend</CardTitle>
              <CardDescription>This week&apos;s daily breakdown</CardDescription>
            </div>
            <div className="flex items-center gap-4 text-xs text-slate-500">
              <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-full bg-brand-500" />Present</span>
              <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-full bg-red-400" />Absent</span>
              <span className="flex items-center gap-1.5"><span className="h-2 w-2 rounded-full bg-amber-400" />On Leave</span>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={attendanceTrend} margin={{ top: 5, right: 10, left: -20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
              <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fontSize: 12, fill: '#94a3b8' }} axisLine={false} tickLine={false} />
              <Tooltip
                contentStyle={{
                  background: '#1e293b',
                  border: 'none',
                  borderRadius: '8px',
                  color: '#f1f5f9',
                  fontSize: '12px',
                }}
              />
              <Line type="monotone" dataKey="present" stroke="#4f46e5" strokeWidth={2.5} dot={{ r: 4, fill: '#4f46e5' }} name="Present" />
              <Line type="monotone" dataKey="absent" stroke="#f87171" strokeWidth={2} dot={{ r: 3, fill: '#f87171' }} name="Absent" />
              <Line type="monotone" dataKey="leave" stroke="#fbbf24" strokeWidth={2} dot={{ r: 3, fill: '#fbbf24' }} name="On Leave" />
            </LineChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Bottom row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Recent activities */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <div className="flex items-center justify-between">
              <CardTitle>Recent Activities</CardTitle>
              <Button variant="ghost" size="sm">View all</Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentActivities.map(activity => (
                <div key={activity.id} className="flex items-start gap-3">
                  <Avatar name={activity.name} size="sm" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-slate-700 leading-snug">{activity.message}</p>
                    <p className="text-xs text-slate-400 mt-0.5">{activity.time}</p>
                  </div>
                  <Badge
                    className={activityTypeColors[activity.type] || 'bg-slate-100 text-slate-600'}
                  >
                    {activity.type}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Right column: Quick actions + Birthdays */}
        <div className="space-y-4">
          {/* Quick actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-2">
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

          {/* Upcoming birthdays */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>Upcoming Birthdays</CardTitle>
                <Gift className="h-4 w-4 text-slate-400" />
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {upcomingBirthdays.map(person => (
                  <div key={person.name} className="flex items-center gap-3">
                    <Avatar name={person.name} size="sm" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-slate-800 truncate">{person.name}</p>
                      <p className="text-xs text-slate-400">{person.dept}</p>
                    </div>
                    <div className="flex items-center gap-1 text-xs text-violet-600 font-medium bg-violet-50 px-2 py-1 rounded-full">
                      <PartyPopper className="h-3 w-3" />
                      {person.date}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
