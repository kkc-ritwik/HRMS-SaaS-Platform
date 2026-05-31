import { useQuery } from '@tanstack/react-query'
import { Plane } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { travelService, type TripRequest } from '@/services/travelService'
import { useAuthStore } from '@/store/authStore'

export function TravelPage() {
  const user = useAuthStore(s => s.user)
  const empId = user?.employeeId || user?.id || ''
  const { data, isLoading } = useQuery({
    queryKey: ['travel', 'me', empId],
    queryFn: () => travelService.myTrips(empId),
    enabled: !!empId,
  })
  return (
    <DataList<TripRequest>
      title="My Trips" description="Domestic + international travel"
      data={data || []} isLoading={isLoading}
      emptyIcon={<Plane className="h-10 w-10" />} emptyTitle="No travel requests"
      columns={[
        { key: 'purpose', label: 'Purpose' },
        { key: 'fromLocation', label: 'From' },
        { key: 'toLocation', label: 'To' },
        { key: 'tripType', label: 'Type', render: t => <Badge>{t.tripType}</Badge> },
        { key: 'departureDate', label: 'Departure' },
        { key: 'returnDate', label: 'Return' },
        { key: 'status', label: 'Status', render: t => <Badge>{t.status}</Badge> },
      ]}
    />
  )
}
