import { Calendar, MessageSquarePlus } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { interviewService } from '@/services/extendedServices'
import { formatDate } from '@/lib/utils'

interface Interview extends Record<string, unknown> {
  id: string; candidateName?: string; jobTitle?: string; scheduledAt?: string; mode?: string; meetingLink?: string; status: string
}

export function InterviewsPage() {
  return (
    <ResourcePage<Interview>
      title="Interviews"
      description="Scheduled, completed and upcoming interviews — schedule, submit feedback"
      icon={<Calendar className="h-10 w-10" />}
      queryKey={['interviews']}
      fetcher={() => interviewService.list()}
      filters={{ status: ['SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW'], mode: ['ONSITE', 'VIDEO', 'PHONE'] }}
      columns={[
        { key: 'scheduledAt', label: 'When', render: i => i.scheduledAt ? formatDate(String(i.scheduledAt), 'PPp') : '—' },
        { key: 'candidateName', label: 'Candidate' },
        { key: 'jobTitle', label: 'Role' },
        { key: 'mode', label: 'Mode', render: i => i.mode ? <Badge>{String(i.mode)}</Badge> : '—' },
        { key: 'status', label: 'Status', render: i => <Badge>{String(i.status)}</Badge> },
        { key: 'meetingLink', label: '', render: i => i.meetingLink ? <a className="text-brand-600 hover:underline" href={String(i.meetingLink)} target="_blank" rel="noreferrer" onClick={e => e.stopPropagation()}>Join</a> : null },
      ]}
      formFields={[
        { name: 'applicationId', label: 'Application ID', type: 'text', required: true },
        { name: 'round', label: 'Round', type: 'text', required: true },
        { name: 'scheduledAt', label: 'Scheduled at', type: 'datetime-local', required: true },
        { name: 'mode', label: 'Mode', type: 'select', required: true, options: [
          { value: 'ONSITE', label: 'On-site' }, { value: 'VIDEO', label: 'Video' }, { value: 'PHONE', label: 'Phone' },
        ] },
        { name: 'meetingLink', label: 'Meeting link', type: 'url' },
        { name: 'interviewerId', label: 'Interviewer ID', type: 'text' },
      ]}
      createTitle="Schedule interview"
      onCreate={v => interviewService.schedule(v)}
      rowActions={i => [
        { label: 'Submit feedback', icon: <MessageSquarePlus className="h-3.5 w-3.5" />, show: i.status !== 'CANCELLED', run: () => interviewService.submitFeedback(i.id, { recommendation: 'HOLD' }) },
      ]}
    />
  )
}
