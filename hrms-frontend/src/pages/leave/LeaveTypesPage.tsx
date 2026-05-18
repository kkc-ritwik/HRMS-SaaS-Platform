import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Plus, Edit, Trash2, Calendar, MoreVertical, CheckCircle, XCircle } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { Card } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Switch } from '@/components/ui/switch'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { EmptyState } from '@/components/ui/empty-state'
import {
  Table, TableHeader, TableBody, TableHead, TableRow, TableCell,
} from '@/components/ui/table'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { leaveService } from '@/services/leaveService'
import { getErrorMessage } from '@/lib/api'

const schema = z.object({
  name: z.string().min(1, 'Name is required'),
  code: z.string().min(1, 'Code is required'),
  daysAllowed: z.coerce.number().min(1, 'Must be at least 1 day'),
  isPaid: z.boolean().default(true),
  carryForward: z.boolean().default(false),
  requiresApproval: z.boolean().default(true),
  description: z.string().optional(),
})

type FormData = z.infer<typeof schema>

const DEMO_LEAVE_TYPES = [
  { id: '1', name: 'Annual Leave', code: 'AL', daysAllowed: 21, isPaid: true, carryForward: true, carryForwardLimit: 5, requiresApproval: true, color: '#4f46e5', createdAt: '' },
  { id: '2', name: 'Sick Leave', code: 'SL', daysAllowed: 12, isPaid: true, carryForward: false, requiresApproval: false, color: '#f59e0b', createdAt: '' },
  { id: '3', name: 'Casual Leave', code: 'CL', daysAllowed: 6, isPaid: true, carryForward: false, requiresApproval: true, color: '#8b5cf6', createdAt: '' },
  { id: '4', name: 'Maternity Leave', code: 'ML', daysAllowed: 180, isPaid: true, carryForward: false, requiresApproval: true, applicableGender: 'FEMALE', color: '#ec4899', createdAt: '' },
  { id: '5', name: 'Paternity Leave', code: 'PL', daysAllowed: 15, isPaid: true, carryForward: false, requiresApproval: true, applicableGender: 'MALE', color: '#06b6d4', createdAt: '' },
  { id: '6', name: 'Unpaid Leave', code: 'UL', daysAllowed: 30, isPaid: false, carryForward: false, requiresApproval: true, color: '#6b7280', createdAt: '' },
]

export function LeaveTypesPage() {
  const queryClient = useQueryClient()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<(typeof DEMO_LEAVE_TYPES)[0] | null>(null)
  const [deleteId, setDeleteId] = useState<string | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['leave-types'],
    queryFn: async () => {
      try {
        return await leaveService.listLeaveTypes()
      } catch {
        return { data: DEMO_LEAVE_TYPES }
      }
    },
  })

  const createMutation = useMutation({
    mutationFn: leaveService.createLeaveType,
    onSuccess: () => { toast.success('Leave type created'); queryClient.invalidateQueries({ queryKey: ['leave-types'] }); setIsModalOpen(false); reset() },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, ...d }: { id: string } & FormData) => leaveService.updateLeaveType(id, d),
    onSuccess: () => { toast.success('Leave type updated'); queryClient.invalidateQueries({ queryKey: ['leave-types'] }); setIsModalOpen(false); setEditingItem(null); reset() },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const deleteMutation = useMutation({
    mutationFn: leaveService.deleteLeaveType,
    onSuccess: () => { toast.success('Leave type deleted'); queryClient.invalidateQueries({ queryKey: ['leave-types'] }); setDeleteId(null) },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const { register, handleSubmit, reset, setValue, watch, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema) as any,
    defaultValues: { isPaid: true, carryForward: false, requiresApproval: true },
  })

  const leaveTypes = data?.data || DEMO_LEAVE_TYPES

  const BoolIcon = ({ val }: { val: boolean }) =>
    val ? <CheckCircle className="h-4 w-4 text-green-500" /> : <XCircle className="h-4 w-4 text-slate-300" />

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Leave Types" description="Configure leave policies" breadcrumbs={[{ label: 'Time & Leave' }, { label: 'Leave Types' }]}>
        <Button size="sm" onClick={() => { setEditingItem(null); reset({ isPaid: true, carryForward: false, requiresApproval: true }); setIsModalOpen(true) }}>
          <Plus className="h-4 w-4" /> Add Leave Type
        </Button>
      </PageHeader>

      <Card>
        {isLoading ? (
          <div className="p-8 text-center text-slate-400">Loading...</div>
        ) : leaveTypes.length === 0 ? (
          <EmptyState icon={<Calendar className="h-8 w-8" />} title="No leave types" action={{ label: 'Add Leave Type', onClick: () => setIsModalOpen(true) }} />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Leave Type</TableHead>
                <TableHead>Code</TableHead>
                <TableHead>Days Allowed</TableHead>
                <TableHead>Paid</TableHead>
                <TableHead>Carry Forward</TableHead>
                <TableHead>Requires Approval</TableHead>
                <TableHead className="w-12"></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {leaveTypes.map((lt: any) => (
                <TableRow key={lt.id}>
                  <TableCell>
                    <div className="flex items-center gap-2.5">
                      <div className="h-3 w-3 rounded-full flex-shrink-0" style={{ backgroundColor: lt.color || '#4f46e5' }} />
                      <span className="font-medium text-slate-900">{lt.name}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <span className="font-mono text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded">{lt.code}</span>
                  </TableCell>
                  <TableCell>
                    <Badge variant="info">{lt.daysAllowed} days</Badge>
                  </TableCell>
                  <TableCell><BoolIcon val={lt.isPaid} /></TableCell>
                  <TableCell><BoolIcon val={lt.carryForward} /></TableCell>
                  <TableCell><BoolIcon val={lt.requiresApproval} /></TableCell>
                  <TableCell>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon-sm"><MoreVertical className="h-4 w-4" /></Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => {
                          setEditingItem(lt)
                          setValue('name', lt.name); setValue('code', lt.code)
                          setValue('daysAllowed', lt.daysAllowed); setValue('isPaid', lt.isPaid)
                          setValue('carryForward', lt.carryForward); setValue('requiresApproval', lt.requiresApproval)
                          setValue('description', '')
                          setIsModalOpen(true)
                        }}>
                          <Edit className="h-4 w-4" /> Edit
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem destructive onClick={() => setDeleteId(lt.id)}>
                          <Trash2 className="h-4 w-4" /> Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>

      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent>
          <DialogHeader><DialogTitle>{editingItem ? 'Edit Leave Type' : 'Add Leave Type'}</DialogTitle></DialogHeader>
          <form onSubmit={handleSubmit(d => editingItem ? updateMutation.mutate({ id: editingItem.id, ...(d as any) }) : createMutation.mutate(d))}>
            <DialogBody className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Leave Type Name" error={errors.name?.message} required>
                  <Input {...register('name')} placeholder="Annual Leave" />
                </FormField>
                <FormField label="Code" error={errors.code?.message} required>
                  <Input {...register('code')} placeholder="AL" />
                </FormField>
              </div>
              <FormField label="Days Allowed per Year" error={errors.daysAllowed?.message} required>
                <Input {...register('daysAllowed')} type="number" min={1} placeholder="21" />
              </FormField>
              <FormField label="Description">
                <Input {...register('description')} placeholder="Brief description" />
              </FormField>
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                  <div>
                    <Label>Paid Leave</Label>
                    <p className="text-xs text-slate-500">Employee is paid during this leave</p>
                  </div>
                  <Switch checked={watch('isPaid')} onCheckedChange={v => setValue('isPaid', v)} />
                </div>
                <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                  <div>
                    <Label>Carry Forward</Label>
                    <p className="text-xs text-slate-500">Unused days carry over to next year</p>
                  </div>
                  <Switch checked={watch('carryForward')} onCheckedChange={v => setValue('carryForward', v)} />
                </div>
                <div className="flex items-center justify-between p-3 bg-slate-50 rounded-lg">
                  <div>
                    <Label>Requires Approval</Label>
                    <p className="text-xs text-slate-500">Manager must approve this leave</p>
                  </div>
                  <Switch checked={watch('requiresApproval')} onCheckedChange={v => setValue('requiresApproval', v)} />
                </div>
              </div>
            </DialogBody>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>Cancel</Button>
              <Button type="submit" loading={createMutation.isPending || updateMutation.isPending}>
                {editingItem ? 'Update' : 'Create'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={open => !open && setDeleteId(null)} title="Delete Leave Type" confirmLabel="Delete" onConfirm={() => deleteId && deleteMutation.mutate(deleteId)} loading={deleteMutation.isPending} />
    </div>
  )
}
