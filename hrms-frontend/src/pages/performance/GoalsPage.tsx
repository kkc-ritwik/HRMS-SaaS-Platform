import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Target, TrendingUp } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { ResourcePage } from '@/components/ui/resource-page'
import { FormDialog } from '@/components/ui/form-dialog'
import { performanceService } from '@/services/performanceService'
import { toast } from 'sonner'

interface Goal extends Record<string, unknown> {
  id: string; title: string; employeeName?: string; category?: string; status: string; progress: number; dueDate?: string; priority?: string
}

export function GoalsPage() {
  const qc = useQueryClient()
  const [progressFor, setProgressFor] = useState<Goal | null>(null)

  const updateProgress = useMutation({
    mutationFn: (v: Record<string, unknown>) => performanceService.updateGoalProgress(String(progressFor?.id), Number(v.progress), String(v.note ?? '')),
    onSuccess: () => { toast.success('Progress updated'); qc.invalidateQueries({ queryKey: ['goals'] }); setProgressFor(null) },
  })

  return (
    <>
      <ResourcePage<Goal>
        title="Goals & OKRs"
        description="Track individual, team, and org goals"
        icon={<Target className="h-10 w-10" />}
        queryKey={['goals', 'me']}
        fetcher={() => performanceService.myGoals()}
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
    </>
  )
}
