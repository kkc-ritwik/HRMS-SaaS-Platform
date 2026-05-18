import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Plus, Edit, Trash2, Star, MoreVertical, Users } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { SearchInput } from '@/components/ui/search-input'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { EmptyState } from '@/components/ui/empty-state'
import { SkeletonTable } from '@/components/ui/skeleton'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Table, TableHeader, TableBody, TableHead, TableRow, TableCell, TablePagination,
} from '@/components/ui/table'
import { designationService, type Designation } from '@/services/departmentService'
import { useDebounce } from '@/hooks/useDebounce'
import { usePagination } from '@/hooks/usePagination'
import { getErrorMessage } from '@/lib/api'

const schema = z.object({
  name: z.string().min(1, 'Designation name is required'),
  code: z.string().optional(),
  description: z.string().optional(),
  level: z.coerce.number().optional(),
})

type FormData = z.infer<typeof schema>

const DEMO_DESIGNATIONS = [
  { id: '1', name: 'Software Engineer', code: 'SE', level: 3, employeeCount: 28, description: '', createdAt: '', updatedAt: '' },
  { id: '2', name: 'Senior Software Engineer', code: 'SSE', level: 4, employeeCount: 18, description: '', createdAt: '', updatedAt: '' },
  { id: '3', name: 'Tech Lead', code: 'TL', level: 5, employeeCount: 8, description: '', createdAt: '', updatedAt: '' },
  { id: '4', name: 'Engineering Manager', code: 'EM', level: 6, employeeCount: 4, description: '', createdAt: '', updatedAt: '' },
  { id: '5', name: 'Product Manager', code: 'PM', level: 5, employeeCount: 6, description: '', createdAt: '', updatedAt: '' },
  { id: '6', name: 'HR Manager', code: 'HRM', level: 5, employeeCount: 3, description: '', createdAt: '', updatedAt: '' },
  { id: '7', name: 'Sales Executive', code: 'SE2', level: 3, employeeCount: 14, description: '', createdAt: '', updatedAt: '' },
  { id: '8', name: 'Marketing Lead', code: 'ML', level: 4, employeeCount: 5, description: '', createdAt: '', updatedAt: '' },
]

export function DesignationsPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebounce(search)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<Designation | null>(null)
  const [deleteId, setDeleteId] = useState<string | null>(null)
  const { page, pageSize, goToPage, changePageSize } = usePagination()

  const { data, isLoading } = useQuery({
    queryKey: ['designations', debouncedSearch, page, pageSize],
    queryFn: async () => {
      try {
        return await designationService.list({ search: debouncedSearch || undefined, page, pageSize })
      } catch {
        return { data: { designations: DEMO_DESIGNATIONS, total: DEMO_DESIGNATIONS.length } }
      }
    },
  })

  const createMutation = useMutation({
    mutationFn: designationService.create,
    onSuccess: () => { toast.success('Designation created'); queryClient.invalidateQueries({ queryKey: ['designations'] }); setIsModalOpen(false); reset() },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, ...d }: { id: string } & FormData) => designationService.update(id, d),
    onSuccess: () => { toast.success('Designation updated'); queryClient.invalidateQueries({ queryKey: ['designations'] }); setIsModalOpen(false); setEditingItem(null); reset() },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const deleteMutation = useMutation({
    mutationFn: designationService.delete,
    onSuccess: () => { toast.success('Designation deleted'); queryClient.invalidateQueries({ queryKey: ['designations'] }); setDeleteId(null) },
    onError: (e: unknown) => toast.error(getErrorMessage(e)),
  })

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<FormData>({ resolver: zodResolver(schema) as any })

  const openEdit = (item: Designation) => {
    setEditingItem(item); setValue('name', item.name); setValue('code', item.code || ''); setValue('description', item.description || ''); setValue('level', item.level); setIsModalOpen(true)
  }

  const designations = data?.data?.designations || DEMO_DESIGNATIONS
  const total = data?.data?.total || DEMO_DESIGNATIONS.length

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="Designations" description={`${total} designations`} breadcrumbs={[{ label: 'People' }, { label: 'Designations' }]}>
        <Button size="sm" onClick={() => { setEditingItem(null); reset(); setIsModalOpen(true) }}>
          <Plus className="h-4 w-4" /> Add Designation
        </Button>
      </PageHeader>

      <Card>
        <div className="flex items-center gap-3 px-5 pt-5 pb-4 border-b border-slate-100">
          <SearchInput value={search} onChange={e => setSearch(e.target.value)} onClear={() => setSearch('')} placeholder="Search designations..." containerClassName="w-64" />
          <span className="ml-auto text-sm text-slate-500">{designations.length} results</span>
        </div>

        {isLoading ? (
          <div className="p-5"><SkeletonTable rows={6} cols={4} /></div>
        ) : designations.length === 0 ? (
          <EmptyState icon={<Star className="h-8 w-8" />} title="No designations found" action={{ label: 'Add Designation', onClick: () => setIsModalOpen(true) }} />
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Code</TableHead>
                  <TableHead>Level</TableHead>
                  <TableHead>Employees</TableHead>
                  <TableHead className="w-12"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {designations.map((item: any) => (
                  <TableRow key={item.id}>
                    <TableCell>
                      <div className="flex items-center gap-2.5">
                        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violet-100">
                          <Star className="h-4 w-4 text-violet-600" />
                        </div>
                        <span className="font-medium text-slate-900">{item.name}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      {item.code ? (
                        <span className="font-mono text-xs bg-slate-100 text-slate-600 px-2 py-0.5 rounded">{item.code}</span>
                      ) : '—'}
                    </TableCell>
                    <TableCell>
                      {item.level ? (
                        <div className="flex items-center gap-1">
                          {Array.from({ length: Math.min(item.level, 6) }).map((_, i) => (
                            <div key={i} className={`h-1.5 w-4 rounded-full ${i < (item.level || 0) ? 'bg-brand-500' : 'bg-slate-200'}`} />
                          ))}
                          <span className="ml-1 text-xs text-slate-500">L{item.level}</span>
                        </div>
                      ) : '—'}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1 text-slate-600">
                        <Users className="h-3.5 w-3.5 text-slate-400" />
                        {item.employeeCount || 0}
                      </div>
                    </TableCell>
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon-sm"><MoreVertical className="h-4 w-4" /></Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => openEdit(item)}><Edit className="h-4 w-4" /> Edit</DropdownMenuItem>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem destructive onClick={() => setDeleteId(item.id)}><Trash2 className="h-4 w-4" /> Delete</DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <TablePagination page={page} pageSize={pageSize} total={total} onPageChange={goToPage} onPageSizeChange={changePageSize} />
          </>
        )}
      </Card>

      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent size="sm">
          <DialogHeader><DialogTitle>{editingItem ? 'Edit Designation' : 'Add Designation'}</DialogTitle></DialogHeader>
          <form onSubmit={handleSubmit(d => editingItem ? updateMutation.mutate({ id: editingItem.id, ...(d as any) }) : createMutation.mutate(d))}>
            <DialogBody className="space-y-4">
              <FormField label="Designation Name" error={errors.name?.message} required>
                <Input {...register('name')} placeholder="Software Engineer" />
              </FormField>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Code">
                  <Input {...register('code')} placeholder="SE" />
                </FormField>
                <FormField label="Level" hint="1-10 seniority scale">
                  <Input {...register('level')} type="number" min={1} max={10} placeholder="3" />
                </FormField>
              </div>
              <FormField label="Description">
                <Input {...register('description')} placeholder="Brief description" />
              </FormField>
            </DialogBody>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>Cancel</Button>
              <Button type="submit" loading={createMutation.isPending || updateMutation.isPending}>
                {editingItem ? 'Update' : 'Create'} Designation
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog open={!!deleteId} onOpenChange={open => !open && setDeleteId(null)} title="Delete Designation" description="This will remove the designation permanently." confirmLabel="Delete" onConfirm={() => deleteId && deleteMutation.mutate(deleteId)} loading={deleteMutation.isPending} />
    </div>
  )
}
