import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import {
  ArrowLeft, Edit, Mail, Phone, MapPin, Calendar, Briefcase,
  Building2, User, CreditCard, FileText, Clock, Shield,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Avatar } from '@/components/ui/avatar'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { employeeService } from '@/services/employeeService'
import { formatDate, getStatusColor } from '@/lib/utils'

const DEMO_EMPLOYEE = {
  id: '1',
  employeeId: 'EMP001',
  firstName: 'Priya',
  lastName: 'Sharma',
  fullName: 'Priya Sharma',
  email: 'priya.sharma@demo.com',
  phone: '+91 98765 43210',
  departmentName: 'Engineering',
  designationName: 'Senior Software Engineer',
  locationName: 'Bangalore',
  managerName: 'Vikram Patel',
  employmentType: 'FULL_TIME' as const,
  status: 'ACTIVE' as const,
  joinDate: '2022-01-15',
  gender: 'FEMALE' as const,
  dateOfBirth: '1993-07-22',
  address: '123 MG Road, Koramangala',
  city: 'Bangalore',
  state: 'Karnataka',
  country: 'India',
  pincode: '560034',
  bankName: 'HDFC Bank',
  bankAccountNumber: '****4521',
  ifscCode: 'HDFC0001234',
  panNumber: 'ABCDE1234F',
  emergencyContact: {
    name: 'Rajesh Sharma',
    phone: '+91 98765 11111',
    relation: 'Father',
  },
  createdAt: '2022-01-10',
  updatedAt: '2024-01-15',
}

const statusVariantMap: Record<string, 'success' | 'warning' | 'secondary' | 'destructive'> = {
  ACTIVE: 'success',
  ON_LEAVE: 'warning',
  INACTIVE: 'secondary',
  TERMINATED: 'destructive',
}

function InfoRow({ label, value, icon: Icon }: { label: string; value?: string | null; icon?: React.ComponentType<{ className?: string }> }) {
  return (
    <div className="flex items-start gap-3 py-3 border-b border-slate-50 last:border-0">
      {Icon && <Icon className="h-4 w-4 text-slate-400 mt-0.5 flex-shrink-0" />}
      <div className="flex-1 min-w-0">
        <p className="text-xs text-slate-400 mb-0.5">{label}</p>
        <p className="text-sm text-slate-800 font-medium">{value || '—'}</p>
      </div>
    </div>
  )
}

export function EmployeeDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data, isLoading } = useQuery({
    queryKey: ['employee', id],
    queryFn: async () => {
      try {
        if (!id) throw new Error('No ID')
        return await employeeService.getById(id)
      } catch {
        return { data: DEMO_EMPLOYEE }
      }
    },
  })

  const employee = data?.data || DEMO_EMPLOYEE

  if (isLoading) {
    return (
      <div className="space-y-5">
        <Skeleton className="h-8 w-48" />
        <div className="grid grid-cols-4 gap-4">
          {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-32 rounded-xl" />)}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader
        title="Employee Profile"
        breadcrumbs={[{ label: 'People' }, { label: 'Employees', href: '/employees' }, { label: employee.fullName }]}
      >
        <Button variant="outline" size="sm" onClick={() => navigate('/employees')}>
          <ArrowLeft className="h-4 w-4" />
          Back
        </Button>
        <Button size="sm" onClick={() => navigate(`/employees/${id}/edit`)}>
          <Edit className="h-4 w-4" />
          Edit
        </Button>
      </PageHeader>

      {/* Profile card */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5">
            <Avatar name={employee.fullName} size="xl" />
            <div className="flex-1 min-w-0">
              <div className="flex flex-wrap items-center gap-3 mb-1">
                <h2 className="text-xl font-bold text-slate-900">{employee.fullName}</h2>
                <Badge variant={statusVariantMap[employee.status] || 'secondary'} dot>
                  {employee.status.replace('_', ' ')}
                </Badge>
              </div>
              <p className="text-sm text-slate-500">{employee.designationName} · {employee.departmentName}</p>
              <div className="flex flex-wrap items-center gap-4 mt-3">
                <a href={`mailto:${employee.email}`} className="flex items-center gap-1.5 text-sm text-slate-600 hover:text-brand-600 transition-colors">
                  <Mail className="h-3.5 w-3.5" />
                  {employee.email}
                </a>
                {employee.phone && (
                  <span className="flex items-center gap-1.5 text-sm text-slate-600">
                    <Phone className="h-3.5 w-3.5" />
                    {employee.phone}
                  </span>
                )}
                {employee.locationName && (
                  <span className="flex items-center gap-1.5 text-sm text-slate-600">
                    <MapPin className="h-3.5 w-3.5" />
                    {employee.locationName}
                  </span>
                )}
              </div>
            </div>
            <div className="flex flex-col gap-2 text-sm text-right">
              <span className="font-mono text-xs bg-brand-50 text-brand-700 px-2 py-1 rounded border border-brand-200">
                {employee.employeeId}
              </span>
              <span className="text-xs text-slate-400">Joined {formatDate(employee.joinDate)}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Tabs defaultValue="overview">
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="personal">Personal</TabsTrigger>
          <TabsTrigger value="employment">Employment</TabsTrigger>
          <TabsTrigger value="documents">Documents</TabsTrigger>
          <TabsTrigger value="payroll">Payroll</TabsTrigger>
          <TabsTrigger value="leave">Leave History</TabsTrigger>
        </TabsList>

        {/* Overview */}
        <TabsContent value="overview">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card>
              <CardHeader><CardTitle className="text-sm">Work Info</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <InfoRow label="Department" value={employee.departmentName} icon={Building2} />
                <InfoRow label="Designation" value={employee.designationName} icon={Briefcase} />
                <InfoRow label="Manager" value={employee.managerName} icon={User} />
                <InfoRow label="Employment Type" value={employee.employmentType.replace('_', ' ')} icon={Briefcase} />
                <InfoRow label="Join Date" value={formatDate(employee.joinDate)} icon={Calendar} />
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-sm">Contact Info</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <InfoRow label="Email" value={employee.email} icon={Mail} />
                <InfoRow label="Phone" value={employee.phone} icon={Phone} />
                <InfoRow label="City" value={employee.city} icon={MapPin} />
                <InfoRow label="State" value={employee.state} />
                <InfoRow label="Country" value={employee.country} />
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-sm">Emergency Contact</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <InfoRow label="Name" value={employee.emergencyContact?.name} icon={User} />
                <InfoRow label="Phone" value={employee.emergencyContact?.phone} icon={Phone} />
                <InfoRow label="Relation" value={employee.emergencyContact?.relation} />
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Personal */}
        <TabsContent value="personal">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card>
              <CardHeader><CardTitle className="text-sm">Personal Details</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <InfoRow label="Date of Birth" value={formatDate(employee.dateOfBirth)} icon={Calendar} />
                <InfoRow label="Gender" value={employee.gender} icon={User} />
                <InfoRow label="PAN Number" value={employee.panNumber} icon={Shield} />
                <InfoRow label="Address" value={employee.address} icon={MapPin} />
                <InfoRow label="City" value={employee.city} />
                <InfoRow label="Pincode" value={employee.pincode} />
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-sm">Emergency Contact</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <InfoRow label="Name" value={employee.emergencyContact?.name} icon={User} />
                <InfoRow label="Phone" value={employee.emergencyContact?.phone} icon={Phone} />
                <InfoRow label="Relationship" value={employee.emergencyContact?.relation} />
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Employment */}
        <TabsContent value="employment">
          <Card>
            <CardHeader><CardTitle className="text-sm">Employment Details</CardTitle></CardHeader>
            <CardContent className="pt-0">
              <div className="grid grid-cols-2 gap-0">
                <InfoRow label="Employee ID" value={employee.employeeId} icon={Shield} />
                <InfoRow label="Employment Type" value={employee.employmentType.replace('_', ' ')} icon={Briefcase} />
                <InfoRow label="Department" value={employee.departmentName} icon={Building2} />
                <InfoRow label="Designation" value={employee.designationName} icon={Briefcase} />
                <InfoRow label="Reporting Manager" value={employee.managerName} icon={User} />
                <InfoRow label="Location" value={employee.locationName} icon={MapPin} />
                <InfoRow label="Join Date" value={formatDate(employee.joinDate)} icon={Calendar} />
                <InfoRow label="Status" value={employee.status.replace('_', ' ')} icon={Clock} />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Documents */}
        <TabsContent value="documents">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-sm">Documents</CardTitle>
                <Button size="sm" variant="outline">
                  <FileText className="h-4 w-4" />
                  Upload Document
                </Button>
              </div>
            </CardHeader>
            <CardContent className="pt-0">
              <div className="flex flex-col items-center justify-center py-10 text-center">
                <FileText className="h-10 w-10 text-slate-300 mb-3" />
                <p className="text-sm font-medium text-slate-600">No documents uploaded yet</p>
                <p className="text-xs text-slate-400 mt-1">Upload offer letters, contracts, ID proofs etc.</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Payroll */}
        <TabsContent value="payroll">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Card>
              <CardHeader><CardTitle className="text-sm">Bank Details</CardTitle></CardHeader>
              <CardContent className="pt-0">
                <InfoRow label="Bank Name" value={employee.bankName} icon={CreditCard} />
                <InfoRow label="Account Number" value={employee.bankAccountNumber} />
                <InfoRow label="IFSC Code" value={employee.ifscCode} />
              </CardContent>
            </Card>
            <Card>
              <CardHeader><CardTitle className="text-sm">Recent Payslips</CardTitle></CardHeader>
              <CardContent className="pt-0">
                {['March 2025', 'February 2025', 'January 2025'].map(month => (
                  <div key={month} className="flex items-center justify-between py-3 border-b border-slate-50 last:border-0">
                    <div>
                      <p className="text-sm font-medium text-slate-800">{month}</p>
                      <p className="text-xs text-slate-400">Paid on 28th</p>
                    </div>
                    <Button variant="outline" size="sm">Download</Button>
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Leave History */}
        <TabsContent value="leave">
          <Card>
            <CardHeader><CardTitle className="text-sm">Leave History</CardTitle></CardHeader>
            <CardContent className="pt-0">
              <div className="text-center py-8 text-slate-400">
                <Calendar className="h-8 w-8 mx-auto mb-2 opacity-50" />
                <p className="text-sm">No leave records found</p>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
