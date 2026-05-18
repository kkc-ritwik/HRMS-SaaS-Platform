import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { format } from 'date-fns'
import { Play, Plus, Eye, MoreVertical, DollarSign, Users, TrendingDown, CheckCircle } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { StatCard } from '@/components/ui/stat-card'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import { FormField } from '@/components/ui/form-field'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { SkeletonCard } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { payrollService } from '@/services/payrollService'
import { formatCurrency, formatDate } from '@/lib/utils'
import { getErrorMessage } from '@/lib/api'

const DEMO_PAY_RUNS = [
  { id: '1', name: 'March 2025 Payroll', period: 'March 2025', month: 3, year: 2025, status: 'COMPLETED', employeeCount: 144, totalGross: 8640000, totalDeductions: 1296000, totalNet: 7344000, processedAt: '2025-03-28', createdAt: '2025-03-25' },
  { id: '2', name: 'February 2025 Payroll', period: 'February 2025', month: 2, year: 2025, status: 'COMPLETED', employeeCount: 141, totalGross: 8460000, totalDeductions: 1269000, totalNet: 7191000, processedAt: '2025-02-28', createdAt: '2025-02-25' },
  { id: '3', name: 'January 2025 Payroll', period: 'January 2025', month: 1, year: 2025, status: 'COMPLETED', employeeCount: 138, totalGross: 8280000, totalDeductions: 1242000, totalNet: 7038000, processedAt: '2025-01-28', createdAt: '2025-01-25' },
  { id: '4', name: 'April 2025 Payroll', period: 'April 2025', month: 4, year: 2025, status: 'DRAFT', employeeCount: 144, totalGross: 0, totalDeductions: 0, totalNet: 0, createdAt: '2025-04-01' },
]

const statusConfig = {
  DRAFT: { variant: 'secondary' as const, label: 'Draft' },
  PROCESSING: { variant: 'warning' as const, label: 'Processing' },
  COMPLETED: { variant: 'success' as const, label: 'Completed' },
  FAILED: { variant: 'destructive' as const, label: 'Failed' },
}

const months = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
]

export function PayrollRunPage() {
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [newPayRun, setNewPayRun] = useState({ month: String(new Date().getMonth() + 1), year: String(new Date().getFullYear()) })

  const { data, isLoading } = useQuery({
    queryKey: ['pay-runs'],
    queryFn: async () => {
      try {
        return await payrollService.listPayRuns()
      } catch {
        return { data: DEMO_PAY_RUNS }
      }
    },
  })

  const createMutation = useMutation({
    mutationFn: payrollService.createPayRun,
    onSuccess: () => {
      toast.success('Pay run created!')
      queryClient.invalidateQueries({ queryKey: ['pay-runs'] })
      setIsModalOpen(false)
    },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const processMutation = useMutation({
    mutationFn: payrollService.processPayRun,
    onSuccess: () => {
      toast.success('Payroll processed successfully!')
      queryClient.invalidateQueries({ queryKey: ['pay-runs'] })
    },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const payRuns = data?.data || DEMO_PAY_RUNS

  const totalNetLastMonth = payRuns.find((p: any) => p.status === 'COMPLETED')?.totalNet || 0

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Payroll Runs" description="Process and manage payroll" breadcrumbs={[{ label: 'Payroll' }, { label: 'Pay Runs' }]}>
        <Button size="sm" onClick={() => setIsModalOpen(true)}>
          <Plus className="h-4 w-4" /> New Pay Run
        </Button>
      </PageHeader>

      {/* Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard title="Last Payroll (Net)" value={formatCurrency(totalNetLastMonth)} icon={<DollarSign className="h-5 w-5 text-green-600" />} iconBg="bg-green-100" />
        <StatCard title="Employees Paid" value={payRuns.find((p: any) => p.status === 'COMPLETED')?.employeeCount || 0} icon={<Users className="h-5 w-5 text-brand-600" />} iconBg="bg-brand-100" />
        <StatCard title="Total Deductions" value={formatCurrency(payRuns.find((p: any) => p.status === 'COMPLETED')?.totalDeductions || 0)} icon={<TrendingDown className="h-5 w-5 text-amber-600" />} iconBg="bg-amber-100" />
      </div>

      {/* Pay runs list */}
      <div className="space-y-3">
        {isLoading ? (
          Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)
        ) : payRuns.length === 0 ? (
          <Card>
            <EmptyState icon={<DollarSign className="h-8 w-8" />} title="No pay runs" action={{ label: 'Create Pay Run', onClick: () => setIsModalOpen(true) }} />
          </Card>
        ) : (
          payRuns.map((run: any) => {
            const sc = statusConfig[run.status as keyof typeof statusConfig]
            return (
              <Card key={run.id} className="hover:shadow-card-hover transition-shadow">
                <CardContent className="pt-5">
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br from-brand-100 to-violet-100 flex-shrink-0">
                        <DollarSign className="h-6 w-6 text-brand-600" />
                      </div>
                      <div>
                        <div className="flex items-center gap-2 mb-1">
                          <h3 className="font-semibold text-slate-900">{run.name}</h3>
                          <Badge variant={sc.variant} dot>{sc.label}</Badge>
                        </div>
                        {run.status === 'COMPLETED' ? (
                          <div className="flex flex-wrap gap-4 text-sm text-slate-600">
                            <span>Gross: <strong className="text-slate-800">{formatCurrency(run.totalGross)}</strong></span>
                            <span>Deductions: <strong className="text-red-600">{formatCurrency(run.totalDeductions)}</strong></span>
                            <span>Net: <strong className="text-green-600">{formatCurrency(run.totalNet)}</strong></span>
                            <span className="flex items-center gap-1"><Users className="h-3.5 w-3.5 text-slate-400" />{run.employeeCount} employees</span>
                          </div>
                        ) : (
                          <p className="text-sm text-slate-500">{run.employeeCount} employees · Draft</p>
                        )}
                        {run.processedAt && (
                          <p className="text-xs text-slate-400 mt-1">Processed: {formatDate(run.processedAt)}</p>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-2 flex-shrink-0">
                      {run.status === 'DRAFT' && (
                        <Button
                          size="sm"
                          onClick={() => processMutation.mutate(run.id)}
                          loading={processMutation.isPending}
                          className="bg-green-600 hover:bg-green-700"
                        >
                          <Play className="h-4 w-4" />
                          Process
                        </Button>
                      )}
                      <Button variant="outline" size="sm">
                        <Eye className="h-4 w-4" />
                        View
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            )
          })
        )}
      </div>

      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent size="sm">
          <DialogHeader><DialogTitle>Create New Pay Run</DialogTitle></DialogHeader>
          <DialogBody className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Month" required>
                <Select value={newPayRun.month} onValueChange={v => setNewPayRun(p => ({ ...p, month: v }))}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {months.map((m, i) => (
                      <SelectItem key={i + 1} value={String(i + 1)}>{m}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <FormField label="Year" required>
                <Select value={newPayRun.year} onValueChange={v => setNewPayRun(p => ({ ...p, year: v }))}>
                  <SelectTrigger><SelectValue /></SelectTrigger>
                  <SelectContent>
                    {[2023, 2024, 2025, 2026].map(y => (
                      <SelectItem key={y} value={String(y)}>{y}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
            </div>
          </DialogBody>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsModalOpen(false)}>Cancel</Button>
            <Button
              onClick={() => createMutation.mutate({ month: Number(newPayRun.month), year: Number(newPayRun.year) })}
              loading={createMutation.isPending}
            >
              Create Pay Run
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
