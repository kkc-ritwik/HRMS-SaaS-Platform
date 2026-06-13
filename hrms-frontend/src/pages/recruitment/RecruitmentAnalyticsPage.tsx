import { useQuery } from '@tanstack/react-query'
import { BarChart3, Clock, DollarSign, Award, TrendingDown, FileClock } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { StatCard } from '@/components/ui/stat-card'
import { Catalog } from '@/services/catalog'

function metric(d: unknown): string {
  if (d == null) return '—'
  if (typeof d === 'number') return String(d)
  const o = d as Record<string, unknown>
  const v = o.value ?? o.days ?? o.rate ?? o.amount ?? o.count
  return v == null ? '—' : String(v)
}

export function RecruitmentAnalyticsPage() {
  const dashQ = useQuery({ queryKey: ['recr-dash'], queryFn: () => Catalog.recruitment.analytics.dashboard() })
  const cphQ = useQuery({ queryKey: ['recr-cph'], queryFn: () => Catalog.recruitment.analytics.costPerHire() })
  const ttfQ = useQuery({ queryKey: ['recr-ttf'], queryFn: () => Catalog.recruitment.analytics.timeToFill() })
  const oarQ = useQuery({ queryKey: ['recr-oar'], queryFn: () => Catalog.recruitment.analytics.offerAcceptanceRate() })
  const qohQ = useQuery({ queryKey: ['recr-qoh'], queryFn: () => Catalog.recruitment.analytics.qualityOfHire() })
  const agingQ = useQuery({ queryKey: ['recr-aging'], queryFn: () => Catalog.recruitment.analytics.reqAging() })
  const pipelineQ = useQuery({ queryKey: ['recr-pipeline'], queryFn: () => Catalog.recruitment.analytics.pipeline() })

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Recruitment Analytics" description="Hiring funnel, cost, speed and quality metrics" />

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
        <StatCard title="Cost per Hire" value={`₹${metric(cphQ.data)}`} icon={<DollarSign className="h-5 w-5 text-brand-600" />} loading={cphQ.isLoading} />
        <StatCard title="Time to Fill (days)" value={metric(ttfQ.data)} icon={<Clock className="h-5 w-5 text-brand-600" />} loading={ttfQ.isLoading} />
        <StatCard title="Offer Acceptance" value={`${metric(oarQ.data)}%`} icon={<Award className="h-5 w-5 text-brand-600" />} loading={oarQ.isLoading} />
        <StatCard title="Quality of Hire" value={metric(qohQ.data)} icon={<TrendingDown className="h-5 w-5 text-brand-600" />} loading={qohQ.isLoading} />
        <StatCard title="Open Reqs Aging" value={metric(agingQ.data)} icon={<FileClock className="h-5 w-5 text-brand-600" />} loading={agingQ.isLoading} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card>
          <CardHeader><CardTitle className="text-sm flex items-center gap-2"><BarChart3 className="h-4 w-4" /> Pipeline</CardTitle></CardHeader>
          <CardContent>{pipelineQ.isLoading ? <Skeleton className="h-40" /> : <pre className="text-xs bg-slate-50 p-3 rounded overflow-auto max-h-80">{JSON.stringify(pipelineQ.data, null, 2)}</pre>}</CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-sm flex items-center gap-2"><BarChart3 className="h-4 w-4" /> Dashboard</CardTitle></CardHeader>
          <CardContent>{dashQ.isLoading ? <Skeleton className="h-40" /> : <pre className="text-xs bg-slate-50 p-3 rounded overflow-auto max-h-80">{JSON.stringify(dashQ.data, null, 2)}</pre>}</CardContent>
        </Card>
      </div>
    </div>
  )
}
