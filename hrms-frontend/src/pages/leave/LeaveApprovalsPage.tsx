import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckCircle, XCircle, Clock, Search, Filter } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { FormField } from '@/components/ui/form-field'
import { EmptyState } from '@/components/ui/empty-state'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import { leaveService } from '@/services/leaveService'
import { formatDate } from '@/lib/utils'
import { getErrorMessage } from '@/lib/api'

const DEMO_PENDING = [
  { id: '1', employeeName: 'Priya Sharma', employeeId: 'EMP001', leaveTypeName: 'Annual Leave', startDate: '2025-04-10', endDate: '2025-04-12', days: 3, reason: 'Family vacation to Goa', createdAt: '2025-04-01', status: 'PENDING' },
  { id: '2', employeeName: 'Rahul Verma', employeeId: 'EMP002', leaveTypeName: 'Sick Leave', startDate: '2025-04-08', endDate: '2025-04-08', days: 1, reason: 'Doctor appointment', createdAt: '2025-04-07', status: 'PENDING' },
  { id: '3', employeeName: 'Sneha Gupta', employeeId: 'EMP003', leaveTypeName: 'Casual Leave', startDate: '2025-04-15', endDate: '2025-04-16', days: 2, reason: 'Personal work', createdAt: '2025-04-05', status: 'PENDING' },
]

const DEMO_RESOLVED = [
  { id: '4', employeeName: 'Amit Kumar', employeeId: 'EMP004', leaveTypeName: 'Annual Leave', startDate: '2025-03-20', endDate: '2025-03-22', days: 3, reason: 'Wedding anniversary', createdAt: '2025-03-15', status: 'APPROVED' },
  { id: '5', employeeName: 'Deepa Nair', employeeId: 'EMP007', leaveTypeName: 'Casual Leave', startDate: '2025-03-10', endDate: '2025-03-10', days: 1, reason: 'Bank work', createdAt: '2025-03-09', status: 'REJECTED' },
]

export function LeaveApprovalsPage() {
  const queryClient = useQueryClient()
  const [rejectModal, setRejectModal] = useState<{ id: string; name: string } | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const { data } = useQuery({
    queryKey: ['leave-approvals'],
    queryFn: async () => {
      try {
        return await leaveService.listApplications({ status: 'PENDING' })
      } catch {
        return { data: DEMO_PENDING }
      }
    },
  })

  const { data: resolvedData } = useQuery({
    queryKey: ['leave-resolved'],
    queryFn: async () => {
      try {
        return await leaveService.listApplications({})
      } catch {
        return { data: DEMO_RESOLVED }
      }
    },
  })

  const approveMutation = useMutation({
    mutationFn: (id: string) => leaveService.approveLeave(id),
    onSuccess: () => {
      toast.success('Leave approved successfully')
      queryClient.invalidateQueries({ queryKey: ['leave-approvals'] })
      queryClient.invalidateQueries({ queryKey: ['leave-resolved'] })
    },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => leaveService.rejectLeave(id, reason),
    onSuccess: () => {
      toast.success('Leave rejected')
      queryClient.invalidateQueries({ queryKey: ['leave-approvals'] })
      queryClient.invalidateQueries({ queryKey: ['leave-resolved'] })
      setRejectModal(null)
      setRejectReason('')
    },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const pending = data?.data || DEMO_PENDING
  const resolved = resolvedData?.data || DEMO_RESOLVED

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Leave Approvals"
        description={`${pending.length} pending approval${pending.length !== 1 ? 's' : ''}`}
        breadcrumbs={[{ label: 'Time & Leave' }, { label: 'Approvals' }]}
      />

      <Tabs defaultValue="pending">
        <TabsList>
          <TabsTrigger value="pending" className="gap-2">
            Pending
            {pending.length > 0 && (
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-amber-500 text-[10px] text-white font-bold">
                {pending.length}
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger value="resolved">Resolved</TabsTrigger>
        </TabsList>

        <TabsContent value="pending">
          {pending.length === 0 ? (
            <Card>
              <EmptyState
                icon={<CheckCircle className="h-8 w-8" />}
                title="All caught up!"
                description="No leave requests pending approval"
              />
            </Card>
          ) : (
            <div className="space-y-3">
              {pending.map((leave: any) => (
                <Card key={leave.id}>
                  <CardContent className="pt-5">
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3 flex-1 min-w-0">
                        <Avatar name={leave.employeeName} size="md" />
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-semibold text-slate-900">{leave.employeeName}</span>
                            <span className="text-xs text-slate-400 font-mono">{leave.employeeId}</span>
                            <Badge variant="warning" dot>Pending</Badge>
                          </div>
                          <div className="flex items-center gap-2 mt-1 flex-wrap">
                            <Badge variant="info">{leave.leaveTypeName}</Badge>
                            <span className="text-sm text-slate-600">
                              {formatDate(leave.startDate)} — {formatDate(leave.endDate)}
                            </span>
                            <span className="text-xs text-slate-400">({leave.days} {leave.days === 1 ? 'day' : 'days'})</span>
                          </div>
                          <p className="text-sm text-slate-600 mt-1.5 italic">"{leave.reason}"</p>
                          <p className="text-xs text-slate-400 mt-1">Applied: {formatDate(leave.createdAt)}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-2 flex-shrink-0">
                        <Button
                          variant="outline"
                          size="sm"
                          className="border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                          onClick={() => setRejectModal({ id: leave.id, name: leave.employeeName })}
                        >
                          <XCircle className="h-4 w-4" />
                          Reject
                        </Button>
                        <Button
                          size="sm"
                          className="bg-green-600 hover:bg-green-700"
                          onClick={() => approveMutation.mutate(leave.id)}
                          loading={approveMutation.isPending}
                        >
                          <CheckCircle className="h-4 w-4" />
                          Approve
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="resolved">
          <Card>
            <CardHeader><CardTitle>Resolved Requests</CardTitle></CardHeader>
            <CardContent className="pt-0">
              <div className="space-y-3">
                {resolved.map((leave: any) => (
                  <div key={leave.id} className="flex items-start gap-3 py-3 border-b border-slate-50 last:border-0">
                    <Avatar name={leave.employeeName} size="sm" />
                    <div className="flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-sm font-medium text-slate-800">{leave.employeeName}</span>
                        <Badge variant={leave.status === 'APPROVED' ? 'success' : 'destructive'} dot>
                          {leave.status}
                        </Badge>
                      </div>
                      <p className="text-xs text-slate-500">
                        {leave.leaveTypeName} · {formatDate(leave.startDate)} — {formatDate(leave.endDate)}
                      </p>
                    </div>
                    <span className="text-xs text-slate-400 flex-shrink-0">{formatDate(leave.createdAt)}</span>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Reject modal */}
      <Dialog open={!!rejectModal} onOpenChange={open => !open && setRejectModal(null)}>
        <DialogContent size="sm">
          <DialogHeader>
            <DialogTitle>Reject Leave Request</DialogTitle>
          </DialogHeader>
          <DialogBody className="space-y-4">
            <p className="text-sm text-slate-600">
              You are rejecting <strong>{rejectModal?.name}</strong>'s leave request. Please provide a reason.
            </p>
            <FormField label="Rejection Reason" required>
              <Textarea
                value={rejectReason}
                onChange={e => setRejectReason(e.target.value)}
                placeholder="Please explain why this request is being rejected..."
                rows={3}
              />
            </FormField>
          </DialogBody>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectModal(null)}>Cancel</Button>
            <Button
              variant="destructive"
              onClick={() => rejectModal && rejectMutation.mutate({ id: rejectModal.id, reason: rejectReason })}
              loading={rejectMutation.isPending}
              disabled={!rejectReason.trim()}
            >
              Reject Leave
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
