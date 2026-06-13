import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { SlidersHorizontal, RotateCcw } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { FormDialog } from '@/components/ui/form-dialog'
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
  const qc = useQueryClient()
  const [search, setSearch] = useState('')
  const [adjustOpen, setAdjustOpen] = useState(false)
  const [initOpen, setInitOpen] = useState(false)
  const debouncedSearch = useDebounce(search)
  const { page, pageSize, goToPage, changePageSize } = usePagination()

  const adjust = useMutation({ mutationFn: (v: Record<string, unknown>) => leaveService.adjustBalance(v), onSuccess: () => { qc.invalidateQueries({ queryKey: ['leave-balances-all'] }); setAdjustOpen(false) } })
  const init = useMutation({ mutationFn: (v: Record<string, unknown>) => leaveService.initBalance(v), onSuccess: () => { qc.invalidateQueries({ queryKey: ['leave-balances-all'] }); setInitOpen(false) } })

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
      <PageHeader title="Leave Balances" description="Employee leave allocation overview" breadcrumbs={[{ label: 'Time & Leave' }, { label: 'Leave Balances' }]}>
        <Button variant="outline" size="sm" onClick={() => setInitOpen(true)}><RotateCcw className="h-4 w-4 mr-1" /> Initialize Year</Button>
        <Button size="sm" onClick={() => setAdjustOpen(true)}><SlidersHorizontal className="h-4 w-4 mr-1" /> Adjust Balance</Button>
      </PageHeader>

      <FormDialog open={adjustOpen} onOpenChange={setAdjustOpen} title="Adjust leave balance" submitLabel="Apply"
        fields={[
          { name: 'employeeId', label: 'Employee ID', type: 'text', required: true },
          { name: 'leaveTypeId', label: 'Leave type ID', type: 'text', required: true },
          { name: 'days', label: 'Days (+ credit / − debit)', type: 'number', required: true },
          { name: 'reason', label: 'Reason', type: 'textarea', span: 2, required: true },
        ]}
        onSubmit={v => adjust.mutateAsync(v)} />
      <FormDialog open={initOpen} onOpenChange={setInitOpen} title="Initialize annual balances" submitLabel="Initialize"
        fields={[
          { name: 'year', label: 'Year', type: 'number', required: true, defaultValue: new Date().getFullYear() },
          { name: 'employeeId', label: 'Employee ID (blank = all)', type: 'text' },
        ]}
        onSubmit={v => init.mutateAsync(v)} />

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
