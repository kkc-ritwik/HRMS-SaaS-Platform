import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { BarChart3, Vote } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { engagementService } from '@/services/engagementService'
import { getErrorMessage } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

interface Poll extends Record<string, unknown> {
  id: string; question: string; options?: string[]; totalVotes?: number; isActive?: boolean; closesAt?: string
}

function VoteDialog({ poll, onClose }: { poll: Poll | null; onClose: () => void }) {
  const qc = useQueryClient()
  const open = !!poll
  const options = (poll?.options as string[] | undefined) ?? []
  const tally = useQuery({
    queryKey: ['poll', poll?.id, 'tally'],
    queryFn: () => engagementService.pollTally(poll!.id) as Promise<Record<number, number>>,
    enabled: open,
  })
  const totals = tally.data ?? {}
  const grand = Object.values(totals).reduce((a, b) => a + Number(b), 0)

  const vote = useMutation({
    mutationFn: (idx: number) => engagementService.votePoll(poll!.id, [idx]),
    onSuccess: () => { toast.success('Vote recorded'); qc.invalidateQueries({ queryKey: ['poll', poll?.id, 'tally'] }); qc.invalidateQueries({ queryKey: ['polls'] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })

  return (
    <Dialog open={open} onOpenChange={o => { if (!o) onClose() }}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2"><Vote className="h-5 w-5" /> {poll?.question}</DialogTitle>
        </DialogHeader>
        <DialogBody className="space-y-3">
          {tally.isLoading ? <Skeleton className="h-32" /> : options.length === 0 ? (
            <p className="text-sm text-slate-500">This poll has no options.</p>
          ) : options.map((opt, idx) => {
            const count = Number(totals[idx] ?? 0)
            const pct = grand > 0 ? Math.round((count / grand) * 100) : 0
            return (
              <div key={idx} className="space-y-1">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-sm">{opt}</span>
                  <div className="flex items-center gap-2">
                    <span className="text-xs text-slate-500">{count} · {pct}%</span>
                    <Button size="sm" variant="outline" loading={vote.isPending} onClick={() => vote.mutate(idx)}>Vote</Button>
                  </div>
                </div>
                <div className="h-2 rounded bg-slate-100 overflow-hidden">
                  <div className="h-full bg-brand-500" style={{ width: `${pct}%` }} />
                </div>
              </div>
            )
          })}
          <p className="text-xs text-slate-400 pt-1">{grand} total vote{grand === 1 ? '' : 's'}</p>
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

export function PollsPage() {
  const [active, setActive] = useState<Poll | null>(null)
  return (
    <>
      <ResourcePage<Poll>
        title="Polls"
        description="Quick pulse polls — create, vote, tally"
        icon={<BarChart3 className="h-10 w-10" />}
        queryKey={['polls']}
        fetcher={() => engagementService.listPolls() as Promise<Poll[]>}
        columns={[
          { key: 'question', label: 'Question' },
          { key: 'totalVotes', label: 'Votes', align: 'right', render: p => String(p.totalVotes ?? 0) },
          { key: 'closesAt', label: 'Closes', render: p => p.closesAt ? formatDate(String(p.closesAt)) : '—' },
          { key: 'isActive', label: 'State', render: p => <Badge variant={p.isActive ? 'success' : 'secondary'}>{p.isActive ? 'Open' : 'Closed'}</Badge> },
        ]}
        formFields={[
          { name: 'question', label: 'Question', type: 'text', required: true, span: 2 },
          { name: 'options', label: 'Options (comma-separated)', type: 'text', required: true, span: 2, helper: 'e.g. Yes, No, Maybe' },
          { name: 'isAnonymous', label: 'Anonymous', type: 'switch', defaultValue: true },
          { name: 'closesAt', label: 'Closes at', type: 'date' },
        ]}
        onCreate={v => {
          const opts = String(v.options ?? '').split(',').map(s => s.trim()).filter(Boolean)
          return engagementService.createPoll({ ...v, options: opts } as never)
        }}
        rowActions={p => [
          { label: 'Vote / tally', icon: <Vote className="h-3.5 w-3.5" />, run: () => { setActive(p); return undefined } },
        ]}
      />
      <VoteDialog poll={active} onClose={() => setActive(null)} />
    </>
  )
}
