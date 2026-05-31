import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Grid3x3 } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Avatar } from '@/components/ui/avatar'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { performanceService } from '@/services/performanceService'

interface EmployeePosition { employeeId: string; performanceRating?: number; potentialRating?: number; overallRating?: number }
interface NineBoxCell { performanceBand: number; potentialBand: number; label: string; employees?: EmployeePosition[]; count: number }
interface NineBoxGrid { cycleId: string; cycleName?: string; grid: NineBoxCell[]; totalReviewed: number }
interface Cycle { id: string; name: string; status?: string }

const CELL_COLOR: Record<string, string> = {
  '3-3': 'bg-green-50 border-green-400', '3-2': 'bg-emerald-50 border-emerald-300', '3-1': 'bg-blue-50 border-blue-300',
  '2-3': 'bg-amber-50 border-amber-300', '2-2': 'bg-indigo-50 border-indigo-300', '2-1': 'bg-slate-50 border-slate-300',
  '1-3': 'bg-yellow-50 border-yellow-300', '1-2': 'bg-orange-50 border-orange-300', '1-1': 'bg-red-50 border-red-300',
}

export function NineBoxPage() {
  const [cycleId, setCycleId] = useState('')
  const cycles = useQuery({ queryKey: ['perf-cycles'], queryFn: () => performanceService.listCycles() })
  const cycleList = (cycles.data as Cycle[]) || []
  useEffect(() => { if (!cycleId && cycleList.length > 0) setCycleId(cycleList[0].id) }, [cycleId, cycleList])

  const { data, isLoading } = useQuery({
    queryKey: ['nine-box', cycleId],
    queryFn: () => performanceService.nineBox(cycleId) as Promise<NineBoxGrid>,
    enabled: !!cycleId,
  })
  const grid = (data as NineBoxGrid | undefined)?.grid || []
  const cellFor = (perf: number, pot: number) => grid.find(c => c.performanceBand === perf && c.potentialBand === pot)

  return (
    <div className="space-y-6">
      <PageHeader title="9-Box Talent Review" description="Performance × Potential grid from finalized reviews" action={
        <Select value={cycleId} onValueChange={setCycleId}>
          <SelectTrigger className="w-64"><SelectValue placeholder="Select cycle" /></SelectTrigger>
          <SelectContent>
            {cycleList.map(c => <SelectItem key={c.id} value={c.id}>{c.name}</SelectItem>)}
          </SelectContent>
        </Select>
      } />

      {isLoading ? <Skeleton className="h-96" /> : (
        <Card>
          <CardContent className="p-6">
            <div className="grid grid-cols-[80px_1fr_1fr_1fr] gap-2">
              <div></div>
              {['Low Performance', 'Medium Performance', 'High Performance'].map(l =>
                <div key={l} className="text-center text-xs font-medium text-slate-500 pb-2">{l}</div>
              )}

              {[3, 2, 1].map(potBand => (
                <>
                  <div key={`label-${potBand}`} className="flex items-center justify-end text-xs font-medium text-slate-500 pr-2">
                    {potBand === 3 ? 'High Potential' : potBand === 2 ? 'Medium Potential' : 'Low Potential'}
                  </div>
                  {[1, 2, 3].map(perfBand => {
                    const cell = cellFor(perfBand, potBand)
                    const emps = cell?.employees || []
                    return (
                      <div key={`${perfBand}-${potBand}`} className={`min-h-[140px] border-2 rounded-lg p-2 ${CELL_COLOR[`${perfBand}-${potBand}`]}`}>
                        <p className="text-xs font-semibold text-slate-700 mb-2">{cell?.label || '—'}</p>
                        <div className="space-y-1">
                          {emps.slice(0, 5).map(e => (
                            <div key={e.employeeId} className="flex items-center gap-1.5 bg-white/70 rounded px-1.5 py-1">
                              <Avatar name={e.employeeId.slice(0, 8)} size="sm" />
                              <span className="text-xs truncate">{e.employeeId.slice(0, 8)}</span>
                            </div>
                          ))}
                          {emps.length > 5 && <p className="text-xs text-slate-400">+{emps.length - 5} more</p>}
                        </div>
                      </div>
                    )
                  })}
                </>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="flex items-center gap-2 text-xs text-slate-500">
        <Grid3x3 className="h-3.5 w-3.5" /> Populated from FINALIZED manager reviews in the selected cycle
      </div>
    </div>
  )
}
