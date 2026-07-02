import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Target, TrendingUp, History } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { FormDialog } from '@/components/ui/form-dialog'
import { performanceService } from '@/services/performanceService'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

interface Goal extends Record<string, unknown> {
  id: string; title: string; employeeName?: string; category?: string; status: string; progress: number; dueDate?: string; priority?: string
}
type View = 'me' | 'cycle'
const selectCls = 'h-9 rounded-md border border-slate-300 bg-white px-2 text-sm'

function HistoryDialog({ goal, onClose }: { goal: Goal | null; onClose: () => void }) {
  const open = !!goal
  const updates = useQuery({
    queryKey: ['goal-updates', goal?.id],
    queryFn: () => performanceService.goalUpdates(goal!.id) as Promise<Array<{ id: string; progress?: number; note?: string; createdAt?: string; createdBy?: string }>>,
    enabled: open,
  })
  return (
    <Dialog open={open} onOpenChange={o => { if (!o) onClose() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader><DialogTitle className="flex items-center gap-2"><History className="h-5 w-5" /> {goal?.title} — progress history</DialogTitle></DialogHeader>
        <DialogBody>
          {updates.isLoading ? <Skeleton className="h-24" /> : (updates.data?.length ?? 0) === 0 ? (
            <p className="text-sm text-slate-500">No progress updates recorded yet.</p>
          ) : (
            <ul className="border-l-2 border-slate-100 ml-2 space-y-3">
              {updates.data!.map(u => (
                <li key={u.id} className="ml-3 -translate-x-[7px]">
                  <span className="inline-block h-2.5 w-2.5 rounded-full bg-brand-500 mr-2" />
                  <span className="text-sm font-medium">{u.progress != null ? `${u.progress}%` : 'Update'}</span>
                  {u.note && <span className="text-sm text-slate-600"> — {u.note}</span>}
                  <span className="block text-xs text-slate-400 ml-4">{u.createdAt ? formatDate(String(u.createdAt), 'PPp') : ''}{u.createdBy ? ` · ${u.createdBy}` : ''}</span>
                </li>
              ))}
            </ul>
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

export function GoalsPage() {
  const qc = useQueryClient()
  const [progressFor, setProgressFor] = useState<Goal | null>(null)
  const [historyFor, setHistoryFor] = useState<Goal | null>(null)
  const [view, setView] = useState<View>('me')
  const [cycleId, setCycleId] = useState('')
  const cycles = useQuery({ queryKey: ['perf-cycles'], queryFn: performanceService.listCycles })
  const cycleList = (cycles.data as Array<{ id: string; name?: string }> | undefined) ?? []

  const fetcher = () => view === 'cycle'
    ? (cycleId ? performanceService.goalsForCycle(cycleId) : Promise.resolve([]))
    : performanceService.myGoals()

  const updateProgress = useMutation({
    mutationFn: (v: Record<string, unknown>) => performanceService.updateGoalProgress(String(progressFor?.id), Number(v.progress), String(v.note ?? '')),
    onSuccess: () => { toast.success('Progress updated'); qc.invalidateQueries({ queryKey: ['goals'] }); setProgressFor(null) },
  })

  return (
    <>
      <div className="flex flex-wrap items-center gap-3 mb-2">
        <Tabs value={view} onValueChange={v => setView(v as View)}>
          <TabsList>
            <TabsTrigger value="me">My goals</TabsTrigger>
            <TabsTrigger value="cycle">By cycle</TabsTrigger>
          </TabsList>
        </Tabs>
        {view === 'cycle' && (
          <select className={selectCls} value={cycleId} onChange={e => setCycleId(e.target.value)}>
            <option value="">Select cycle…</option>
            {cycleList.map(c => <option key={c.id} value={c.id}>{c.name || c.id.slice(0, 8)}</option>)}
          </select>
        )}
      </div>

      <ResourcePage<Goal>
        key={`${view}-${cycleId}`}
        title="Goals & OKRs"
        description="Track individual, team, and org goals"
        icon={<Target className="h-10 w-10" />}
        queryKey={['goals', view, cycleId]}
        fetcher={fetcher}
        filters={{ status: ['DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED'], category: ['INDIVIDUAL', 'TEAM', 'COMPANY'] }}
        columns={[
          { key: 'title', label: 'Title' },
          { key: 'employeeName', label: 'Owner' },
          { key: 'category', label: 'Type', render: g => g.category ? <Badge>{String(g.category)}</Badge> : '—' },
          { key: 'progress', label: 'Progress', render: g => <div className="flex items-center gap-2 min-w-[100px]"><Progress value={Number(g.progress ?? 0)} /><span className="text-xs">{Number(g.progress ?? 0)}%</span></div> },
          { key: 'priority', label: 'Priority', render: g => g.priority ? <Badge>{String(g.priority)}</Badge> : '—' },
          { key: 'status', label: 'Status', render: g => <Badge>{String(g.status)}</Badge> },
          { key: 'dueDate', label: 'Due' },
        ]}
        formFields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
          { name: 'category', label: 'Type', type: 'select', required: true, options: [
            { value: 'INDIVIDUAL', label: 'Individual' }, { value: 'TEAM', label: 'Team' }, { value: 'COMPANY', label: 'Company' },
          ] },
          { name: 'priority', label: 'Priority', type: 'select', options: [
            { value: 'LOW', label: 'Low' }, { value: 'MEDIUM', label: 'Medium' }, { value: 'HIGH', label: 'High' }, { value: 'CRITICAL', label: 'Critical' },
          ] },
          { name: 'startDate', label: 'Start date', type: 'date' },
          { name: 'endDate', label: 'End date', type: 'date' },
        ]}
        createTitle="Create goal"
        onCreate={v => performanceService.createGoal(v)}
        onUpdate={(id, v) => performanceService.updateGoal(id, v)}
        onDelete={id => performanceService.deleteGoal(id)}
        rowActions={g => [
          { label: 'Update progress', icon: <TrendingUp className="h-3.5 w-3.5" />, run: () => { setProgressFor(g); return Promise.resolve() } },
          { label: 'History', icon: <History className="h-3.5 w-3.5" />, run: () => { setHistoryFor(g); return Promise.resolve() } },
        ]}
      />
      <FormDialog
        open={!!progressFor} onOpenChange={o => !o && setProgressFor(null)}
        title={`Update progress — ${progressFor?.title ?? ''}`} submitLabel="Update"
        fields={[
          { name: 'progress', label: 'Progress (%)', type: 'number', required: true },
          { name: 'note', label: 'Update note', type: 'textarea', span: 2 },
        ]}
        onSubmit={v => updateProgress.mutateAsync(v)}
      />
      <HistoryDialog goal={historyFor} onClose={() => setHistoryFor(null)} />
    </>
  )
}
