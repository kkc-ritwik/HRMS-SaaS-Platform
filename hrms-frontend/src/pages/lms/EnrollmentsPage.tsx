import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { GraduationCap, Play } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Progress } from '@/components/ui/progress'
import { DataList } from '@/components/ui/data-list'
import { lmsService, type Enrollment } from '@/services/lmsService'

export function EnrollmentsPage() {
  const navigate = useNavigate()
  const { data, isLoading } = useQuery({ queryKey: ['enrollments', 'me'], queryFn: lmsService.myEnrollments })
  return (
    <DataList<Enrollment & Record<string, unknown>>
      title="My Enrollments"
      description="Courses you're currently taking — click to continue"
      data={(data as (Enrollment & Record<string, unknown>)[]) || []}
      isLoading={isLoading}
      emptyIcon={<GraduationCap className="h-10 w-10" />}
      emptyTitle="No enrollments yet"
      onRowClick={e => e.courseId && navigate(`/courses/${e.courseId}/play`)}
      columns={[
        { key: 'courseTitle', label: 'Course' },
        { key: 'status', label: 'Status', render: e => <Badge>{String(e.status)}</Badge> },
        { key: 'progressPercent', label: 'Progress', render: e => <div className="w-32"><Progress value={Number(e.progressPercent ?? 0)} /></div> },
        { key: 'score', label: 'Score', render: e => e.score ?? '—' },
        { key: 'courseId', label: '', align: 'right', sortable: false, render: e => e.courseId
          ? <Button size="sm" variant="ghost" onClick={ev => { ev.stopPropagation(); navigate(`/courses/${e.courseId}/play`) }}><Play className="h-3.5 w-3.5" /></Button>
          : null },
      ]}
    />
  )
}
