import React, { useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { SimpleTooltip } from '@/components/ui/tooltip'
import {
  LayoutDashboard,
  Users,
  UserCircle,
  Calendar,
  FileText,
  CreditCard,
  Building2,
  MapPin,
  Briefcase,
  UserPlus,
  MessageSquare,
  Gift,
  BarChart3,
  Settings,
  ChevronLeft,
  ChevronRight,
  Clock,
  CheckSquare,
  DollarSign,
  Target,
  BookOpen,
  Heart,
  Package,
  Receipt,
  Shield,
  LogOut,
  Cpu,
  Star,
  TrendingUp,
  Bell,
  HelpCircle,
  GraduationCap,
  UserMinus,
} from 'lucide-react'
import { useAuth } from '@/hooks/useAuth'
import { Avatar } from '@/components/ui/avatar'

interface NavItem {
  label: string
  href: string
  icon: React.ComponentType<{ className?: string; style?: React.CSSProperties }>
  badge?: number
}

interface NavGroup {
  label: string
  items: NavItem[]
}

const navGroups: NavGroup[] = [
  {
    label: 'Overview',
    items: [
      { label: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
    ],
  },
  {
    label: 'My Space',
    items: [
      { label: 'My Profile', href: '/profile', icon: UserCircle },
      { label: 'My Leave', href: '/my-leave', icon: Calendar },
      { label: 'Attendance', href: '/attendance', icon: Clock },
      { label: 'My Payslips', href: '/payslips', icon: CreditCard },
      { label: 'My Documents', href: '/documents', icon: FileText },
    ],
  },
  {
    label: 'People',
    items: [
      { label: 'Employees', href: '/employees', icon: Users },
      { label: 'Departments', href: '/departments', icon: Building2 },
      { label: 'Designations', href: '/designations', icon: Star },
      { label: 'Locations', href: '/locations', icon: MapPin },
    ],
  },
  {
    label: 'Recruitment',
    items: [
      { label: 'Job Openings', href: '/jobs', icon: Briefcase },
      { label: 'Candidates', href: '/candidates', icon: UserPlus },
      { label: 'Applications', href: '/applications', icon: CheckSquare },
    ],
  },
  {
    label: 'Time & Leave',
    items: [
      { label: 'Leave Types', href: '/leave-types', icon: Calendar },
      { label: 'Leave Balances', href: '/leave-balances', icon: TrendingUp },
      { label: 'Approvals', href: '/leave-approvals', icon: CheckSquare },
      { label: 'Holidays', href: '/holidays', icon: Gift },
    ],
  },
  {
    label: 'Payroll',
    items: [
      { label: 'Salary Structures', href: '/salary-structures', icon: DollarSign },
      { label: 'Pay Runs', href: '/pay-runs', icon: Cpu },
      { label: 'Payslips', href: '/payroll-payslips', icon: CreditCard },
    ],
  },
  {
    label: 'Performance',
    items: [
      { label: 'Goals', href: '/goals', icon: Target },
      { label: 'Reviews', href: '/reviews', icon: Star },
    ],
  },
  {
    label: 'Learning',
    items: [
      { label: 'Courses', href: '/courses', icon: BookOpen },
      { label: 'Enrollments', href: '/enrollments', icon: GraduationCap },
    ],
  },
  {
    label: 'Engagement',
    items: [
      { label: 'Social Feed', href: '/social', icon: Heart },
      { label: 'Helpdesk', href: '/helpdesk', icon: HelpCircle },
      { label: 'Notifications', href: '/notifications', icon: Bell },
    ],
  },
  {
    label: 'Assets & Expense',
    items: [
      { label: 'Assets', href: '/assets', icon: Package },
      { label: 'Expenses', href: '/expenses', icon: Receipt },
    ],
  },
  {
    label: 'Compliance',
    items: [
      { label: 'Compliance', href: '/compliance', icon: Shield },
      { label: 'Offboarding', href: '/offboarding', icon: UserMinus },
    ],
  },
  {
    label: 'Reports',
    items: [
      { label: 'Reports & Analytics', href: '/reports', icon: BarChart3 },
    ],
  },
  {
    label: 'Administration',
    items: [
      { label: 'Settings', href: '/settings', icon: Settings },
    ],
  },
]

interface SidebarProps {
  collapsed: boolean
  onToggle: () => void
}

export function Sidebar({ collapsed, onToggle }: SidebarProps) {
  const { user, logout } = useAuth()
  const location = useLocation()
  const [expandedGroups, setExpandedGroups] = useState<Set<string>>(
    new Set(navGroups.map(g => g.label))
  )

  const toggleGroup = (label: string) => {
    if (collapsed) return
    setExpandedGroups(prev => {
      const next = new Set(prev)
      if (next.has(label)) next.delete(label)
      else next.add(label)
      return next
    })
  }

  const isActive = (href: string) => {
    if (href === '/dashboard') return location.pathname === '/dashboard' || location.pathname === '/'
    return location.pathname.startsWith(href)
  }

  return (
    <div
      className={cn(
        'flex flex-col h-screen transition-all duration-300 ease-in-out relative',
        'shadow-[4px_0_24px_rgba(0,0,0,0.12)]',
      )}
      style={{
        width: collapsed ? 68 : 260,
        backgroundColor: '#1e1b4b',
      }}
    >
      {/* Logo */}
      <div
        className="flex items-center h-16 px-4 border-b flex-shrink-0"
        style={{ borderColor: 'rgba(255,255,255,0.08)' }}
      >
        <div className="flex items-center gap-3 min-w-0">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-brand-500 to-violet-600 flex-shrink-0">
            <span className="text-white font-bold text-sm">H</span>
          </div>
          {!collapsed && (
            <div className="min-w-0">
              <p className="text-white font-bold text-base leading-none truncate">HRMS</p>
              <p className="text-xs leading-none mt-0.5 truncate" style={{ color: 'rgba(255,255,255,0.5)' }}>
                People Platform
              </p>
            </div>
          )}
        </div>
      </div>

      {/* Nav */}
      <nav className="flex-1 overflow-y-auto py-3 sidebar-scroll">
        <div className="px-2 space-y-0.5">
          {navGroups.map(group => (
            <div key={group.label}>
              {/* Group label */}
              {!collapsed && (
                <button
                  onClick={() => toggleGroup(group.label)}
                  className="w-full flex items-center justify-between px-2 py-1.5 text-xs font-semibold uppercase tracking-wider mb-0.5 rounded transition-colors hover:bg-white/5"
                  style={{ color: 'rgba(255,255,255,0.35)' }}
                >
                  <span>{group.label}</span>
                  <ChevronRight
                    className={cn(
                      'h-3 w-3 transition-transform duration-200',
                      expandedGroups.has(group.label) && 'rotate-90'
                    )}
                  />
                </button>
              )}

              {/* Items */}
              {(collapsed || expandedGroups.has(group.label)) && (
                <div className={cn('space-y-0.5', !collapsed && 'mb-2')}>
                  {group.items.map(item => {
                    const active = isActive(item.href)
                    const Icon = item.icon

                    if (collapsed) {
                      return (
                        <SimpleTooltip key={item.href} content={item.label} side="right">
                          <NavLink
                            to={item.href}
                            className={cn(
                              'flex items-center justify-center h-10 w-10 mx-auto rounded-lg transition-all duration-150',
                              active
                                ? 'bg-brand-600/30 text-white'
                                : 'text-white/60 hover:bg-white/08 hover:text-white'
                            )}
                          >
                            <Icon className="h-4 w-4" />
                          </NavLink>
                        </SimpleTooltip>
                      )
                    }

                    return (
                      <NavLink
                        key={item.href}
                        to={item.href}
                        className={cn(
                          'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all duration-150 group',
                          active
                            ? 'font-semibold'
                            : 'font-normal',
                        )}
                        style={{
                          backgroundColor: active ? 'rgba(99,102,241,0.25)' : 'transparent',
                          color: active ? '#ffffff' : 'rgba(255,255,255,0.7)',
                        }}
                        onMouseEnter={(e) => {
                          if (!active) {
                            e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.08)'
                            e.currentTarget.style.color = '#ffffff'
                          }
                        }}
                        onMouseLeave={(e) => {
                          if (!active) {
                            e.currentTarget.style.backgroundColor = 'transparent'
                            e.currentTarget.style.color = 'rgba(255,255,255,0.7)'
                          }
                        }}
                      >
                        <Icon
                          className="h-4 w-4 flex-shrink-0"
                          style={{ color: active ? '#a5b4fc' : 'rgba(255,255,255,0.5)' }}
                        />
                        <span className="truncate">{item.label}</span>
                        {item.badge !== undefined && (
                          <span className="ml-auto flex h-5 w-5 items-center justify-center rounded-full bg-brand-500 text-[10px] font-bold text-white">
                            {item.badge}
                          </span>
                        )}
                      </NavLink>
                    )
                  })}
                </div>
              )}
            </div>
          ))}
        </div>
      </nav>

      {/* User footer */}
      <div
        className="flex-shrink-0 p-3 border-t"
        style={{ borderColor: 'rgba(255,255,255,0.08)' }}
      >
        {collapsed ? (
          <SimpleTooltip content={user?.fullName || 'User'} side="right">
            <button className="w-10 h-10 mx-auto flex items-center justify-center rounded-lg hover:bg-white/08 transition-colors">
              <Avatar name={user?.fullName} size="sm" />
            </button>
          </SimpleTooltip>
        ) : (
          <div className="flex items-center gap-3 px-2 py-2 rounded-lg hover:bg-white/05 transition-colors cursor-pointer group">
            <Avatar name={user?.fullName} size="sm" />
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{user?.fullName || 'User'}</p>
              <p className="text-xs truncate" style={{ color: 'rgba(255,255,255,0.45)' }}>
                {user?.roles?.[0]?.replace('_', ' ') || 'Employee'}
              </p>
            </div>
            <button
              onClick={logout}
              className="opacity-0 group-hover:opacity-100 p-1 rounded hover:bg-white/10 transition-all"
              style={{ color: 'rgba(255,255,255,0.5)' }}
              title="Logout"
            >
              <LogOut className="h-3.5 w-3.5" />
            </button>
          </div>
        )}
      </div>

      {/* Toggle button */}
      <button
        onClick={onToggle}
        className="absolute -right-3 top-20 z-10 flex h-6 w-6 items-center justify-center rounded-full border border-slate-200 bg-white shadow-md text-slate-600 hover:text-slate-900 hover:shadow-lg transition-all duration-150"
      >
        {collapsed ? (
          <ChevronRight className="h-3 w-3" />
        ) : (
          <ChevronLeft className="h-3 w-3" />
        )}
      </button>
    </div>
  )
}
