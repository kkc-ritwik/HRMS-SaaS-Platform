import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Target, ChevronDown, ChevronRight, RefreshCw } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Progress } from '@/components/ui/progress'
import { Skeleton } from '@/components/ui/skeleton'
import { Avatar } from '@/components/ui/avatar'
import { PageHeader } from '@/components/ui/page-header'
import { performanceService } from '@/services/performanceService'
import { Catalog } from '@/services/catalog'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'

interface GoalNode {
  id: string
  title: string
  ownerName?: string
  progress: number
  category?: string
  children?: GoalNode[]
}

function buildTree(goals: Array<{ id: string; parentId?: string | null; title: string; employeeName?: string; progress: number; category?: string }>): GoalNode[] {
  const map = new Map<string, GoalNode>()
  const roots: GoalNode[] = []
  goals.forEach(g => map.set(g.id, { id: g.id, title: g.title, ownerName: g.employeeName, progress: g.progress, category: g.category, children: [] }))
  goals.forEach(g => {
    const node = map.get(g.id)!
    if (g.parentId && map.has(g.parentId)) map.get(g.parentId)!.children!.push(node)
    else roots.push(node)
  })
  return roots
}

function Node({ n, level, onRollup, rollingUp }: { n: GoalNode; level: number; onRollup: (id: string) => void; rollingUp: string | null }) {
  const [open, setOpen] = useState(true)
  const has = (n.children?.length || 0) > 0
  return (
    <div className="space-y-1">
      <Card className="border-l-4" style={{ borderLeftColor: ['#7c3aed', '#3b82f6', '#10b981', '#f59e0b', '#ef4444'][level % 5] }}>
        <CardContent className="p-3 flex items-center gap-3">
          {has ? (
            <button onClick={() => setOpen(o => !o)}>
              {open ? <ChevronDown className="h-4 w-4" /> : <ChevronRight className="h-4 w-4" />}
            </button>
          ) : <div className="w-4" />}
          <Target className="h-4 w-4 text-violet-500 flex-shrink-0" />
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium truncate">{n.title}</p>
            <div className="flex items-center gap-2 mt-1">
              {n.ownerName && (
                <div className="flex items-center gap-1">
                  <Avatar name={n.ownerName} size="sm" />
                  <span className="text-xs text-slate-500">{n.ownerName}</span>
                </div>
              )}
              {n.category && <Badge>{n.category}</Badge>}
            </div>
          </div>
          <div className="w-32">
            <Progress value={n.progress} />
            <p className="text-xs text-right text-slate-500 mt-1">{n.progress}%</p>
          </div>
          {has && (
            <Button size="sm" variant="ghost" title="Recompute progress from child goals"
              loading={rollingUp === n.id} onClick={() => onRollup(n.id)}>
              <RefreshCw className="h-3.5 w-3.5" />
            </Button>
          )}
        </CardContent>
      </Card>
      {open && has && (
        <div className="ml-8 space-y-1 border-l-2 border-dashed border-slate-200 pl-4">
          {n.children!.map(c => <Node key={c.id} n={c} level={level + 1} onRollup={onRollup} rollingUp={rollingUp} />)}
        </div>
      )}
    </div>
  )
}

export function GoalCascadePage() {
  const qc = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['goal-cascade'], queryFn: () => performanceService.myGoals() })
  const flat = (data as Array<{ id: string; parentId?: string | null; title: string; employeeName?: string; progress: number; category?: string }>) || []
  const tree = buildTree(flat)

  const rollup = useMutation({
    mutationFn: (goalId: string) => Catalog.performance.goalCascade.rollup(goalId),
    onSuccess: () => { toast.success('Progress rolled up from child goals'); qc.invalidateQueries({ queryKey: ['goal-cascade'] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })
  const rollingUp = rollup.isPending ? (rollup.variables as string) : null

  return (
    <div className="space-y-6">
      <PageHeader title="Goal Cascade" description="Org → team → individual goal alignment tree — roll up progress from child goals" />
      {isLoading ? <Skeleton className="h-96" /> : tree.length === 0 ? (
        <Card><CardContent className="p-10 text-center text-slate-500">No goals to cascade</CardContent></Card>
      ) : (
        <div className="space-y-2">{tree.map(n => <Node key={n.id} n={n} level={0} onRollup={id => rollup.mutate(id)} rollingUp={rollingUp} />)}</div>
      )}
    </div>
  )
}
