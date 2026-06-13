import { Star, Send, CheckCircle2 } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { performanceService, type Review } from '@/services/performanceService'

export function ReviewsPage() {
  return (
    <ResourcePage<Review & Record<string, unknown>>
      title="Performance Reviews"
      description="Your review forms — self-rate, submit, and acknowledge"
      icon={<Star className="h-10 w-10" />}
      queryKey={['reviews', 'me']}
      fetcher={() => performanceService.myReviews()}
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
            label: 'Acknowledge', icon: <CheckCircle2 className="h-3.5 w-3.5" />,
            show: status === 'COMPLETED',
            run: () => performanceService.acknowledgeReview(r.id),
          },
        ]
      }}
    />
  )
}
