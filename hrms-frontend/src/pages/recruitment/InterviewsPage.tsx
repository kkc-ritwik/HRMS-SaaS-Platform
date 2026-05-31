import { useQuery } from '@tanstack/react-query'
import { Calendar } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { interviewService } from '@/services/extendedServices'

interface Interview {
  id: string
  candidateName?: string
  jobTitle?: string
  scheduledAt: string
  mode: string
  meetingLink?: string
  status: string
}

export function InterviewsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['interviews'], queryFn: () => interviewService.list() })
  const items: Interview[] = (data as { content?: Interview[] } | undefined)?.content
    || (Array.isArray(data) ? data as Interview[] : [])
  return (
    <DataList<Interview>
      title="Interviews" description="Scheduled, completed and upcoming interviews"
      data={items} isLoading={isLoading}
      emptyIcon={<Calendar className="h-10 w-10" />} emptyTitle="No interviews scheduled"
      columns={[
        { key: 'scheduledAt', label: 'When' },
        { key: 'candidateName', label: 'Candidate' },
        { key: 'jobTitle', label: 'Role' },
        { key: 'mode', label: 'Mode', render: i => <Badge>{i.mode}</Badge> },
        { key: 'status', label: 'Status', render: i => <Badge>{i.status}</Badge> },
        { key: 'meetingLink', label: '', render: i => i.meetingLink ? <a className="text-violet-600 hover:underline" href={i.meetingLink} target="_blank" rel="noreferrer">Join</a> : null },
      ]}
    />
  )
}
