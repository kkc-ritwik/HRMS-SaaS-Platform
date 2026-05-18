import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Plus, Calendar, Clock, CheckCircle, XCircle, AlertCircle, Minus } from 'lucide-react'
import { format, differenceInCalendarDays, parseISO } from 'date-fns'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { EmptyState } from '@/components/ui/empty-state'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import { leaveService } from '@/services/leaveService'
import { getErrorMessage } from '@/lib/api'
import { formatDate, getStatusColor } from '@/lib/utils'
import { useAuthStore } from '@/store/authStore'

const schema = z.object({
  leaveTypeId: z.string().min(1, 'Please select a leave type'),
  startDate: z.string().min(1, 'Start date is required'),
  endDate: z.string().min(1, 'End date is required'),
  reason: z.string().min(5, 'Please provide a reason (min 5 characters)'),
})

type FormData = z.infer<typeof schema>

const DEMO_LEAVES = [
  { id: '1', employeeId: '1', employeeName: 'Current User', leaveTypeId: '1', leaveTypeName: 'Annual Leave', startDate: '2025-04-10', endDate: '2025-04-12', days: 3, reason: 'Family vacation', status: 'PENDING', createdAt: '2025-04-01' },
  { id: '2', employeeId: '1', employeeName: 'Current User', leaveTypeId: '2', leaveTypeName: 'Sick Leave', startDate: '2025-03-20', endDate: '2025-03-21', days: 2, reason: 'Not feeling well', status: 'APPROVED', approverName: 'Anita Singh', createdAt: '2025-03-19' },
  { id: '3', employeeId: '1', employeeName: 'Current User', leaveTypeId: '3', leaveTypeName: 'Casual Leave', startDate: '2025-02-14', endDate: '2025-02-14', days: 1, reason: 'Personal work', status: 'REJECTED', rejectionReason: 'Peak project deadline', createdAt: '2025-02-10' },
]

const DEMO_BALANCES = [
  { id: '1', leaveTypeName: 'Annual Leave', allocated: 21, used: 3, pending: 3, remaining: 15 },
  { id: '2', leaveTypeName: 'Sick Leave', allocated: 12, used: 2, pending: 0, remaining: 10 },
  { id: '3', leaveTypeName: 'Casual Leave', allocated: 6, used: 1, pending: 0, remaining: 5 },
]

const DEMO_LEAVE_TYPES = [
  { id: '1', name: 'Annual Leave', code: 'AL' },
  { id: '2', name: 'Sick Leave', code: 'SL' },
  { id: '3', name: 'Casual Leave', code: 'CL' },
]

const statusConfig = {
  PENDING: { icon: Clock, color: 'bg-amber-100 text-amber-700', variant: 'warning' as const },
  APPROVED: { icon: CheckCircle, color: 'bg-green-100 text-green-700', variant: 'success' as const },
  REJECTED: { icon: XCircle, color: 'bg-red-100 text-red-700', variant: 'destructive' as const },
  CANCELLED: { icon: Minus, color: 'bg-slate-100 text-slate-600', variant: 'secondary' as const },
}

export function LeaveApplicationPage() {
  const user = useAuthStore(s => s.user)
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)

  const { data: leavesData } = useQuery({
    queryKey: ['my-leaves'],
    queryFn: async () => {
      try {
        return await leaveService.listApplications({ employeeId: user?.id })
      } catch {
        return { data: DEMO_LEAVES }
      }
    },
  })

  const { data: balancesData } = useQuery({
    queryKey: ['my-leave-balances'],
    queryFn: async () => {
      try {
        return await leaveService.listBalances({ employeeId: user?.id })
      } catch {
        return { data: DEMO_BALANCES }
      }
    },
  })

  const applyMutation = useMutation({
    mutationFn: leaveService.applyLeave,
    onSuccess: () => {
      toast.success('Leave application submitted!')
      queryClient.invalidateQueries({ queryKey: ['my-leaves'] })
      queryClient.invalidateQueries({ queryKey: ['my-leave-balances'] })
      setIsModalOpen(false)
      reset()
    },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const cancelMutation = useMutation({
    mutationFn: leaveService.cancelLeave,
    onSuccess: () => {
      toast.success('Leave cancelled')
      queryClient.invalidateQueries({ queryKey: ['my-leaves'] })
    },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const { register, handleSubmit, reset, watch, setValue, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema) })

  const startDate = watch('startDate')
  const endDate = watch('endDate')
  const daysCount = startDate && endDate
    ? Math.max(1, differenceInCalendarDays(parseISO(endDate), parseISO(startDate)) + 1)
    : 0

  const leaves = leavesData?.data || DEMO_LEAVES
  const balances = balancesData?.data || DEMO_BALANCES

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="My Leave" description="Manage your leave applications" breadcrumbs={[{ label: 'My Space' }, { label: 'My Leave' }]}>
        <Button size="sm" onClick={() => { reset(); setIsModalOpen(true) }}>
          <Plus className="h-4 w-4" /> Apply Leave
        </Button>
      </PageHeader>

      {/* Leave balances */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        {balances.map((b: any) => (
          <Card key={b.id}>
            <CardContent className="pt-5">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="text-sm font-semibold text-slate-800">{b.leaveTypeName}</p>
                  <p className="text-xs text-slate-400 mt-0.5">Year 2025</p>
                </div>
                <span className="text-2xl font-bold text-brand-600">{b.remaining}</span>
              </div>
              <div className="w-full bg-slate-100 rounded-full h-1.5 mb-2">
                <div
                  className="h-1.5 rounded-full bg-brand-500 transition-all"
                  style={{ width: `${((b.remaining) / b.allocated) * 100}%` }}
                />
              </div>
              <div className="flex justify-between text-xs text-slate-500">
                <span>{b.used} used</span>
                {b.pending > 0 && <span className="text-amber-600">{b.pending} pending</span>}
                <span>{b.allocated} total</span>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Leave list */}
      <Card>
        <CardHeader>
          <CardTitle>My Applications</CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          {leaves.length === 0 ? (
            <EmptyState
              icon={<Calendar className="h-8 w-8" />}
              title="No leave applications"
              description="Apply for your first leave"
              action={{ label: 'Apply Leave', onClick: () => setIsModalOpen(true) }}
            />
          ) : (
            <div className="space-y-3">
              {leaves.map((leave: any) => {
                const statusInfo = statusConfig[leave.status as keyof typeof statusConfig]
                const StatusIcon = statusInfo.icon
                return (
                  <div key={leave.id} className="flex items-start gap-4 p-4 rounded-xl border border-slate-100 hover:bg-slate-50 transition-colors">
                    <div className={`flex h-10 w-10 items-center justify-center rounded-xl flex-shrink-0 ${statusInfo.color}`}>
                      <StatusIcon className="h-5 w-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-0.5">
                        <span className="font-medium text-slate-900">{leave.leaveTypeName}</span>
                        <Badge variant={statusInfo.variant} dot>{leave.status}</Badge>
                      </div>
                      <p className="text-sm text-slate-600">
                        {formatDate(leave.startDate)} — {formatDate(leave.endDate)}
                        <span className="text-slate-400 ml-1">({leave.days} {leave.days === 1 ? 'day' : 'days'})</span>
                      </p>
                      <p className="text-xs text-slate-400 mt-0.5">{leave.reason}</p>
                      {leave.status === 'REJECTED' && (leave as { rejectionReason?: string }).rejectionReason && (
                        <p className="text-xs text-red-500 mt-1">
                          Reason: {(leave as { rejectionReason?: string }).rejectionReason}
                        </p>
                      )}
                    </div>
                    <div className="flex-shrink-0 text-right">
                      <p className="text-xs text-slate-400">{formatDate(leave.createdAt)}</p>
                      {leave.status === 'PENDING' && (
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-red-500 hover:text-red-600 mt-1"
                          onClick={() => cancelMutation.mutate(leave.id)}
                        >
                          Cancel
                        </Button>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Apply Leave Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>Apply for Leave</DialogTitle></DialogHeader>
          <form onSubmit={handleSubmit(d => applyMutation.mutate(d))}>
            <DialogBody className="space-y-4">
              <FormField label="Leave Type" error={errors.leaveTypeId?.message} required>
                <Select value={watch('leaveTypeId')} onValueChange={v => setValue('leaveTypeId', v)}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select leave type" />
                  </SelectTrigger>
                  <SelectContent>
                    {DEMO_LEAVE_TYPES.map(lt => (
                      <SelectItem key={lt.id} value={lt.id}>{lt.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </FormField>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Start Date" error={errors.startDate?.message} required>
                  <Input {...register('startDate')} type="date" />
                </FormField>
                <FormField label="End Date" error={errors.endDate?.message} required>
                  <Input {...register('endDate')} type="date" />
                </FormField>
              </div>
              {daysCount > 0 && (
                <div className="flex items-center gap-2 p-3 rounded-lg bg-brand-50 border border-brand-200">
                  <AlertCircle className="h-4 w-4 text-brand-600" />
                  <p className="text-sm text-brand-700 font-medium">
                    Applying for {daysCount} {daysCount === 1 ? 'day' : 'days'}
                  </p>
                </div>
              )}
              <FormField label="Reason" error={errors.reason?.message} required>
                <Textarea {...register('reason')} placeholder="Please provide a reason for your leave..." rows={3} />
              </FormField>
            </DialogBody>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>Cancel</Button>
              <Button type="submit" loading={applyMutation.isPending}>Submit Application</Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  )
}
