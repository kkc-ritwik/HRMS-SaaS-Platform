import { Plane, Send, Check, X } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { travelService, type TripRequest } from '@/services/travelService'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'

export function TravelPage() {
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  return (
    <ResourcePage<TripRequest & Record<string, unknown>>
      title="My Trips"
      description="Domestic + international travel — request, submit, approve"
      icon={<Plane className="h-10 w-10" />}
      queryKey={['travel', 'me', empId]}
      fetcher={() => travelService.myTrips(empId)}
      rowHref={t => `/travel/${t.id}`}
      filters={{ status: ['DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'COMPLETED'], tripType: ['DOMESTIC', 'INTERNATIONAL'] }}
      columns={[
        { key: 'purpose', label: 'Purpose' },
        { key: 'fromLocation', label: 'From' },
        { key: 'toLocation', label: 'To' },
        { key: 'tripType', label: 'Type', render: t => <Badge>{String(t.tripType)}</Badge> },
        { key: 'departureDate', label: 'Departure', render: t => t.departureDate ? formatDate(String(t.departureDate)) : '—' },
        { key: 'returnDate', label: 'Return', render: t => t.returnDate ? formatDate(String(t.returnDate)) : '—' },
        { key: 'status', label: 'Status', render: t => <Badge variant={t.status === 'REJECTED' ? 'destructive' : t.status === 'APPROVED' ? 'success' : 'warning'}>{String(t.status)}</Badge> },
      ]}
      formFields={[
        { name: 'purpose', label: 'Purpose', type: 'text', required: true, span: 2 },
        { name: 'tripType', label: 'Type', type: 'select', required: true, options: [
          { value: 'DOMESTIC', label: 'Domestic' }, { value: 'INTERNATIONAL', label: 'International' },
        ] },
        { name: 'fromLocation', label: 'From', type: 'text', required: true },
        { name: 'toLocation', label: 'To', type: 'text', required: true },
        { name: 'departureDate', label: 'Departure', type: 'date', required: true },
        { name: 'returnDate', label: 'Return', type: 'date' },
        { name: 'estimatedCost', label: 'Estimated cost', type: 'currency' },
        { name: 'notes', label: 'Notes', type: 'textarea', span: 2 },
      ]}
      createTitle="Request trip"
      onCreate={v => travelService.createTrip({ ...v, employeeId: empId } as Partial<TripRequest>)}
      rowActions={t => [
        { label: 'Submit', icon: <Send className="h-3.5 w-3.5" />, show: t.status === 'DRAFT', run: () => travelService.submitTrip(t.id) },
        { label: 'Approve', icon: <Check className="h-3.5 w-3.5" />, show: t.status === 'SUBMITTED', run: () => travelService.approveTrip(t.id, empId) },
        { label: 'Reject', icon: <X className="h-3.5 w-3.5" />, show: t.status === 'SUBMITTED', destructive: true, confirm: 'Reject this trip request?', run: () => travelService.rejectTrip(t.id, 'Rejected') },
      ]}
    />
  )
}
