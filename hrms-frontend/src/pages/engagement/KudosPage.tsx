import { useQuery } from '@tanstack/react-query'
import { Gift } from 'lucide-react'
import { Avatar } from '@/components/ui/avatar'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { engagementService, type Kudos } from '@/services/engagementService'
import { formatDate } from '@/lib/utils'

export function KudosPage() {
  const { data, isLoading } = useQuery({ queryKey: ['kudos', 'feed'], queryFn: engagementService.myKudosFeed })

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <PageHeader title="Kudos" description="Public peer recognition" />
      {isLoading ? (
        <Skeleton className="h-48" />
      ) : (data || []).length === 0 ? (
        <EmptyState icon={<Gift className="h-10 w-10" />} title="No kudos yet" description="Be the first to recognise a teammate" />
      ) : (
        <div className="space-y-3">
          {(data as Kudos[] || []).map(k => (
            <Card key={k.id}>
              <CardContent className="p-4 flex items-start gap-3">
                <Avatar name={k.recipientName} />
                <div className="flex-1">
                  <p className="text-sm"><strong>{k.recipientName || 'Someone'}</strong> received kudos</p>
                  {k.value && <p className="text-xs text-violet-600 font-medium mt-1">{k.value}</p>}
                  <p className="text-sm text-slate-700 mt-2">{k.message}</p>
                  <p className="text-xs text-slate-400 mt-2">{formatDate(k.createdAt, 'PPp')}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
