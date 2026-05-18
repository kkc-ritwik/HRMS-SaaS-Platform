import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { TrendingUp, Users } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Card } from '@/components/ui/card'
import { SearchInput } from '@/components/ui/search-input'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Progress } from '@/components/ui/progress'
import { SkeletonTable } from '@/components/ui/skeleton'
import { Table, TableHeader, TableBody, TableHead, TableRow, TableCell, TablePagination } from '@/components/ui/table'
import { leaveService } from '@/services/leaveService'
import { useDebounce } from '@/hooks/useDebounce'
import { usePagination } from '@/hooks/usePagination'

const DEMO_BALANCES = [
  { id: '1', employeeName: 'Priya Sharma', employeeId: 'EMP001', leaveTypeName: 'Annual Leave', allocated: 21, used: 5, pending: 3, remaining: 13 },
  { id: '2', employeeName: 'Priya Sharma', employeeId: 'EMP001', leaveTypeName: 'Sick Leave', allocated: 12, used: 2, pending: 0, remaining: 10 },
  { id: '3', employeeName: 'Rahul Verma', employeeId: 'EMP002', leaveTypeName: 'Annual Leave', allocated: 21, used: 8, pending: 1, remaining: 12 },
  { id: '4', employeeName: 'Rahul Verma', employeeId: 'EMP002', leaveTypeName: 'Sick Leave', allocated: 12, used: 0, pending: 0, remaining: 12 },
  { id: '5', employeeName: 'Sneha Gupta', employeeId: 'EMP003', leaveTypeName: 'Annual Leave', allocated: 21, used: 15, pending: 2, remaining: 4 },
  { id: '6', employeeName: 'Amit Kumar', employeeId: 'EMP004', leaveTypeName: 'Annual Leave', allocated: 21, used: 3, pending: 0, remaining: 18 },
]

export function LeaveBalancePage() {
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebounce(search)
  const { page, pageSize, goToPage, changePageSize } = usePagination()

  const { data, isLoading } = useQuery({
    queryKey: ['leave-balances-all', debouncedSearch, page, pageSize],
    queryFn: async () => {
      try {
        return await leaveService.listBalances({ page, pageSize })
      } catch {
        return { data: DEMO_BALANCES }
      }
    },
  })

  const balances = data?.data || DEMO_BALANCES

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Leave Balances" description="Employee leave allocation overview" breadcrumbs={[{ label: 'Time & Leave' }, { label: 'Leave Balances' }]} />

      <Card>
        <div className="flex items-center gap-3 px-5 pt-5 pb-4 border-b border-slate-100">
          <SearchInput value={search} onChange={e => setSearch(e.target.value)} onClear={() => setSearch('')} placeholder="Search employee..." containerClassName="w-64" />
        </div>

        {isLoading ? (
          <div className="p-5"><SkeletonTable rows={6} cols={6} /></div>
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead>Leave Type</TableHead>
                  <TableHead>Allocated</TableHead>
                  <TableHead>Used</TableHead>
                  <TableHead>Pending</TableHead>
                  <TableHead>Remaining</TableHead>
                  <TableHead>Usage</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {balances.map((b: any) => {
                  const usagePercent = Math.round((b.used / b.allocated) * 100)
                  const isLow = b.remaining <= 3
                  return (
                    <TableRow key={b.id}>
                      <TableCell>
                        <div className="flex items-center gap-2.5">
                          <Avatar name={b.employeeName} size="xs" />
                          <div>
                            <p className="font-medium text-slate-900 text-xs">{b.employeeName}</p>
                            <p className="text-[10px] text-slate-400 font-mono">{b.employeeId}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell><span className="text-sm">{b.leaveTypeName}</span></TableCell>
                      <TableCell><span className="text-sm font-medium">{b.allocated}</span></TableCell>
                      <TableCell><span className="text-sm">{b.used}</span></TableCell>
                      <TableCell>
                        {b.pending > 0 ? (
                          <Badge variant="warning">{b.pending}</Badge>
                        ) : (
                          <span className="text-slate-400 text-sm">0</span>
                        )}
                      </TableCell>
                      <TableCell>
                        <span className={`text-sm font-semibold ${isLow ? 'text-red-600' : 'text-green-600'}`}>
                          {b.remaining}
                        </span>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2 min-w-[100px]">
                          <Progress value={usagePercent} className="h-1.5 flex-1" indicatorClassName={isLow ? 'bg-red-400' : ''} />
                          <span className="text-xs text-slate-500 w-8 text-right">{usagePercent}%</span>
                        </div>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
            <TablePagination page={page} pageSize={pageSize} total={balances.length} onPageChange={goToPage} onPageSizeChange={changePageSize} />
          </>
        )}
      </Card>
    </div>
  )
}
