import { useQuery } from '@tanstack/react-query'
import { Star } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { performanceService, type Review } from '@/services/performanceService'

export function ReviewsPage() {
  const { data, isLoading } = useQuery({ queryKey: ['reviews', 'me'], queryFn: () => performanceService.myReviews() })
  const reviews = (data as Review[]) || []

  return (
    <DataList<Review>
      title="Performance Reviews"
      description="Active and upcoming review cycles"
      data={reviews}
      isLoading={isLoading}
      emptyIcon={<Star className="h-10 w-10" />}
      emptyTitle="No reviews scheduled"
      columns={[
        { key: 'title', label: 'Cycle' },
        { key: 'employeeName', label: 'Employee' },
        { key: 'reviewerName', label: 'Reviewer' },
        { key: 'period', label: 'Period' },
        { key: 'overallRating', label: 'Rating', render: r => r.overallRating ? `${r.overallRating}/5` : '—' },
        { key: 'status', label: 'Status', render: r => <Badge>{r.status}</Badge> },
      ]}
    />
  )
}
