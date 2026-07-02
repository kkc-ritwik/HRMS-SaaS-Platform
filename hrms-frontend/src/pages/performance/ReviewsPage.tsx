import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Star, Send, CheckCircle2, SlidersHorizontal } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody } from '@/components/ui/dialog'
import { ResourcePage } from '@/components/ui/resource-page'
import { performanceService, type Review } from '@/services/performanceService'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'

type View = 'me' | 'pending' | 'cycle'
const selectCls = 'h-9 rounded-md border border-slate-300 bg-white px-2 text-sm'

function CalibrateDialog({ review, onClose }: { review: (Review & Record<string, unknown>) | null; onClose: () => void }) {
  const qc = useQueryClient()
  const open = !!review
  const [form, setForm] = useState({ overallRating: '', performanceRating: '', potentialRating: '3', finalComments: '' })

  const calibrate = useMutation({
    mutationFn: () => performanceService.calibrateReview(review!.id, {
      overallRating: form.overallRating ? Number(form.overallRating) : undefined,
      performanceRating: form.performanceRating ? Number(form.performanceRating) : undefined,
      potentialRating: form.potentialRating ? Number(form.potentialRating) : undefined,
      finalComments: form.finalComments || undefined,
    }),
    onSuccess: () => { toast.success('Review calibrated'); qc.invalidateQueries({ queryKey: ['reviews'] }); onClose() },
    onError: e => toast.error(getErrorMessage(e)),
  })

  return (
    <Dialog open={open} onOpenChange={o => { if (!o) onClose() }}>
      <DialogContent className="max-w-md">
        <DialogHeader><DialogTitle className="flex items-center gap-2"><SlidersHorizontal className="h-5 w-5" /> Calibrate review</DialogTitle></DialogHeader>
        <DialogBody className="space-y-3">
          <p className="text-sm text-slate-500">Adjust final ratings for {review?.employeeName || 'this review'} and record calibration notes.</p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <Label className="text-xs">Overall rating (1–5)</Label>
              <Input type="number" min={1} max={5} step="0.1" value={form.overallRating} onChange={e => setForm(s => ({ ...s, overallRating: e.target.value }))} />
            </div>
            <div>
              <Label className="text-xs">Performance rating (1–5)</Label>
              <Input type="number" min={1} max={5} step="0.1" value={form.performanceRating} onChange={e => setForm(s => ({ ...s, performanceRating: e.target.value }))} />
            </div>
          </div>
          <div>
            <Label className="text-xs">Potential rating</Label>
            <select className={selectCls} value={form.potentialRating} onChange={e => setForm(s => ({ ...s, potentialRating: e.target.value }))}>
              {[1, 2, 3, 4, 5].map(n => <option key={n} value={n}>{n}</option>)}
            </select>
          </div>
          <div>
            <Label className="text-xs">Final comments</Label>
            <Textarea value={form.finalComments} onChange={e => setForm(s => ({ ...s, finalComments: e.target.value }))} />
          </div>
          <Button onClick={() => calibrate.mutate()} loading={calibrate.isPending}>Calibrate &amp; finalize</Button>
        </DialogBody>
      </DialogContent>
    </Dialog>
  )
}

export function ReviewsPage() {
  const [view, setView] = useState<View>('me')
  const [cycleId, setCycleId] = useState('')
  const [calibrating, setCalibrating] = useState<(Review & Record<string, unknown>) | null>(null)
  const cycles = useQuery({ queryKey: ['perf-cycles'], queryFn: performanceService.listCycles })
  const cycleList = (cycles.data as Array<{ id: string; name?: string }> | undefined) ?? []

  const fetcher = () => {
    if (view === 'pending') return performanceService.myPendingReviews()
    if (view === 'cycle') return cycleId ? performanceService.reviewsForCycle(cycleId) : Promise.resolve([] as Review[])
    return performanceService.myReviews()
  }

  return (
    <>
      <div className="flex flex-wrap items-center gap-3 mb-2">
        <Tabs value={view} onValueChange={v => setView(v as View)}>
          <TabsList>
            <TabsTrigger value="me">My reviews</TabsTrigger>
            <TabsTrigger value="pending">Pending on me</TabsTrigger>
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

      <ResourcePage<Review & Record<string, unknown>>
        key={`${view}-${cycleId}`}
        title="Performance Reviews"
        description="Self-rate, submit, acknowledge — and calibrate as HR"
        icon={<Star className="h-10 w-10" />}
        queryKey={['reviews', view, cycleId]}
        fetcher={fetcher}
        filters={{ status: ['PENDING', 'SELF_REVIEW', 'MANAGER_REVIEW', 'CALIBRATION', 'COMPLETED', 'ACKNOWLEDGED'] }}
        columns={[
          { key: 'title', label: 'Cycle' },
          { key: 'employeeName', label: 'Employee' },
          { key: 'reviewerName', label: 'Reviewer' },
          { key: 'period', label: 'Period' },
          { key: 'overallRating', label: 'Rating', render: r => r.overallRating ? `${r.overallRating}/5` : '—' },
          { key: 'status', label: 'Status', render: r => <Badge>{String(r.status)}</Badge> },
        ]}
        rowActions={r => {
          const status = String(r.status)
          return [
            {
              label: 'Submit self-rating', icon: <Send className="h-3.5 w-3.5" />,
              show: status === 'PENDING' || status === 'SELF_REVIEW' || status === 'SCHEDULED',
              run: () => performanceService.submitReview(r.id, { selfRating: Number(r.overallRating ?? 3) }),
            },
            {
              label: 'Calibrate', icon: <SlidersHorizontal className="h-3.5 w-3.5" />,
              show: status === 'MANAGER_REVIEW' || status === 'CALIBRATION',
              run: () => { setCalibrating(r); return undefined },
            },
            {
              label: 'Acknowledge', icon: <CheckCircle2 className="h-3.5 w-3.5" />,
              show: status === 'COMPLETED',
              run: () => performanceService.acknowledgeReview(r.id),
            },
          ]
        }}
      />
      <CalibrateDialog review={calibrating} onClose={() => setCalibrating(null)} />
    </>
  )
}
