import React, { useState } from 'react'
import { NavLink, useLocation } from 'react-router-dom'
import { cn } from '@/lib/utils'
import { SimpleTooltip } from '@/components/ui/tooltip'
import {
  LayoutDashboard, Users, UserCircle, Calendar, FileText, CreditCard, Building2,
  MapPin, Briefcase, UserPlus, MessageSquare, Gift, BarChart3, Settings, ChevronLeft,
  ChevronRight, Clock, CheckSquare, DollarSign, Target, BookOpen, Heart, Package,
  Receipt, Shield, LogOut, Cpu, Star, TrendingUp, Bell, HelpCircle, GraduationCap,
  UserMinus, FileSignature, Network, Banknote, Award, Boxes, Trophy, Wrench, Plane,
  Activity, Sparkles, GitBranch, AlertTriangle, BadgeCheck, ClipboardList, ClipboardEdit,
  Wallet, Repeat, MessageCircle,
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
      { label: 'Notifications', href: '/notifications', icon: Bell },
      { label: 'My Approvals', href: '/approvals', icon: CheckSquare },
    ],
  },
  {
    label: 'My Space',
    items: [
      { label: 'My Profile', href: '/profile', icon: UserCircle },
      { label: 'My Team', href: '/my-team', icon: Users },
      { label: 'My Leave', href: '/my-leave', icon: Calendar },
      { label: 'Attendance', href: '/attendance', icon: Clock },
      { label: 'Regularizations', href: '/regularizations', icon: ClipboardEdit },
      { label: 'Timesheet', href: '/timesheet', icon: Clock },
      { label: 'My Payslips', href: '/payslips', icon: CreditCard },
      { label: 'My Documents', href: '/documents', icon: FileText },
      { label: 'Tax Declaration', href: '/tax/declaration', icon: Receipt },
      { label: 'Investment Proofs', href: '/tax/proofs', icon: FileText },
      { label: 'Company Policies', href: '/policies', icon: BadgeCheck },
    ],
  },
  {
    label: 'People',
    items: [
      { label: 'Employees', href: '/employees', icon: Users },
      { label: 'Org Chart', href: '/org-chart', icon: Network },
      { label: 'Departments', href: '/departments', icon: Building2 },
      { label: 'Designations', href: '/designations', icon: Star },
      { label: 'Locations', href: '/locations', icon: MapPin },
      { label: 'Cost Centres', href: '/cost-centers', icon: Wallet },
      { label: 'Skills', href: '/skills', icon: Sparkles },
      { label: 'Onboarding', href: '/onboarding', icon: UserPlus },
      { label: 'Onboarding Documents', href: '/onboarding-documents', icon: FileText },
      { label: 'Bulk Import', href: '/employee-bulk-import', icon: UserPlus },
      { label: 'Offboarding', href: '/offboarding', icon: UserMinus },
    ],
  },
  {
    label: 'Recruitment',
    items: [
      { label: 'Job Openings', href: '/jobs', icon: Briefcase },
      { label: 'Pipeline Kanban', href: '/pipeline', icon: GitBranch },
      { label: 'Candidates', href: '/candidates', icon: UserPlus },
      { label: 'Applications', href: '/applications', icon: CheckSquare },
      { label: 'Interviews', href: '/interviews', icon: Calendar },
      { label: 'Hiring Loops', href: '/hiring-loops', icon: Users },
      { label: 'Offers', href: '/offers', icon: FileSignature },
      { label: 'Reference Checks', href: '/reference-checks', icon: CheckSquare },
      { label: 'Psychometric Tests', href: '/psychometric', icon: Star },
      { label: 'Internal Mobility', href: '/internal-mobility', icon: TrendingUp },
      { label: 'Career Site', href: '/career-site', icon: Briefcase },
    ],
  },
  {
    label: 'Time & Leave',
    items: [
      { label: 'Leave Types', href: '/leave-types', icon: Calendar },
      { label: 'Leave Balances', href: '/leave-balances', icon: TrendingUp },
      { label: 'Leave Approvals', href: '/leave-approvals', icon: CheckSquare },
      { label: 'Leave Policies', href: '/leave-policies', icon: Calendar },
      { label: 'Holidays', href: '/holidays', icon: Gift },
      { label: 'Team Calendar', href: '/calendar', icon: Calendar },
      { label: 'Shifts', href: '/shifts', icon: Clock },
    ],
  },
  {
    label: 'Payroll',
    items: [
      { label: 'Pay Runs', href: '/pay-runs', icon: Cpu },
      { label: 'Salary Structures', href: '/salary-structures', icon: DollarSign },
      { label: 'Pay Grades', href: '/pay-grades', icon: Award },
      { label: 'Compensation Plans', href: '/compensation-plans', icon: DollarSign },
      { label: 'Benefits', href: '/benefits', icon: Heart },
      { label: 'Benefit Enrolments', href: '/employee-benefits', icon: Heart },
      { label: 'Market Benchmark', href: '/market-benchmark', icon: TrendingUp },
      { label: 'Loans', href: '/loans', icon: Banknote },
      { label: 'GST Returns', href: '/gst-returns', icon: Receipt },
    ],
  },
  {
    label: 'Performance',
    items: [
      { label: 'Goals', href: '/goals', icon: Target },
      { label: 'Goal Cascade Tree', href: '/goal-cascade', icon: GitBranch },
      { label: '9-Box Talent Grid', href: '/nine-box', icon: Activity },
      { label: 'Reviews', href: '/reviews', icon: Star },
      { label: 'Review Cycles', href: '/review-cycles', icon: Repeat },
      { label: '1-on-1s', href: '/one-on-ones', icon: MessageCircle },
      { label: 'Competencies', href: '/competencies', icon: Target },
      { label: 'PIPs', href: '/pip', icon: AlertTriangle },
    ],
  },
  {
    label: 'Learning',
    items: [
      { label: 'Courses', href: '/courses', icon: BookOpen },
      { label: 'My Enrollments', href: '/enrollments', icon: GraduationCap },
      { label: 'Certifications', href: '/certifications', icon: Award },
    ],
  },
  {
    label: 'Engagement',
    items: [
      { label: 'Social Feed', href: '/social', icon: Heart },
      { label: 'Kudos', href: '/kudos', icon: Gift },
      { label: 'Awards', href: '/awards', icon: Trophy },
      { label: 'Rewards Catalogue', href: '/rewards-catalog', icon: Gift },
      { label: 'Suggestions', href: '/suggestions', icon: MessageSquare },
      { label: 'Polls', href: '/polls', icon: BarChart3 },
      { label: 'Surveys', href: '/surveys', icon: ClipboardList },
      { label: 'Pulse Check-in', href: '/pulse', icon: Heart },
      { label: 'Engagement Heatmap', href: '/heatmap', icon: Activity },
      { label: 'Wellness Programs', href: '/wellness', icon: Heart },
      { label: 'Stay Interviews', href: '/stay-interviews', icon: MessageCircle },
    ],
  },
  {
    label: 'Workplace',
    items: [
      { label: 'Book a Desk', href: '/desk-booking', icon: MapPin },
      { label: 'Floor Plan', href: '/floor-plan', icon: MapPin },
      { label: 'Visitors', href: '/visitors', icon: Users },
      { label: 'Travel Requests', href: '/travel', icon: Plane },
    ],
  },
  {
    label: 'Operational',
    items: [
      { label: 'Assets', href: '/assets', icon: Package },
      { label: 'Asset Categories', href: '/asset-categories', icon: Boxes },
      { label: 'Asset Requests', href: '/asset-requests', icon: ClipboardList },
      { label: 'Asset Maintenance', href: '/asset-maintenance', icon: Wrench },
      { label: 'AMC Contracts', href: '/amc-contracts', icon: FileSignature },
      { label: 'Vendors', href: '/vendors', icon: Building2 },
      { label: 'Expenses', href: '/expenses', icon: Receipt },
      { label: 'Expense Categories', href: '/expense-categories', icon: Receipt },
      { label: 'Advances', href: '/advances', icon: Banknote },
      { label: 'Helpdesk', href: '/helpdesk', icon: HelpCircle },
      { label: 'Knowledge Base', href: '/kb', icon: BookOpen },
      { label: 'CSAT Dashboard', href: '/csat', icon: Heart },
      { label: 'Files', href: '/files', icon: FileText },
    ],
  },
  {
    label: 'Workflow',
    items: [
      { label: 'My Approvals', href: '/approvals', icon: CheckSquare },
      { label: 'Workflows', href: '/workflows', icon: GitBranch },
      { label: 'Workflow Designer', href: '/workflows/designer', icon: GitBranch },
      { label: 'Form Builder', href: '/forms/builder', icon: ClipboardEdit },
      { label: 'Template Editor', href: '/settings/templates', icon: FileSignature },
      { label: 'Out of Office', href: '/out-of-office', icon: Calendar },
    ],
  },
  {
    label: 'Compliance',
    items: [
      { label: 'Audit Log', href: '/compliance', icon: Shield },
      { label: 'HR Cases', href: '/cases', icon: AlertTriangle },
      { label: 'GDPR / DPDP', href: '/gdpr', icon: Shield },
      { label: 'Compliance Checklist', href: '/compliance-items', icon: CheckSquare },
      { label: 'Licenses', href: '/licenses', icon: BadgeCheck },
    ],
  },
  {
    label: 'Documents',
    items: [
      { label: 'Letters', href: '/letters', icon: FileSignature },
      { label: 'Forms Library', href: '/forms', icon: ClipboardEdit },
    ],
  },
  {
    label: 'Insights',
    items: [
      { label: 'Reports & Analytics', href: '/reports', icon: BarChart3 },
      { label: 'D&I Analytics', href: '/dei', icon: Users },
    ],
  },
  {
    label: 'Administration',
    items: [
      { label: 'Settings', href: '/settings', icon: Settings },
      { label: 'Users', href: '/users', icon: Users },
      { label: 'Roles', href: '/roles', icon: Shield },
      { label: 'API Keys', href: '/api-keys', icon: Cpu },
      { label: 'Legal Entities', href: '/legal-entities', icon: Building2 },
      { label: 'Custom Fields', href: '/custom-fields', icon: Star },
      { label: 'Biometric Devices', href: '/biometric-devices', icon: Cpu },
      { label: 'Webhooks', href: '/webhooks', icon: Cpu },
      { label: 'Notification Preferences', href: '/notification-preferences', icon: Bell },
      { label: 'Announcements', href: '/announcements', icon: Bell },
      { label: 'Email Templates', href: '/email-templates', icon: FileText },
      { label: 'Statutory Returns', href: '/statutory-returns', icon: FileSignature },
      { label: 'Compliance Tasks', href: '/compliance-tasks', icon: CheckSquare },
      { label: 'Salary Components', href: '/salary-components', icon: DollarSign },
      { label: 'Employee Salaries', href: '/employee-salaries', icon: DollarSign },
      { label: 'Tax Config', href: '/tax-config', icon: Receipt },
      { label: 'Proof Verification', href: '/tax/proof-verification', icon: BadgeCheck },
      { label: 'Document Templates', href: '/document-templates', icon: FileSignature },
      { label: 'Document Types', href: '/document-types', icon: FileText },
      { label: 'File Vault', href: '/file-vault', icon: FileText },
      { label: 'Ticket Categories', href: '/ticket-categories', icon: HelpCircle },
      { label: 'Course Modules', href: '/course-modules', icon: BookOpen },
      { label: 'Assessments', href: '/assessments', icon: GraduationCap },
      { label: 'Expense Policies', href: '/expense-policies', icon: Receipt },
      { label: 'Receipt OCR', href: '/receipt-ocr', icon: Receipt },
      { label: 'Recruitment Agencies', href: '/recruitment-agencies', icon: Briefcase },
      { label: 'BGV Cases', href: '/bgv', icon: Shield },
      { label: 'Recruitment Analytics', href: '/recruitment-analytics', icon: BarChart3 },
      { label: 'Performance Analytics', href: '/performance-analytics', icon: BarChart3 },
      { label: 'Continuous Feedback', href: '/continuous-feedback', icon: MessageSquare },
      { label: 'Onboarding Templates', href: '/onboarding-templates', icon: ClipboardEdit },
      { label: 'Onboarding Tasks Library', href: '/onboarding-tasks-library', icon: CheckSquare },
      { label: 'Probation Reviews', href: '/probation-reviews', icon: Star },
      { label: 'Buddy Assignments', href: '/buddy-assignments', icon: UserPlus },
      { label: 'Pre-Onboarding', href: '/pre-onboarding', icon: UserPlus },
      { label: 'Exit Interviews', href: '/exit-interviews', icon: UserMinus },
      { label: 'Exit Checklists', href: '/exit-checklists', icon: CheckSquare },
      { label: 'Knowledge Transfers', href: '/knowledge-transfers', icon: BookOpen },
      { label: 'Groups', href: '/groups', icon: Users },
      { label: 'Events', href: '/events', icon: Calendar },
      { label: 'Dashboards', href: '/dashboards', icon: BarChart3 },
      { label: 'Saved Reports', href: '/saved-reports', icon: BarChart3 },
      { label: 'Job-Cost Reports', href: '/job-cost-reports', icon: BarChart3 },
      { label: 'Workflow Instances', href: '/workflow-instances', icon: GitBranch },
      { label: 'Workflow Steps', href: '/workflow-steps', icon: GitBranch },
      { label: 'Delegation Rules', href: '/delegation-rules', icon: GitBranch },
      { label: 'Dashboard Widgets', href: '/dashboard-widgets', icon: BarChart3 },
      { label: 'Document Versions', href: '/document-versions', icon: FileText },
      { label: 'Notification Dispatch', href: '/notification-dispatch', icon: Bell },
      { label: 'SCORM Player', href: '/scorm-player', icon: BookOpen },
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
