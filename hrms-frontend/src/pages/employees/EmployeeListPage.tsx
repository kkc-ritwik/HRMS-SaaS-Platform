import React, { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import {
  Plus, Filter, Download, MoreVertical, Eye, Edit, Trash2,
  UserCheck, UserX, Mail, Phone,
} from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/ui/page-header'
import { Button } from '@/components/ui/button'
import { SearchInput } from '@/components/ui/search-input'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Card } from '@/components/ui/card'
import { ConfirmDialog } from '@/components/ui/confirm-dialog'
import { EmptyState } from '@/components/ui/empty-state'
import {
  Table, TableHeader, TableBody, TableHead, TableRow, TableCell, TablePagination,
} from '@/components/ui/table'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
  Select, SelectContent, SelectItem, SelectTrigger, SelectValue,
} from '@/components/ui/select'
import { SkeletonTable } from '@/components/ui/skeleton'
import { employeeService } from '@/services/employeeService'
import { useDebounce } from '@/hooks/useDebounce'
import { usePagination } from '@/hooks/usePagination'
import { formatDate, getStatusColor } from '@/lib/utils'

const DEMO_EMPLOYEES = [
  { id: '1', employeeId: 'EMP001', fullName: 'Priya Sharma', email: 'priya@demo.com', phone: '+91 98765 43210', departmentName: 'Engineering', designationName: 'Senior Engineer', status: 'ACTIVE', employmentType: 'FULL_TIME', joinDate: '2022-01-15', avatar: undefined },
  { id: '2', employeeId: 'EMP002', fullName: 'Rahul Verma', email: 'rahul@demo.com', phone: '+91 98765 43211', departmentName: 'Product', designationName: 'Product Manager', status: 'ACTIVE', employmentType: 'FULL_TIME', joinDate: '2021-06-01', avatar: undefined },
  { id: '3', employeeId: 'EMP003', fullName: 'Sneha Gupta', email: 'sneha@demo.com', phone: '+91 98765 43212', departmentName: 'Marketing', designationName: 'Marketing Lead', status: 'ON_LEAVE', employmentType: 'FULL_TIME', joinDate: '2020-03-20', avatar: undefined },
  { id: '4', employeeId: 'EMP004', fullName: 'Amit Kumar', email: 'amit@demo.com', phone: '+91 98765 43213', departmentName: 'Sales', designationName: 'Sales Executive', status: 'ACTIVE', employmentType: 'FULL_TIME', joinDate: '2023-08-10', avatar: undefined },
  { id: '5', employeeId: 'EMP005', fullName: 'Anita Singh', email: 'anita@demo.com', phone: '+91 98765 43214', departmentName: 'HR', designationName: 'HR Manager', status: 'ACTIVE', employmentType: 'FULL_TIME', joinDate: '2019-11-05', avatar: undefined },
  { id: '6', employeeId: 'EMP006', fullName: 'Vikram Patel', email: 'vikram@demo.com', phone: '+91 98765 43215', departmentName: 'Finance', designationName: 'Financial Analyst', status: 'INACTIVE', employmentType: 'CONTRACT', joinDate: '2022-09-01', avatar: undefined },
  { id: '7', employeeId: 'EMP007', fullName: 'Deepa Nair', email: 'deepa@demo.com', phone: '+91 98765 43216', departmentName: 'Engineering', designationName: 'Frontend Developer', status: 'ACTIVE', employmentType: 'FULL_TIME', joinDate: '2023-02-14', avatar: undefined },
  { id: '8', employeeId: 'EMP008', fullName: 'Kiran Rao', email: 'kiran@demo.com', phone: '+91 98765 43217', departmentName: 'Operations', designationName: 'Operations Manager', status: 'ACTIVE', employmentType: 'FULL_TIME', joinDate: '2018-07-22', avatar: undefined },
]

const statusVariantMap: Record<string, 'success' | 'warning' | 'secondary' | 'destructive'> = {
  ACTIVE: 'success',
  ON_LEAVE: 'warning',
  INACTIVE: 'secondary',
  TERMINATED: 'destructive',
}

export function EmployeeListPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [deptFilter, setDeptFilter] = useState<string>('all')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [deleteId, setDeleteId] = useState<string | null>(null)
  const debouncedSearch = useDebounce(search, 300)
  const { page, pageSize, goToPage, changePageSize } = usePagination({ initialPageSize: 10 })

  const { data, isLoading, isError } = useQuery({
    queryKey: ['employees', debouncedSearch, deptFilter, statusFilter, page, pageSize],
    queryFn: async () => {
      try {
        return await employeeService.list({
          search: debouncedSearch || undefined,
          departmentId: deptFilter !== 'all' ? deptFilter : undefined,
          status: statusFilter !== 'all' ? statusFilter : undefined,
          page,
          pageSize,
        })
      } catch {
        // Return demo data on error
        return {
          data: {
            employees: DEMO_EMPLOYEES,
            total: DEMO_EMPLOYEES.length,
            page: 1,
            pageSize: 10,
          }
        }
      }
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id: string) => employeeService.delete(id),
    onSuccess: () => {
      toast.success('Employee deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['employees'] })
      setDeleteId(null)
    },
    onError: () => {
      toast.error('Failed to delete employee')
      setDeleteId(null)
    },
  })

  const employees = data?.data?.employees || DEMO_EMPLOYEES
  const total = data?.data?.total || DEMO_EMPLOYEES.length

  const filteredEmployees = employees.filter(e => {
    const matchSearch = !debouncedSearch ||
      e.fullName.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      e.email.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      e.employeeId.toLowerCase().includes(debouncedSearch.toLowerCase())
    const matchStatus = statusFilter === 'all' || e.status === statusFilter
    return matchSearch && matchStatus
  })

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Employees"
        description={`${total} employees total`}
        breadcrumbs={[{ label: 'People' }, { label: 'Employees' }]}
      >
        <Button variant="outline" size="sm">
          <Download className="h-4 w-4" />
          Export
        </Button>
        <Button size="sm" onClick={() => navigate('/employees/new')}>
          <Plus className="h-4 w-4" />
          Add Employee
        </Button>
      </PageHeader>

      <Card>
        {/* Filters bar */}
        <div className="flex flex-wrap items-center gap-3 px-5 pt-5 pb-4 border-b border-slate-100">
          <SearchInput
            value={search}
            onChange={e => setSearch(e.target.value)}
            onClear={() => setSearch('')}
            placeholder="Search employees..."
            containerClassName="w-64"
          />
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-36 h-9">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Status</SelectItem>
              <SelectItem value="ACTIVE">Active</SelectItem>
              <SelectItem value="ON_LEAVE">On Leave</SelectItem>
              <SelectItem value="INACTIVE">Inactive</SelectItem>
              <SelectItem value="TERMINATED">Terminated</SelectItem>
            </SelectContent>
          </Select>
          <Select value={deptFilter} onValueChange={setDeptFilter}>
            <SelectTrigger className="w-40 h-9">
              <SelectValue placeholder="Department" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Departments</SelectItem>
              <SelectItem value="engineering">Engineering</SelectItem>
              <SelectItem value="product">Product</SelectItem>
              <SelectItem value="marketing">Marketing</SelectItem>
              <SelectItem value="sales">Sales</SelectItem>
              <SelectItem value="hr">HR</SelectItem>
              <SelectItem value="finance">Finance</SelectItem>
            </SelectContent>
          </Select>
          <Button variant="outline" size="sm">
            <Filter className="h-4 w-4" />
            More Filters
          </Button>
          <div className="ml-auto text-sm text-slate-500">
            {filteredEmployees.length} results
          </div>
        </div>

        {/* Table */}
        {isLoading ? (
          <div className="p-5">
            <SkeletonTable rows={8} cols={6} />
          </div>
        ) : isError || filteredEmployees.length === 0 ? (
          <EmptyState
            icon={<UserCheck className="h-8 w-8" />}
            title="No employees found"
            description="Try adjusting your search or filters, or add a new employee."
            action={{ label: 'Add Employee', onClick: () => navigate('/employees/new') }}
          />
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead>ID</TableHead>
                  <TableHead>Department</TableHead>
                  <TableHead>Designation</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Join Date</TableHead>
                  <TableHead className="w-12"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredEmployees.map(emp => (
                  <TableRow
                    key={emp.id}
                    className="cursor-pointer"
                    onClick={() => navigate(`/employees/${emp.id}`)}
                  >
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Avatar name={emp.fullName} src={emp.avatar} size="sm" />
                        <div>
                          <p className="font-medium text-slate-900">{emp.fullName}</p>
                          <div className="flex items-center gap-1 text-xs text-slate-400 mt-0.5">
                            <Mail className="h-3 w-3" />
                            {emp.email}
                          </div>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <span className="font-mono text-xs text-slate-500 bg-slate-100 px-1.5 py-0.5 rounded">
                        {emp.employeeId}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span className="text-slate-700">{emp.departmentName || '—'}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-slate-600">{emp.designationName || '—'}</span>
                    </TableCell>
                    <TableCell>
                      <Badge variant={statusVariantMap[emp.status] || 'secondary'} dot>
                        {emp.status.replace('_', ' ')}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <span className="text-slate-600 text-xs">{formatDate(emp.joinDate)}</span>
                    </TableCell>
                    <TableCell onClick={e => e.stopPropagation()}>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon-sm">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => navigate(`/employees/${emp.id}`)}>
                            <Eye className="h-4 w-4" /> View Profile
                          </DropdownMenuItem>
                          <DropdownMenuItem onClick={() => navigate(`/employees/${emp.id}/edit`)}>
                            <Edit className="h-4 w-4" /> Edit
                          </DropdownMenuItem>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem destructive onClick={() => setDeleteId(emp.id)}>
                            <Trash2 className="h-4 w-4" /> Delete
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <TablePagination
              page={page}
              pageSize={pageSize}
              total={total}
              onPageChange={goToPage}
              onPageSizeChange={changePageSize}
            />
          </>
        )}
      </Card>

      <ConfirmDialog
        open={!!deleteId}
        onOpenChange={open => !open && setDeleteId(null)}
        title="Delete Employee"
        description="Are you sure you want to delete this employee? This action cannot be undone."
        confirmLabel="Delete Employee"
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        loading={deleteMutation.isPending}
      />
    </div>
  )
}
