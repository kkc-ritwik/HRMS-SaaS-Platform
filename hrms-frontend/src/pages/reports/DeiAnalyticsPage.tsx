import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Users } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts'
import { complianceService } from '@/services/complianceService'

export function DeiAnalyticsPage() {
  const [by, setBy] = useState<'gender' | 'ethnicity' | 'generation' | 'disability' | 'nationality'>('gender')
  const headcount = useQuery({ queryKey: ['dei-headcount', by], queryFn: () => complianceService.deiHeadcount(by) })
  const leadership = useQuery({ queryKey: ['dei-leadership'], queryFn: complianceService.deiLeadership })
  const payGap = useQuery({ queryKey: ['dei-pay-gap'], queryFn: () => complianceService.deiPayGap('gender') })

  return (
    <div className="space-y-6">
      <PageHeader title="Diversity & Inclusion" description="Headcount slices, leadership representation, pay equity" />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2"><Users className="h-5 w-5" /> Headcount distribution</CardTitle>
        </CardHeader>
        <CardContent>
          <Tabs value={by} onValueChange={v => setBy(v as never)}>
            <TabsList>
              <TabsTrigger value="gender">Gender</TabsTrigger>
              <TabsTrigger value="ethnicity">Ethnicity</TabsTrigger>
              <TabsTrigger value="generation">Generation</TabsTrigger>
              <TabsTrigger value="disability">Disability</TabsTrigger>
              <TabsTrigger value="nationality">Nationality</TabsTrigger>
            </TabsList>
            <TabsContent value={by}>
              {headcount.isLoading ? <Skeleton className="h-48" /> : (
                <ResponsiveContainer width="100%" height={240}>
                  <BarChart data={headcount.data as never}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                    <XAxis dataKey="group" tick={{ fontSize: 11 }} />
                    <YAxis tick={{ fontSize: 11 }} />
                    <Tooltip />
                    <Bar dataKey="count" fill="#7c3aed" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Card>
          <CardHeader><CardTitle>Leadership representation</CardTitle></CardHeader>
          <CardContent>
            {leadership.isLoading ? <Skeleton className="h-32" /> : <pre className="text-xs">{JSON.stringify(leadership.data, null, 2)}</pre>}
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Pay gap analysis</CardTitle></CardHeader>
          <CardContent>
            {payGap.isLoading ? <Skeleton className="h-32" /> : <pre className="text-xs">{JSON.stringify(payGap.data, null, 2)}</pre>}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
