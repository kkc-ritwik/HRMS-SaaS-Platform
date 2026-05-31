import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Plus } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { leaveService } from '@/services/leaveService'

export function MyLeavePage() {
  const navigate = useNavigate()
  const balances = useQuery({ queryKey: ['leave', 'balances', 'me'], queryFn: leaveService.myBalances })
  const recent = useQuery({ queryKey: ['leave', 'me'], queryFn: leaveService.myLeaves })

  return (
    <div className="space-y-6">
      <PageHeader
        title="My Leave"
        description="Track balances and apply for time off"
        action={<Button onClick={() => navigate('/leave/apply')}><Plus className="h-4 w-4 mr-1" /> Apply</Button>}
      />

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {balances.isLoading ? (
          Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-24" />)
        ) : (
          (balances.data as Array<{ leaveType: string; available: number; used: number; total: number }> | undefined || []).map(b => (
            <Card key={b.leaveType}>
              <CardContent className="p-4">
                <p className="text-xs text-slate-500">{b.leaveType}</p>
                <p className="text-2xl font-bold text-slate-800 mt-1">{b.available}</p>
                <p className="text-xs text-slate-400 mt-1">{b.used} used / {b.total} total</p>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      <Card>
        <CardHeader><CardTitle>My Applications</CardTitle></CardHeader>
        <CardContent>
          <div className="space-y-2">
            {recent.isLoading ? (
              Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-14" />)
            ) : (
              (recent.data as Array<{ id: string; leaveType: string; fromDate: string; toDate: string; days: number; status: string }> | undefined || []).map(l => (
                <div key={l.id} className="flex items-center justify-between p-3 rounded-lg border border-slate-100 hover:bg-slate-50">
                  <div>
                    <p className="font-medium text-sm">{l.leaveType} · {l.days} day(s)</p>
                    <p className="text-xs text-slate-500">{l.fromDate} → {l.toDate}</p>
                  </div>
                  <Badge>{l.status}</Badge>
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
