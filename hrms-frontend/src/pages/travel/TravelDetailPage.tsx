import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Plane, MapPin, Banknote, Car, Send, Check, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { CrudSection } from '@/components/ui/crud-section'
import { FormDialog } from '@/components/ui/form-dialog'
import { travelService, type TripRequest } from '@/services/travelService'
import { useAuthStore } from '@/store/authStore'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function TravelDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  const [advanceOpen, setAdvanceOpen] = useState(false)
  const [mileageOpen, setMileageOpen] = useState(false)

  const tripQ = useQuery({ queryKey: ['trip', id], queryFn: () => travelService.getTrip(id), enabled: !!id })
  const itinQ = useQuery({ queryKey: ['trip-itinerary', id], queryFn: () => travelService.itinerary(id), enabled: !!id })
  const trip = (tripQ.data ?? {}) as AnyObj
  const status = String(trip.status ?? '')
  const invalidate = () => qc.invalidateQueries({ queryKey: ['trip', id] })

  const act = useMutation({
    mutationFn: ({ kind }: { kind: 'submit' | 'approve' | 'reject' }) =>
      kind === 'submit' ? travelService.submitTrip(id) : kind === 'approve' ? travelService.approveTrip(id, empId) : travelService.rejectTrip(id, 'Rejected'),
    onSuccess: () => { toast.success('Done'); invalidate() },
  })
  const advance = useMutation({ mutationFn: (v: Record<string, unknown>) => travelService.requestAdvance({ ...v, tripId: id }), onSuccess: () => { toast.success('Advance requested'); setAdvanceOpen(false) } })
  const mileage = useMutation({ mutationFn: (v: Record<string, unknown>) => travelService.claimMileage({ ...v, tripId: id }), onSuccess: () => { toast.success('Mileage logged'); setMileageOpen(false) } })

  return (
    <div className="space-y-5 animate-fade-in">
      <Button variant="ghost" size="sm" onClick={() => navigate('/travel')}><ArrowLeft className="h-4 w-4 mr-1" /> Back to trips</Button>
      <PageHeader
        title={tripQ.isLoading ? 'Loading…' : `${String(trip.purpose ?? 'Trip')} — ${String(trip.fromLocation ?? '')} → ${String(trip.toLocation ?? '')}`}
        description="Travel request detail"
        action={
          <div className="flex gap-2">
            {status === 'DRAFT' && <Button size="sm" onClick={() => act.mutate({ kind: 'submit' })}><Send className="h-4 w-4 mr-1" /> Submit</Button>}
            {status === 'SUBMITTED' && <>
              <Button size="sm" onClick={() => act.mutate({ kind: 'approve' })}><Check className="h-4 w-4 mr-1" /> Approve</Button>
              <Button size="sm" variant="outline" onClick={() => act.mutate({ kind: 'reject' })}><X className="h-4 w-4 mr-1" /> Reject</Button>
            </>}
            <Button size="sm" variant="outline" onClick={() => setAdvanceOpen(true)}><Banknote className="h-4 w-4 mr-1" /> Advance</Button>
            <Button size="sm" variant="outline" onClick={() => setMileageOpen(true)}><Car className="h-4 w-4 mr-1" /> Mileage</Button>
          </div>
        }
      />

      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        {tripQ.isLoading ? Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-20 rounded-xl" />) : <>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Status</p><Badge>{status}</Badge></CardContent></Card>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Type</p><p className="text-sm font-medium">{String(trip.tripType ?? '—')}</p></CardContent></Card>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Departure</p><p className="text-sm">{trip.departureDate ? formatDate(String(trip.departureDate)) : '—'}</p></CardContent></Card>
          <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Est. cost</p><p className="text-sm font-medium">{trip.estimatedCost ? `₹${Number(trip.estimatedCost).toLocaleString()}` : '—'}</p></CardContent></Card>
        </>}
      </div>

      <CrudSection
        title="Itinerary" icon={<MapPin className="h-6 w-6" />}
        items={rows(itinQ.data)} loading={itinQ.isLoading}
        emptyText="No itinerary legs" queryKey={['trip-itinerary', id]}
        fields={[
          { name: 'mode', label: 'Mode', type: 'select', required: true, options: [
            { value: 'FLIGHT', label: 'Flight' }, { value: 'TRAIN', label: 'Train' }, { value: 'BUS', label: 'Bus' }, { value: 'CAB', label: 'Cab' }, { value: 'HOTEL', label: 'Hotel' },
          ] },
          { name: 'fromLocation', label: 'From', type: 'text', required: true },
          { name: 'toLocation', label: 'To', type: 'text', required: true },
          { name: 'departAt', label: 'Depart', type: 'datetime-local' },
          { name: 'arriveAt', label: 'Arrive', type: 'datetime-local' },
          { name: 'estimatedCost', label: 'Cost', type: 'currency' },
        ]}
        onCreate={v => travelService.addLeg({ ...v, tripId: id })}
        renderItem={(l: AnyObj) => (<>
          <p className="text-sm font-medium"><Plane className="h-3.5 w-3.5 inline mr-1" />{String(l.mode ?? '')}: {String(l.fromLocation ?? '')} → {String(l.toLocation ?? '')}</p>
          <p className="text-xs text-slate-500">{l.departAt ? formatDate(String(l.departAt), 'PPp') : ''}{l.estimatedCost ? ` · ₹${Number(l.estimatedCost).toLocaleString()}` : ''}</p>
        </>)}
      />

      <FormDialog open={advanceOpen} onOpenChange={setAdvanceOpen} title="Request travel advance" submitLabel="Request"
        fields={[
          { name: 'amount', label: 'Amount', type: 'currency', required: true },
          { name: 'reason', label: 'Reason', type: 'textarea', span: 2 },
        ]}
        onSubmit={v => advance.mutateAsync(v)} />
      <FormDialog open={mileageOpen} onOpenChange={setMileageOpen} title="Log mileage claim" submitLabel="Log"
        fields={[
          { name: 'distanceKm', label: 'Distance (km)', type: 'number', required: true },
          { name: 'ratePerKm', label: 'Rate per km', type: 'number' },
          { name: 'fromLocation', label: 'From', type: 'text' },
          { name: 'toLocation', label: 'To', type: 'text' },
        ]}
        onSubmit={v => mileage.mutateAsync(v)} />
    </div>
  )
}

export type { TripRequest }
