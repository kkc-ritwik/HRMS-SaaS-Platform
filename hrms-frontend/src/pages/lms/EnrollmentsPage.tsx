import { useQuery } from '@tanstack/react-query'
import { GraduationCap } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { DataList } from '@/components/ui/data-list'
import { lmsService, type Enrollment } from '@/services/lmsService'

export function EnrollmentsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['enrollments', 'me'], queryFn: lmsService.myEnrollments })
  return (
    <DataList<Enrollment>
      title="My Enrollments"
      description="Courses you're currently taking"
      data={data || []}
      isLoading={isLoading}
      emptyIcon={<GraduationCap className="h-10 w-10" />}
      emptyTitle="No enrollments yet"
      columns={[
        { key: 'courseTitle', label: 'Course' },
        { key: 'status', label: 'Status', render: e => <Badge>{e.status}</Badge> },
        { key: 'progressPercent', label: 'Progress', render: e => <div className="w-32"><Progress value={e.progressPercent} /></div> },
        { key: 'score', label: 'Score', render: e => e.score ?? '—' },
      ]}
    />
  )
}
