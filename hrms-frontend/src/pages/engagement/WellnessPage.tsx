import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Heart, Trophy, Medal } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { wellnessService } from '@/services/extendedServices'

interface WellnessProgram extends Record<string, unknown> { id: string; code?: string; name: string; category?: string; goalMetric?: string; goalTarget?: number; active?: boolean }
interface LeaderRow { employeeId?: string; employeeName?: string; total?: number; rank?: number }

function isoDaysAgo(days: number) { const d = new Date(); d.setDate(d.getDate() - days); return d.toISOString().slice(0, 10) }

function LeaderboardDialog({ program, onClose }: { program: WellnessProgram | null; onClose: () => void }) {
  const open = !!program
  const board = useQuery({
    queryKey: ['wellness-leaderboard', program?.id],
    queryFn: () => wellnessService.leaderboard(program!.id, isoDaysAgo(30), isoDaysAgo(0)) as Promise<LeaderRow[]>,
    enabled: open,
  })
  const rows = (board.data as LeaderRow[] | undefined) ?? []
  return (
    <Dialog open={open} onOpenChange={o => { if (!o) onClose() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader><DialogTitle className="flex items-center gap-2"><Trophy className="h-5 w-5" /> {program?.name} — leaderboard (30 days)</DialogTitle></DialogHeader>
        <DialogBody>
          {board.isLoading ? <Skeleton className="h-32" /> : rows.length === 0 ? (
            <p className="text-sm text-slate-500">No activity logged for this program yet.</p>
          ) : (
            <ol className="space-y-1">
              {rows.map((r, i) => (
                <li key={r.employeeId ?? i} className="flex items-center justify-between rounded border p-2">
                  <span className="flex items-center gap-2 text-sm">
                    {i < 3 ? <Medal className={`h-4 w-4 ${['text-amber-400', 'text-slate-400', 'text-orange-400'][i]}`} /> : <span className="w-4 text-center text-xs text-slate-400">{r.rank ?? i + 1}</span>}
                    {r.employeeName ?? r.employeeId?.slice(0, 8) ?? '—'}
                  </span>
                  <span className="text-sm font-medium">{Number(r.total ?? 0).toLocaleString()}</span>
                </li>
              ))}
            </ol>
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

export function WellnessPage() {
  const [leaderboardFor, setLeaderboardFor] = useState<WellnessProgram | null>(null)
  return (
    <>
      <ResourcePage<WellnessProgram>
        title="Wellness Programs"
        description="Step challenges, meditation streaks, fitness goals"
        icon={<Heart className="h-10 w-10" />}
        queryKey={['wellness']}
        fetcher={() => wellnessService.programs()}
        filters={{ category: ['FITNESS', 'MENTAL_HEALTH', 'NUTRITION', 'FINANCIAL', 'SOCIAL'] }}
        columns={[
          { key: 'name', label: 'Name' },
          { key: 'category', label: 'Category', render: p => p.category ? <Badge>{String(p.category)}</Badge> : '—' },
          { key: 'goalMetric', label: 'Metric' },
          { key: 'goalTarget', label: 'Target', align: 'right' },
          { key: 'active', label: 'Active', render: p => <Badge variant={p.active === false ? 'secondary' : 'success'}>{p.active === false ? 'Inactive' : 'Active'}</Badge> },
        ]}
        formFields={[
          { name: 'name', label: 'Program name', type: 'text', required: true, span: 2 },
          { name: 'category', label: 'Category', type: 'select', required: true, options: [
            { value: 'FITNESS', label: 'Fitness' }, { value: 'MENTAL_HEALTH', label: 'Mental health' },
            { value: 'NUTRITION', label: 'Nutrition' }, { value: 'FINANCIAL', label: 'Financial' }, { value: 'SOCIAL', label: 'Social' },
          ] },
          { name: 'goalMetric', label: 'Goal metric', type: 'text', helper: 'e.g. steps, minutes' },
          { name: 'goalTarget', label: 'Goal target', type: 'number' },
          { name: 'startDate', label: 'Start date', type: 'date' },
          { name: 'endDate', label: 'End date', type: 'date' },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
        ]}
        onCreate={v => wellnessService.createProgram(v)}
        rowActions={p => [
          { label: 'Leaderboard', icon: <Trophy className="h-3.5 w-3.5" />, run: () => { setLeaderboardFor(p); return undefined } },
        ]}
      />
      <LeaderboardDialog program={leaderboardFor} onClose={() => setLeaderboardFor(null)} />
    </>
  )
}
