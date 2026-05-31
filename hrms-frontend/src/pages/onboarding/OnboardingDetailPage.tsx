import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Check, Circle, FileText, ClipboardCheck } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Progress } from '@/components/ui/progress'
import { onboardingService, type OnboardingWorkflow, type OnboardingTask } from '@/services/onboardingService'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'

export function OnboardingDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const wf = useQuery({ queryKey: ['onboarding', id], queryFn: () => onboardingService.getWorkflow(id), enabled: !!id })
  const tasks = useQuery({ queryKey: ['onboarding-tasks', id], queryFn: () => onboardingService.listTasks(id), enabled: !!id })

  const complete = useMutation({
    mutationFn: (taskId: string) => onboardingService.completeTask(taskId),
    onSuccess: () => { toast.success('Task completed'); qc.invalidateQueries({ queryKey: ['onboarding-tasks', id] }) },
  })

  if (wf.isLoading) return <Skeleton className="h-96" />
  const w = wf.data as OnboardingWorkflow
  if (!w) return <p>Workflow not found</p>
  const taskList = (tasks.data as OnboardingTask[]) || []

  const byCategory = taskList.reduce<Record<string, OnboardingTask[]>>((acc, t) => {
    const cat = t.category || 'Other'
    if (!acc[cat]) acc[cat] = []
    acc[cat].push(t)
    return acc
  }, {})

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/onboarding')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <Card>
        <CardContent className="p-6">
          <div className="flex items-start justify-between">
            <div>
              <h1 className="text-2xl font-bold">Onboarding · {w.employeeId.slice(0, 8)}…</h1>
              <p className="text-sm text-slate-500 mt-1">Started {w.startDate} · Expected {w.expectedCompletion || 'TBD'}</p>
            </div>
            <Badge>{w.status}</Badge>
          </div>
          <div className="mt-4">
            <div className="flex justify-between text-xs text-slate-500 mb-1">
              <span>Progress</span><span>{w.progressPercent}%</span>
            </div>
            <Progress value={w.progressPercent} />
          </div>
        </CardContent>
      </Card>

      {Object.entries(byCategory).map(([cat, items]) => (
        <Card key={cat}>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><ClipboardCheck className="h-5 w-5" /> {cat}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {items.map(t => (
              <div key={t.id} className={cn('flex items-start gap-3 p-3 rounded-lg border', t.status === 'COMPLETED' ? 'bg-green-50 border-green-200' : 'border-slate-100')}>
                <button onClick={() => t.status !== 'COMPLETED' && complete.mutate(t.id)} className="mt-0.5">
                  {t.status === 'COMPLETED'
                    ? <div className="h-5 w-5 rounded-full bg-green-500 text-white flex items-center justify-center"><Check className="h-3 w-3" /></div>
                    : <Circle className="h-5 w-5 text-slate-300 hover:text-violet-500" />}
                </button>
                <div className="flex-1">
                  <p className={cn('text-sm font-medium', t.status === 'COMPLETED' && 'line-through text-slate-500')}>{t.title}</p>
                  {t.description && <p className="text-xs text-slate-500 mt-0.5">{t.description}</p>}
                  <div className="flex items-center gap-3 mt-1 text-xs text-slate-400">
                    {t.assigneeRole && <span>👤 {t.assigneeRole}</span>}
                    {t.dueDate && <span>📅 Due {t.dueDate}</span>}
                    {t.documentUri && <a className="text-violet-600 hover:underline flex items-center gap-1" href={t.documentUri} target="_blank" rel="noreferrer"><FileText className="h-3 w-3" />Document</a>}
                  </div>
                </div>
                <Badge>{t.status}</Badge>
              </div>
            ))}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
