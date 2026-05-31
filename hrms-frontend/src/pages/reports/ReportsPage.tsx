import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { BarChart3 } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { reportsService, type ReportDefinition } from '@/services/reportsService'

export function ReportsPage() {
  const navigate = useNavigate()
  const { data, isLoading } = useQuery({ queryKey: ['reports', 'defs'], queryFn: reportsService.listDefinitions })

  return (
    <div className="space-y-6">
      <PageHeader title="Reports & Analytics" description="Pre-built and custom reports across the platform" />
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-28" />)}
        </div>
      ) : (data || []).length === 0 ? (
        <EmptyState icon={<BarChart3 className="h-10 w-10" />} title="No reports configured" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {(data as ReportDefinition[] || []).map(r => (
            <Card key={r.id} className="hover:shadow-md transition cursor-pointer" onClick={() => navigate(`/reports/${r.id}`)}>
              <CardContent className="p-4">
                <p className="text-xs font-medium uppercase text-slate-500">{r.module}</p>
                <h3 className="font-semibold mt-1">{r.name}</h3>
                {r.description && <p className="text-xs text-slate-500 mt-1 line-clamp-2">{r.description}</p>}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
