import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { UserMinus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { offboardingService, type Separation } from '@/services/offboardingService'

export function OffboardingPage() {
  const navigate = useNavigate()
  const { data, isLoading } = useQuery({ queryKey: ['offboarding'], queryFn: offboardingService.list })
  return (
    <DataList<Separation>
      title="Separations"
      description="Resignations, terminations, retirements"
      data={data || []}
      isLoading={isLoading}
      onRowClick={s => navigate(`/separations/${s.id}`)}
      emptyIcon={<UserMinus className="h-10 w-10" />}
      emptyTitle="No active separations"
      columns={[
        { key: 'employeeId', label: 'Employee' },
        { key: 'type', label: 'Type', render: s => <Badge>{s.type}</Badge> },
        { key: 'resignationDate', label: 'Resigned' },
        { key: 'lastWorkingDay', label: 'Last day' },
        { key: 'status', label: 'Status', render: s => <Badge>{s.status}</Badge> },
      ]}
    />
  )
}
