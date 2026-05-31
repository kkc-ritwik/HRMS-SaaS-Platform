import { useState, useEffect, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { Search, ArrowRight, X } from 'lucide-react'

interface NavTarget { label: string; href: string; group: string }

const NAV_TARGETS: NavTarget[] = [
  { label: 'Dashboard', href: '/dashboard', group: 'Overview' },
  { label: 'My Profile', href: '/profile', group: 'My Space' },
  { label: 'My Leave', href: '/my-leave', group: 'My Space' },
  { label: 'Apply Leave', href: '/leave/apply', group: 'My Space' },
  { label: 'My Payslips', href: '/payslips', group: 'My Space' },
  { label: 'My Documents', href: '/documents', group: 'My Space' },
  { label: 'Tax Declaration', href: '/tax/declaration', group: 'My Space' },
  { label: 'Investment Proofs', href: '/tax/proofs', group: 'My Space' },
  { label: 'Timesheet', href: '/timesheet', group: 'My Space' },
  { label: 'Attendance', href: '/attendance', group: 'My Space' },
  { label: 'Notifications', href: '/notifications', group: 'My Space' },
  { label: 'My Approvals', href: '/approvals', group: 'Workflow' },
  { label: 'Out of Office', href: '/out-of-office', group: 'Workflow' },
  { label: 'Employees', href: '/employees', group: 'People' },
  { label: 'Add Employee', href: '/employees/new', group: 'People' },
  { label: 'Org Chart', href: '/org-chart', group: 'People' },
  { label: 'Departments', href: '/departments', group: 'People' },
  { label: 'Cost Centres', href: '/cost-centers', group: 'People' },
  { label: 'Onboarding', href: '/onboarding', group: 'Lifecycle' },
  { label: 'Offboarding', href: '/offboarding', group: 'Lifecycle' },
  { label: 'Jobs', href: '/jobs', group: 'Recruitment' },
  { label: 'Pipeline Kanban', href: '/pipeline', group: 'Recruitment' },
  { label: 'Candidates', href: '/candidates', group: 'Recruitment' },
  { label: 'Interviews', href: '/interviews', group: 'Recruitment' },
  { label: 'Offers', href: '/offers', group: 'Recruitment' },
  { label: 'Hiring Loops', href: '/hiring-loops', group: 'Recruitment' },
  { label: 'Pay Runs', href: '/pay-runs', group: 'Payroll' },
  { label: 'Salary Structures', href: '/salary-structures', group: 'Payroll' },
  { label: 'Pay Grades', href: '/pay-grades', group: 'Payroll' },
  { label: 'Benefits', href: '/benefits', group: 'Payroll' },
  { label: 'Loans', href: '/loans', group: 'Payroll' },
  { label: 'Goals', href: '/goals', group: 'Performance' },
  { label: 'Goal Cascade', href: '/goal-cascade', group: 'Performance' },
  { label: 'Reviews', href: '/reviews', group: 'Performance' },
  { label: 'Review Cycles', href: '/review-cycles', group: 'Performance' },
  { label: '9-Box Grid', href: '/nine-box', group: 'Performance' },
  { label: '1-on-1s', href: '/one-on-ones', group: 'Performance' },
  { label: 'Competencies', href: '/competencies', group: 'Performance' },
  { label: 'PIPs', href: '/pip', group: 'Performance' },
  { label: 'Courses', href: '/courses', group: 'Learning' },
  { label: 'My Enrollments', href: '/enrollments', group: 'Learning' },
  { label: 'Certifications', href: '/certifications', group: 'Learning' },
  { label: 'Social Feed', href: '/social', group: 'Engagement' },
  { label: 'Kudos', href: '/kudos', group: 'Engagement' },
  { label: 'Awards', href: '/awards', group: 'Engagement' },
  { label: 'Rewards Catalogue', href: '/rewards-catalog', group: 'Engagement' },
  { label: 'Suggestions', href: '/suggestions', group: 'Engagement' },
  { label: 'Polls', href: '/polls', group: 'Engagement' },
  { label: 'Pulse Check-in', href: '/pulse', group: 'Engagement' },
  { label: 'Engagement Heatmap', href: '/heatmap', group: 'Engagement' },
  { label: 'Wellness Programs', href: '/wellness', group: 'Engagement' },
  { label: 'Stay Interviews', href: '/stay-interviews', group: 'Engagement' },
  { label: 'Book a Desk', href: '/desk-booking', group: 'Workplace' },
  { label: 'Floor Plan', href: '/floor-plan', group: 'Workplace' },
  { label: 'Visitors', href: '/visitors', group: 'Workplace' },
  { label: 'Travel Requests', href: '/travel', group: 'Workplace' },
  { label: 'Travel Documents', href: '/travel-documents', group: 'Workplace' },
  { label: 'Assets', href: '/assets', group: 'Operational' },
  { label: 'Expenses', href: '/expenses', group: 'Operational' },
  { label: 'Advances', href: '/advances', group: 'Operational' },
  { label: 'Helpdesk', href: '/helpdesk', group: 'Operational' },
  { label: 'Knowledge Base', href: '/kb', group: 'Operational' },
  { label: 'Vendors', href: '/vendors', group: 'Operational' },
  { label: 'Workflows', href: '/workflows', group: 'Workflow' },
  { label: 'Workflow Designer', href: '/workflows/designer', group: 'Workflow' },
  { label: 'Forms', href: '/forms', group: 'Workflow' },
  { label: 'Form Builder', href: '/forms/builder', group: 'Workflow' },
  { label: 'Letters', href: '/letters', group: 'Documents' },
  { label: 'Template Editor', href: '/settings/templates', group: 'Documents' },
  { label: 'Audit Log', href: '/compliance', group: 'Compliance' },
  { label: 'HR Cases', href: '/cases', group: 'Compliance' },
  { label: 'GDPR / DPDP', href: '/gdpr', group: 'Compliance' },
  { label: 'Compliance Checklist', href: '/compliance-items', group: 'Compliance' },
  { label: 'Licenses', href: '/licenses', group: 'Compliance' },
  { label: 'Reports', href: '/reports', group: 'Insights' },
  { label: 'D&I Analytics', href: '/dei', group: 'Insights' },
  { label: 'Settings', href: '/settings', group: 'Admin' },
  { label: 'Holidays', href: '/holidays', group: 'Admin' },
  { label: 'Team Calendar', href: '/calendar', group: 'Admin' },
  { label: 'Shifts', href: '/shifts', group: 'Admin' },
  { label: 'Skills Library', href: '/skills', group: 'Admin' },
  { label: 'Company Policies', href: '/policies', group: 'Admin' },
]

interface Props { open: boolean; onClose: () => void }

export function CommandPalette({ open, onClose }: Props) {
  const navigate = useNavigate()
  const [q, setQ] = useState('')
  const [idx, setIdx] = useState(0)

  const filtered = useMemo(() => {
    if (!q.trim()) return NAV_TARGETS.slice(0, 12)
    const lower = q.toLowerCase()
    return NAV_TARGETS.filter(t =>
      t.label.toLowerCase().includes(lower) || t.group.toLowerCase().includes(lower)
    ).slice(0, 20)
  }, [q])

  useEffect(() => { setIdx(0) }, [q])

  useEffect(() => {
    if (!open) return
    const handler = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose()
      if (e.key === 'ArrowDown') { e.preventDefault(); setIdx(i => Math.min(filtered.length - 1, i + 1)) }
      if (e.key === 'ArrowUp')   { e.preventDefault(); setIdx(i => Math.max(0, i - 1)) }
      if (e.key === 'Enter')     { const t = filtered[idx]; if (t) { navigate(t.href); onClose() } }
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [open, filtered, idx, navigate, onClose])

  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 bg-black/40 backdrop-blur-sm flex items-start justify-center pt-24" onClick={onClose}>
      <div
        className="bg-white rounded-xl shadow-2xl w-full max-w-xl mx-4 overflow-hidden"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center gap-2 p-3 border-b">
          <Search className="h-4 w-4 text-slate-400" />
          <input
            autoFocus
            value={q}
            onChange={e => setQ(e.target.value)}
            className="flex-1 bg-transparent focus:outline-none text-sm"
            placeholder="Jump to anything... (esc to close)"
          />
          <button onClick={onClose} className="text-slate-400 hover:text-slate-700"><X className="h-4 w-4" /></button>
        </div>
        <div className="max-h-96 overflow-y-auto p-1">
          {filtered.length === 0 ? (
            <p className="p-6 text-sm text-slate-400 text-center">No matches</p>
          ) : (
            filtered.map((t, i) => (
              <button
                key={t.href}
                onMouseEnter={() => setIdx(i)}
                onClick={() => { navigate(t.href); onClose() }}
                className={`w-full flex items-center justify-between p-2 rounded text-sm text-left ${i === idx ? 'bg-violet-100 text-violet-800' : 'hover:bg-slate-50'}`}
              >
                <div>
                  <span className="font-medium">{t.label}</span>
                  <span className="text-xs text-slate-400 ml-2">{t.group}</span>
                </div>
                <ArrowRight className="h-3.5 w-3.5 text-slate-400" />
              </button>
            ))
          )}
        </div>
        <div className="border-t bg-slate-50 p-2 text-xs text-slate-500 flex items-center justify-between">
          <span>↑↓ navigate · ↵ open · esc close</span>
          <span>⌘K to summon</span>
        </div>
      </div>
    </div>
  )
}
