import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Target } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { performanceService } from '@/services/performanceService'

interface Goal {
  id: string
  title: string
  employeeName?: string
  category?: string
  status: string
  progress: number
  dueDate?: string
  priority?: string
}

export function GoalsPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['goals', 'me'], queryFn: () => performanceService.myGoals() })
  const goals = (data as Goal[]) || []

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => performanceService.createGoal(v),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['goals'] }),
  })

  return (
    <>
      <DataList<Goal>
        title="Goals & OKRs" description="Track individual, team, and org goals"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Goal</Button>}
        data={goals} isLoading={isLoading}
        emptyIcon={<Target className="h-10 w-10" />} emptyTitle="No goals defined"
        filters={{ status: ['DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED'], category: ['INDIVIDUAL', 'TEAM', 'COMPANY'] }}
        columns={[
          { key: 'title', label: 'Title' },
          { key: 'employeeName', label: 'Owner' },
          { key: 'category', label: 'Type', render: g => g.category ? <Badge>{g.category}</Badge> : '—' },
          { key: 'progress', label: 'Progress', render: g => <div className="flex items-center gap-2 min-w-[100px]"><Progress value={g.progress ?? 0} /><span className="text-xs">{g.progress ?? 0}%</span></div> },
          { key: 'priority', label: 'Priority', render: g => g.priority ? <Badge>{g.priority}</Badge> : '—' },
          { key: 'status', label: 'Status', render: g => <Badge>{g.status}</Badge> },
          { key: 'dueDate', label: 'Due' },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Create goal"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
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
      />
    </>
  )
}
