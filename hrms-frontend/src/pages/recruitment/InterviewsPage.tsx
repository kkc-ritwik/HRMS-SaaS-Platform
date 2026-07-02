import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Calendar, Users, Trash2, Star } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Skeleton } from '@/components/ui/skeleton'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { recruitmentService } from '@/services/recruitmentService'
import { getErrorMessage } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

const TYPES = ['PHONE_SCREEN', 'TECHNICAL', 'HR', 'PANEL', 'FINAL', 'CASE_STUDY']
const MODES = ['IN_PERSON', 'VIDEO', 'PHONE']
const STATUSES = ['SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW']
const ROLES = ['LEAD', 'PANELIST', 'SHADOW']
const RECOMMENDATIONS = ['STRONGLY_HIRE', 'HIRE', 'NEUTRAL', 'NO_HIRE', 'STRONG_NO_HIRE']

interface Interview extends Record<string, unknown> {
  id: string; applicationId?: string; interviewType?: string; roundNumber?: number
  scheduledAt?: string; mode?: string; meetingLink?: string; status: string
}
interface Panelist { id: string; interviewerId: string; role: string; rating?: number; feedback?: string; submittedAt?: string }
interface InterviewDetail extends Interview {
  durationMinutes?: number; venue?: string; overallRating?: number; recommendation?: string; feedback?: string
  panelists?: Panelist[]
}

const selectCls = 'h-9 rounded-md border border-slate-300 bg-white px-2 text-sm'

function PanelDialog({ interviewId, onClose }: { interviewId: string | null; onClose: () => void }) {
  const qc = useQueryClient()
  const open = !!interviewId
  const detail = useQuery({
    queryKey: ['interview', interviewId],
    queryFn: () => recruitmentService.getInterview(interviewId!) as Promise<InterviewDetail>,
    enabled: open,
  })
  const iv = detail.data

  const [newPanelist, setNewPanelist] = useState({ interviewerId: '', role: 'PANELIST' })
  const [fb, setFb] = useState<Record<string, { rating: number; feedback: string }>>({})
  const [decision, setDecision] = useState({ overallRating: 3, recommendation: 'HIRE', feedback: '' })

  const invalidate = () => {
    qc.invalidateQueries({ queryKey: ['interview', interviewId] })
    qc.invalidateQueries({ queryKey: ['interviews'] })
  }
  const onErr = (e: unknown) => toast.error(getErrorMessage(e))

  const setStatus = useMutation({
    mutationFn: (status: string) => recruitmentService.setInterviewStatus(interviewId!, status),
    onSuccess: () => { toast.success('Status updated'); invalidate() }, onError: onErr,
  })
  const addPanelist = useMutation({
    mutationFn: () => recruitmentService.addPanelist(interviewId!, newPanelist),
    onSuccess: () => { toast.success('Panelist added'); setNewPanelist({ interviewerId: '', role: 'PANELIST' }); invalidate() }, onError: onErr,
  })
  const removePanelist = useMutation({
    mutationFn: (pid: string) => recruitmentService.removePanelist(interviewId!, pid),
    onSuccess: () => { toast.success('Panelist removed'); invalidate() }, onError: onErr,
  })
  const panelistFeedback = useMutation({
    mutationFn: (p: { pid: string; rating: number; feedback: string }) =>
      recruitmentService.submitPanelistFeedback(interviewId!, p.pid, { rating: p.rating, feedback: p.feedback }),
    onSuccess: () => { toast.success('Feedback recorded'); invalidate() }, onError: onErr,
  })
  const submitDecision = useMutation({
    mutationFn: () => recruitmentService.submitInterviewFeedback(interviewId!, decision),
    onSuccess: () => { toast.success('Decision recorded'); invalidate() }, onError: onErr,
  })

  return (
    <Dialog open={open} onOpenChange={(o) => { if (!o) onClose() }}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2"><Users className="h-5 w-5" /> Interview panel</DialogTitle>
        </DialogHeader>
        <DialogBody className="space-y-5">
          {detail.isLoading || !iv ? <Skeleton className="h-40" /> : (
            <>
              <div className="flex flex-wrap items-center gap-2 text-sm">
                <Badge>{String(iv.interviewType ?? '—')}</Badge>
                <span className="text-slate-500">Round {iv.roundNumber ?? 1}</span>
                {iv.scheduledAt && <span className="text-slate-500">· {formatDate(String(iv.scheduledAt), 'PPp')}</span>}
                <div className="ml-auto flex items-center gap-2">
                  <Label className="text-xs">Status</Label>
                  <select className={selectCls} value={iv.status} onChange={e => setStatus.mutate(e.target.value)}>
                    {STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
              </div>

              {/* Panelists */}
              <div className="space-y-2">
                <h4 className="text-sm font-semibold">Panelists ({iv.panelists?.length ?? 0})</h4>
                {(iv.panelists ?? []).length === 0 && <p className="text-xs text-slate-500">No panelists yet.</p>}
                {(iv.panelists ?? []).map(p => (
                  <div key={p.id} className="rounded border p-3 space-y-2">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium">{p.interviewerId.slice(0, 8)} <Badge variant="outline">{p.role}</Badge></p>
                        {p.rating != null
                          ? <p className="text-xs text-slate-500 flex items-center gap-1"><Star className="h-3 w-3 fill-amber-400 text-amber-400" /> {p.rating}/5 · {p.feedback || 'no notes'}</p>
                          : <p className="text-xs text-amber-600">Feedback pending</p>}
                      </div>
                      <Button size="sm" variant="ghost" onClick={() => removePanelist.mutate(p.id)}><Trash2 className="h-3.5 w-3.5" /></Button>
                    </div>
                    {p.rating == null && (
                      <div className="flex items-end gap-2">
                        <div>
                          <Label className="text-xs">Rating</Label>
                          <select className={selectCls} value={fb[p.id]?.rating ?? 3}
                            onChange={e => setFb(s => ({ ...s, [p.id]: { rating: Number(e.target.value), feedback: s[p.id]?.feedback ?? '' } }))}>
                            {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                          </select>
                        </div>
                        <Input className="flex-1" placeholder="Notes" value={fb[p.id]?.feedback ?? ''}
                          onChange={e => setFb(s => ({ ...s, [p.id]: { rating: s[p.id]?.rating ?? 3, feedback: e.target.value } }))} />
                        <Button size="sm" loading={panelistFeedback.isPending}
                          onClick={() => panelistFeedback.mutate({ pid: p.id, rating: fb[p.id]?.rating ?? 3, feedback: fb[p.id]?.feedback ?? '' })}>Save</Button>
                      </div>
                    )}
                  </div>
                ))}
                {/* Add panelist */}
                <div className="flex items-end gap-2 pt-1">
                  <div className="flex-1">
                    <Label className="text-xs">Interviewer ID</Label>
                    <Input placeholder="employee UUID" value={newPanelist.interviewerId}
                      onChange={e => setNewPanelist(s => ({ ...s, interviewerId: e.target.value }))} />
                  </div>
                  <div>
                    <Label className="text-xs">Role</Label>
                    <select className={selectCls} value={newPanelist.role} onChange={e => setNewPanelist(s => ({ ...s, role: e.target.value }))}>
                      {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                    </select>
                  </div>
                  <Button loading={addPanelist.isPending} disabled={!newPanelist.interviewerId.trim()} onClick={() => addPanelist.mutate()}>Add</Button>
                </div>
              </div>

              {/* Overall decision */}
              <div className="space-y-2 border-t pt-4">
                <h4 className="text-sm font-semibold">Overall decision</h4>
                {iv.recommendation && <p className="text-xs text-slate-500">Current: <Badge>{iv.recommendation}</Badge> {iv.overallRating ? `· ${iv.overallRating}/5` : ''}</p>}
                <div className="flex items-end gap-2">
                  <div>
                    <Label className="text-xs">Rating</Label>
                    <select className={selectCls} value={decision.overallRating} onChange={e => setDecision(s => ({ ...s, overallRating: Number(e.target.value) }))}>
                      {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
                    </select>
                  </div>
                  <div>
                    <Label className="text-xs">Recommendation</Label>
                    <select className={selectCls} value={decision.recommendation} onChange={e => setDecision(s => ({ ...s, recommendation: e.target.value }))}>
                      {RECOMMENDATIONS.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
                    </select>
                  </div>
                </div>
                <Textarea placeholder="Summary feedback" value={decision.feedback} onChange={e => setDecision(s => ({ ...s, feedback: e.target.value }))} />
                <Button loading={submitDecision.isPending} onClick={() => submitDecision.mutate()}>Record decision</Button>
              </div>
            </>
          )}
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

export function InterviewsPage() {
  const [managing, setManaging] = useState<string | null>(null)
  return (
    <>
      <ResourcePage<Interview>
        title="Interviews"
        description="Scheduled, completed and upcoming interviews — schedule, manage panel & feedback"
        icon={<Calendar className="h-10 w-10" />}
        queryKey={['interviews']}
        fetcher={() => recruitmentService.listInterviews()}
        filters={{ status: STATUSES, mode: MODES }}
        columns={[
          { key: 'scheduledAt', label: 'When', render: i => i.scheduledAt ? formatDate(String(i.scheduledAt), 'PPp') : '—' },
          { key: 'interviewType', label: 'Type', render: i => i.interviewType ? <Badge>{String(i.interviewType)}</Badge> : '—' },
          { key: 'roundNumber', label: 'Round', render: i => String(i.roundNumber ?? 1) },
          { key: 'mode', label: 'Mode', render: i => i.mode ? <Badge variant="outline">{String(i.mode)}</Badge> : '—' },
          { key: 'status', label: 'Status', render: i => <Badge>{String(i.status)}</Badge> },
          { key: 'meetingLink', label: '', render: i => i.meetingLink ? <a className="text-brand-600 hover:underline" href={String(i.meetingLink)} target="_blank" rel="noreferrer" onClick={e => e.stopPropagation()}>Join</a> : null },
        ]}
        formFields={[
          { name: 'applicationId', label: 'Application ID', type: 'text', required: true, span: 2 },
          { name: 'interviewType', label: 'Type', type: 'select', required: true, options: TYPES.map(t => ({ value: t, label: t.replace(/_/g, ' ') })) },
          { name: 'roundNumber', label: 'Round', type: 'number' },
          { name: 'scheduledAt', label: 'Scheduled at', type: 'datetime-local', required: true },
          { name: 'durationMinutes', label: 'Duration (min)', type: 'number' },
          { name: 'mode', label: 'Mode', type: 'select', required: true, options: MODES.map(m => ({ value: m, label: m.replace(/_/g, ' ') })) },
          { name: 'meetingLink', label: 'Meeting link', type: 'url' },
          { name: 'venue', label: 'Venue', type: 'text' },
        ]}
        createTitle="Schedule interview"
        onCreate={v => recruitmentService.scheduleInterview({
          ...v,
          roundNumber: v.roundNumber ? Number(v.roundNumber) : 1,
          durationMinutes: v.durationMinutes ? Number(v.durationMinutes) : 60,
          scheduledAt: v.scheduledAt ? new Date(String(v.scheduledAt)).toISOString() : undefined,
        })}
        rowActions={i => [
          { label: 'Manage panel', icon: <Users className="h-3.5 w-3.5" />, run: () => { setManaging(i.id); return undefined } },
        ]}
      />
      <PanelDialog interviewId={managing} onClose={() => setManaging(null)} />
    </>
  )
}
