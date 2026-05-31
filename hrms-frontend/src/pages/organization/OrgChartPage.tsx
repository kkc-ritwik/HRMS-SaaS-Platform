import { useQuery } from '@tanstack/react-query'
import { Network } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Avatar } from '@/components/ui/avatar'
import { PageHeader } from '@/components/ui/page-header'
import { orgChartService } from '@/services/extendedServices'

interface OrgNode { id: string; name: string; title?: string; departmentName?: string; reports?: OrgNode[] }

function Node({ n }: { n: OrgNode }) {
  return (
    <div className="flex flex-col items-center">
      <Card className="min-w-[180px]">
        <CardContent className="p-3 flex flex-col items-center gap-1">
          <Avatar name={n.name} size="sm" />
          <p className="text-sm font-medium text-center">{n.name}</p>
          {n.title && <p className="text-xs text-slate-500 text-center">{n.title}</p>}
        </CardContent>
      </Card>
      {n.reports && n.reports.length > 0 && (
        <div className="mt-4 flex flex-wrap items-start justify-center gap-3 relative">
          <div className="absolute -top-2 left-1/2 w-px h-2 bg-slate-300" />
          {n.reports.map(r => <Node key={r.id} n={r} />)}
        </div>
      )}
    </div>
  )
}

export function OrgChartPage() {
  const { data, isLoading } = useQuery({ queryKey: ['org-chart'], queryFn: orgChartService.tree })
  return (
    <div className="space-y-6">
      <PageHeader title="Organisation Chart" description="Reporting hierarchy" />
      {isLoading ? <Skeleton className="h-96" /> : !data ? (
        <EmptyState icon={<Network className="h-10 w-10" />} title="No org structure" />
      ) : (
        <div className="overflow-x-auto p-4">
          <Node n={data as OrgNode} />
        </div>
      )}
    </div>
  )
}
