import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { toast } from 'sonner'
import { Plus, Edit, Trash2, Building2, Users, MoreVertical } from 'lucide-react'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { SearchInput } from '@/components/ui/search-input'
import { Card, CardContent } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { EmptyState } from '@/components/ui/empty-state'
import { SkeletonCard } from '@/components/ui/skeleton'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogBody,
} from '@/components/ui/dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { departmentService, type Department } from '@/services/departmentService'
import { useDebounce } from '@/hooks/useDebounce'
import { getErrorMessage } from '@/lib/api'
import { generateColor } from '@/lib/utils'

const departmentSchema = z.object({
  name: z.string().min(1, 'Department name is required'),
  code: z.string().optional(),
  description: z.string().optional(),
})

type DepartmentFormData = z.infer<typeof departmentSchema>

const DEMO_DEPARTMENTS = [
  { id: '1', name: 'Engineering', code: 'ENG', description: 'Software development and engineering', employeeCount: 42, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
  { id: '2', name: 'Product', code: 'PROD', description: 'Product management and design', employeeCount: 18, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
  { id: '3', name: 'Marketing', code: 'MKT', description: 'Marketing and communications', employeeCount: 14, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
  { id: '4', name: 'Sales', code: 'SALES', description: 'Sales and business development', employeeCount: 28, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
  { id: '5', name: 'Human Resources', code: 'HR', description: 'HR and people operations', employeeCount: 8, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
  { id: '6', name: 'Finance', code: 'FIN', description: 'Finance and accounting', employeeCount: 12, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
  { id: '7', name: 'Operations', code: 'OPS', description: 'Operations and logistics', employeeCount: 22, createdAt: '2020-01-01', updatedAt: '2020-01-01' },
]

export function DepartmentsPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const debouncedSearch = useDebounce(search)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingDept, setEditingDept] = useState<Department | null>(null)
  const [deleteId, setDeleteId] = useState<string | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['departments', debouncedSearch],
    queryFn: async () => {
      try {
        return await departmentService.list({ search: debouncedSearch || undefined })
      } catch {
        return { data: { departments: DEMO_DEPARTMENTS, total: DEMO_DEPARTMENTS.length } }
      }
    },
  })

  const createMutation = useMutation({
    mutationFn: departmentService.create,
    onSuccess: () => {
      toast.success('Department created successfully')
      queryClient.invalidateQueries({ queryKey: ['departments'] })
      setIsModalOpen(false)
      reset()
    },
    onError: (err: unknown) => toast.error(getErrorMessage(err)),
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, ...data }: { id: string } & DepartmentFormData) =>
      departmentService.update(id, data),
    onSuccess: () => {
      toast.success('Department updated successfully')
      queryClient.invalidateQueries({ queryKey: ['departments'] })
      setIsModalOpen(false)
      setEditingDept(null)
      reset()
    },
    onError: (err: unknown) => toast.error(getErrorMessage(err)),
  })

  const deleteMutation = useMutation({
    mutationFn: departmentService.delete,
    onSuccess: () => {
      toast.success('Department deleted')
      queryClient.invalidateQueries({ queryKey: ['departments'] })
      setDeleteId(null)
    },
    onError: (err: unknown) => toast.error(getErrorMessage(err)),
  })

  const { register, handleSubmit, reset, setValue, formState: { errors } } = useForm<DepartmentFormData>({
    resolver: zodResolver(departmentSchema),
  })

  const openEdit = (dept: Department) => {
    setEditingDept(dept)
    setValue('name', dept.name)
    setValue('code', dept.code || '')
    setValue('description', dept.description || '')
    setIsModalOpen(true)
  }

  const openCreate = () => {
    setEditingDept(null)
    reset()
    setIsModalOpen(true)
  }

  const onSubmit = (formData: DepartmentFormData) => {
    if (editingDept) {
      updateMutation.mutate({ id: editingDept.id, ...formData })
    } else {
      createMutation.mutate(formData)
    }
  }

  const departments = data?.data?.departments || DEMO_DEPARTMENTS

  const filtered = departments.filter((d: any) =>
    !debouncedSearch ||
    d.name.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
    d.code?.toLowerCase().includes(debouncedSearch.toLowerCase())
  )

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Departments"
        description={`${departments.length} departments`}
        breadcrumbs={[{ label: 'People' }, { label: 'Departments' }]}
      >
        <Button size="sm" onClick={openCreate}>
          <Plus className="h-4 w-4" />
          Add Department
        </Button>
      </PageHeader>

      {/* Search */}
      <div className="flex items-center gap-3">
        <SearchInput
          value={search}
          onChange={e => setSearch(e.target.value)}
          onClear={() => setSearch('')}
          placeholder="Search departments..."
          containerClassName="w-64"
        />
        <span className="text-sm text-slate-500">{filtered.length} results</span>
      </div>

      {/* Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={<Building2 className="h-8 w-8" />}
          title="No departments found"
          description="Create your first department to get started"
          action={{ label: 'Add Department', onClick: openCreate }}
        />
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {filtered.map((dept: any) => {
            const colorClass = generateColor(dept.name)
            return (
              <Card key={dept.id} className="hover:shadow-card-hover transition-shadow duration-200">
                <CardContent className="pt-5">
                  <div className="flex items-start justify-between mb-4">
                    <div className={`flex h-11 w-11 items-center justify-center rounded-xl text-white font-bold text-lg ${colorClass}`}>
                      {dept.name.charAt(0)}
                    </div>
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button variant="ghost" size="icon-sm">
                          <MoreVertical className="h-4 w-4" />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end">
                        <DropdownMenuItem onClick={() => openEdit(dept)}>
                          <Edit className="h-4 w-4" />
                          Edit
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem destructive onClick={() => setDeleteId(dept.id)}>
                          <Trash2 className="h-4 w-4" />
                          Delete
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>

                  <h3 className="font-semibold text-slate-900 mb-0.5">{dept.name}</h3>
                  {dept.code && (
                    <Badge variant="secondary" className="mb-2 text-[10px]">{dept.code}</Badge>
                  )}
                  {dept.description && (
                    <p className="text-xs text-slate-500 mb-3 line-clamp-2">{dept.description}</p>
                  )}

                  <div className="flex items-center gap-1.5 text-sm text-slate-600 border-t border-slate-50 pt-3">
                    <Users className="h-3.5 w-3.5 text-slate-400" />
                    <span>{dept.employeeCount || 0} employees</span>
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}

      {/* Add/Edit Modal */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent size="sm">
          <DialogHeader>
            <DialogTitle>{editingDept ? 'Edit Department' : 'Add Department'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)}>
            <DialogBody className="space-y-4">
              <FormField label="Department Name" error={errors.name?.message} required>
                <Input {...register('name')} placeholder="Engineering" />
              </FormField>
              <FormField label="Code" hint="Short identifier for the department">
                <Input {...register('code')} placeholder="ENG" />
              </FormField>
              <FormField label="Description">
                <Input {...register('description')} placeholder="Brief description..." />
              </FormField>
            </DialogBody>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button
                type="submit"
                loading={createMutation.isPending || updateMutation.isPending}
              >
                {editingDept ? 'Update' : 'Create'} Department
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={open => !open && setDeleteId(null)}
        title="Delete Department"
        description="Are you sure you want to delete this department?"
        confirmLabel="Delete"
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        loading={deleteMutation.isPending}
      />
    </div>
  )
}
