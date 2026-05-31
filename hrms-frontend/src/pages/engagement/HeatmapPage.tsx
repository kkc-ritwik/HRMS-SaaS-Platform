import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Activity } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import { PageHeader } from '@/components/ui/page-header'
import { heatmapService } from '@/services/extendedServices'

function thirtyDayWindow() {
  const to = new Date()
  const from = new Date(to)
  from.setDate(to.getDate() - 30)
  return { from: from.toISOString().slice(0, 10), to: to.toISOString().slice(0, 10) }
}

function Heat({ data, isLoading }: { data: Array<{ label?: string; band?: string; averageScore: number; enps: number; responses: number }> | undefined; isLoading: boolean }) {
  if (isLoading) return <Skeleton className="h-48" />
  const items = data || []
  if (!items.length) return <p className="text-sm text-slate-500 p-4">No data yet</p>
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
      {items.map((d, i) => {
        const color = d.averageScore >= 4 ? 'bg-green-50 border-green-200' : d.averageScore >= 3 ? 'bg-yellow-50 border-yellow-200' : 'bg-red-50 border-red-200'
        return (
          <Card key={i} className={color}>
            <CardContent className="p-4">
              <p className="text-sm font-medium">{d.label || d.band}</p>
              <p className="text-2xl font-bold mt-1">{d.averageScore?.toFixed(2)}</p>
              <div className="flex items-center justify-between text-xs mt-2 text-slate-600">
                <span>{d.responses} responses</span>
                <Badge>eNPS {d.enps}</Badge>
              </div>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}

export function HeatmapPage() {
  const [{ from, to }] = useState(thirtyDayWindow())
  const dept = useQuery({ queryKey: ['heatmap', 'dept', from, to], queryFn: () => heatmapService.byDept(from, to) })
  const loc = useQuery({ queryKey: ['heatmap', 'loc', from, to], queryFn: () => heatmapService.byLocation(from, to) })
  const mgr = useQuery({ queryKey: ['heatmap', 'mgr', from, to], queryFn: () => heatmapService.byManager(from, to) })
  const tenure = useQuery({ queryKey: ['heatmap', 'tenure', from, to], queryFn: () => heatmapService.byTenure(from, to) })

  return (
    <div className="space-y-6">
      <PageHeader title="Engagement Heatmap" description={`Pulse score breakdowns · ${from} → ${to}`} />
      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><Activity className="h-5 w-5" /> Slice by</CardTitle></CardHeader>
        <CardContent>
          <Tabs defaultValue="dept">
            <TabsList>
              <TabsTrigger value="dept">Department</TabsTrigger>
              <TabsTrigger value="loc">Location</TabsTrigger>
              <TabsTrigger value="mgr">Manager</TabsTrigger>
              <TabsTrigger value="tenure">Tenure</TabsTrigger>
            </TabsList>
            <TabsContent value="dept"><Heat data={dept.data as never} isLoading={dept.isLoading} /></TabsContent>
            <TabsContent value="loc"><Heat data={loc.data as never} isLoading={loc.isLoading} /></TabsContent>
            <TabsContent value="mgr"><Heat data={mgr.data as never} isLoading={mgr.isLoading} /></TabsContent>
            <TabsContent value="tenure"><Heat data={tenure.data as never} isLoading={tenure.isLoading} /></TabsContent>
          </Tabs>
        </CardContent>
      </Card>
    </div>
  )
}
