import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ClipboardList, Rocket, BarChart3 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { engagementService, type Survey } from '@/services/engagementService'
import { getErrorMessage } from '@/lib/api'
import { formatDate } from '@/lib/utils'
import { toast } from 'sonner'

const TYPES = ['ENGAGEMENT', 'ENPS', 'PULSE', 'EXIT', 'ONBOARDING', 'CUSTOM']

interface SurveyRow extends Survey, Record<string, unknown> { type?: string; startsOn?: string; endsOn?: string }

function ResultsDialog({ survey, onClose }: { survey: SurveyRow | null; onClose: () => void }) {
  const open = !!survey
  const responses = useQuery({
    queryKey: ['survey', survey?.id, 'responses'],
    queryFn: () => engagementService.surveyResponses(survey!.id) as Promise<Array<{ id: string; respondentId?: string; submittedAt?: string; answers?: Record<string, unknown> }>>,
    enabled: open,
  })
  const enps = useQuery({
    queryKey: ['survey', survey?.id, 'enps'],
    queryFn: () => engagementService.surveyEnps(survey!.id) as Promise<Record<string, unknown>>,
    enabled: open && survey?.type === 'ENPS',
  })

  return (
    <Dialog open={open} onOpenChange={o => { if (!o) onClose() }}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2"><BarChart3 className="h-5 w-5" /> {survey?.title} — results</DialogTitle>
        </DialogHeader>
        <DialogBody className="space-y-5">
          {survey?.type === 'ENPS' && (
            <div>
              <h4 className="text-sm font-semibold mb-2">eNPS</h4>
              {enps.isLoading ? <Skeleton className="h-16" /> : (
                <div className="flex flex-wrap gap-3">
                  {Object.entries(enps.data ?? {}).map(([k, v]) => (
                    <div key={k} className="rounded border px-3 py-2 text-center min-w-20">
                      <p className="text-lg font-semibold">{String(v)}</p>
                      <p className="text-xs text-slate-500 capitalize">{k}</p>
                    </div>
                  ))}
                  {(!enps.data || Object.keys(enps.data).length === 0) && <p className="text-sm text-slate-500">No eNPS data yet.</p>}
                </div>
              )}
            </div>
          )}
          <div>
            <h4 className="text-sm font-semibold mb-2">Responses ({responses.data?.length ?? 0})</h4>
            {responses.isLoading ? <Skeleton className="h-24" /> : (responses.data?.length ?? 0) === 0 ? (
              <p className="text-sm text-slate-500">No responses submitted yet.</p>
            ) : (
              <ul className="space-y-2">
                {responses.data!.map(r => (
                  <li key={r.id} className="rounded border p-2 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="text-slate-500">{r.respondentId ? r.respondentId.slice(0, 8) : 'Anonymous'}</span>
                      <span className="text-xs text-slate-400">{r.submittedAt ? formatDate(String(r.submittedAt)) : ''}</span>
                    </div>
                    {r.answers && <pre className="mt-1 text-xs text-slate-600 whitespace-pre-wrap">{JSON.stringify(r.answers, null, 1)}</pre>}
                  </li>
                ))}
              </ul>
            )}
          </div>
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

export function SurveysPage() {
  const qc = useQueryClient()
  const [viewing, setViewing] = useState<SurveyRow | null>(null)

  const launch = useMutation({
    mutationFn: (id: string) => engagementService.launchSurvey(id),
    onSuccess: () => { toast.success('Survey launched'); qc.invalidateQueries({ queryKey: ['surveys'] }) },
    onError: e => toast.error(getErrorMessage(e)),
  })

  return (
    <>
      <ResourcePage<SurveyRow>
        title="Surveys"
        description="Engagement, eNPS, pulse and exit surveys — create, launch, view results"
        icon={<ClipboardList className="h-10 w-10" />}
        queryKey={['surveys']}
        fetcher={() => engagementService.listSurveys() as Promise<SurveyRow[]>}
        filters={{ status: ['DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED'], type: TYPES }}
        columns={[
          { key: 'title', label: 'Title' },
          { key: 'type', label: 'Type', render: s => s.type ? <Badge variant="outline">{String(s.type)}</Badge> : '—' },
          { key: 'status', label: 'Status', render: s => <Badge variant={s.status === 'ACTIVE' ? 'success' : s.status === 'CLOSED' ? 'secondary' : 'warning'}>{String(s.status)}</Badge> },
          { key: 'endsOn', label: 'Ends', render: s => s.endsOn ? formatDate(String(s.endsOn)) : '—' },
        ]}
        formFields={[
          { name: 'title', label: 'Title', type: 'text', required: true, span: 2 },
          { name: 'type', label: 'Type', type: 'select', required: true, options: TYPES.map(t => ({ value: t, label: t })) },
          { name: 'isAnonymous', label: 'Anonymous', type: 'switch', defaultValue: true },
          { name: 'description', label: 'Description', type: 'textarea', span: 2 },
          { name: 'startsOn', label: 'Starts on', type: 'date' },
          { name: 'endsOn', label: 'Ends on', type: 'date' },
        ]}
        createTitle="Create survey"
        onCreate={v => engagementService.createSurvey({ ...v, status: 'DRAFT' } as Partial<Survey>)}
        rowActions={s => [
          { label: 'Launch', icon: <Rocket className="h-3.5 w-3.5" />, show: s.status === 'DRAFT', run: () => launch.mutateAsync(s.id) },
          { label: 'View results', icon: <BarChart3 className="h-3.5 w-3.5" />, run: () => { setViewing(s); return undefined } },
        ]}
      />
      <ResultsDialog survey={viewing} onClose={() => setViewing(null)} />
    </>
  )
}
