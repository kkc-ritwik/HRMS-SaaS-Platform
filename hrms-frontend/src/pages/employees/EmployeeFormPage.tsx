import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ArrowLeft, ArrowRight, CheckCircle2, User, Briefcase, CreditCard } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { FormField } from '@/components/ui/form-field'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { employeeService } from '@/services/employeeService'
import { getErrorMessage } from '@/lib/api'
import { cn } from '@/lib/utils'

const personalSchema = z.object({
  firstName: z.string().min(1, 'First name is required'),
  lastName: z.string().min(1, 'Last name is required'),
  email: z.string().email('Valid email required'),
  phone: z.string().optional(),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']),
  dateOfBirth: z.string().optional(),
  address: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  country: z.string().optional(),
  pincode: z.string().optional(),
})

const employmentSchema = z.object({
  employeeId: z.string().min(1, 'Employee ID is required'),
  departmentId: z.string().min(1, 'Department is required'),
  designationId: z.string().min(1, 'Designation is required'),
  employmentType: z.enum(['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN']),
  joinDate: z.string().min(1, 'Join date is required'),
  managerId: z.string().optional(),
  locationId: z.string().optional(),
})

const bankSchema = z.object({
  bankName: z.string().optional(),
  bankAccountNumber: z.string().optional(),
  ifscCode: z.string().optional(),
  panNumber: z.string().optional(),
})

type PersonalData = z.infer<typeof personalSchema>
type EmploymentData = z.infer<typeof employmentSchema>
type BankData = z.infer<typeof bankSchema>

const steps = [
  { id: 1, title: 'Personal Info', icon: User },
  { id: 2, title: 'Employment', icon: Briefcase },
  { id: 3, title: 'Bank & Docs', icon: CreditCard },
]

export function EmployeeFormPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const isEditing = !!id
  const [currentStep, setCurrentStep] = useState(1)
  const [personalData, setPersonalData] = useState<PersonalData | null>(null)
  const [employmentData, setEmploymentData] = useState<EmploymentData | null>(null)

  const personalForm = useForm<PersonalData>({
    resolver: zodResolver(personalSchema) as any,
    defaultValues: { gender: 'MALE', country: 'India' },
  })

  const employmentForm = useForm<EmploymentData>({
    resolver: zodResolver(employmentSchema),
    defaultValues: { employmentType: 'FULL_TIME' },
  })

  const bankForm = useForm<BankData>({
    resolver: zodResolver(bankSchema),
  })

  const createMutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => employeeService.create(data),
    onSuccess: () => {
      toast.success('Employee created successfully!')
      navigate('/employees')
    },
    onError: (error: unknown) => {
      toast.error(getErrorMessage(error))
    },
  })

  const handlePersonalSubmit = (data: PersonalData) => {
    setPersonalData(data)
    setCurrentStep(2)
  }

  const handleEmploymentSubmit = (data: EmploymentData) => {
    setEmploymentData(data)
    setCurrentStep(3)
  }

  const handleFinalSubmit = (bankData: BankData) => {
    if (!personalData || !employmentData) return
    const payload = {
      ...personalData,
      ...employmentData,
      ...bankData,
      fullName: `${personalData.firstName} ${personalData.lastName}`,
    }
    createMutation.mutate(payload)
  }

  return (
    <div className="space-y-5 max-w-3xl animate-fade-in">
      <PageHeader
        title={isEditing ? 'Edit Employee' : 'Add New Employee'}
        breadcrumbs={[
          { label: 'People' },
          { label: 'Employees', href: '/employees' },
          { label: isEditing ? 'Edit' : 'Add New' },
        ]}
      >
        <Button variant="outline" size="sm" onClick={() => navigate('/employees')}>
          <ArrowLeft className="h-4 w-4" />
          Cancel
        </Button>
      </PageHeader>

      {/* Step indicator */}
      <div className="flex items-center gap-0">
        {steps.map((step, idx) => {
          const Icon = step.icon
          const isActive = currentStep === step.id
          const isDone = currentStep > step.id
          return (
            <React.Fragment key={step.id}>
              <div className="flex items-center gap-2">
                <div className={cn(
                  'flex h-8 w-8 items-center justify-center rounded-full text-sm font-semibold transition-all',
                  isActive && 'bg-brand-600 text-white',
                  isDone && 'bg-green-500 text-white',
                  !isActive && !isDone && 'bg-slate-100 text-slate-400',
                )}>
                  {isDone ? <CheckCircle2 className="h-4 w-4" /> : <Icon className="h-4 w-4" />}
                </div>
                <span className={cn(
                  'text-sm font-medium transition-colors',
                  isActive ? 'text-slate-900' : isDone ? 'text-green-600' : 'text-slate-400'
                )}>
                  {step.title}
                </span>
              </div>
              {idx < steps.length - 1 && (
                <div className={cn(
                  'h-0.5 flex-1 mx-3 rounded transition-all',
                  currentStep > step.id ? 'bg-green-400' : 'bg-slate-200'
                )} />
              )}
            </React.Fragment>
          )
        })}
      </div>

      {/* Step 1: Personal Info */}
      {currentStep === 1 && (
        <Card>
          <CardHeader>
            <CardTitle>Personal Information</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={personalForm.handleSubmit(handlePersonalSubmit as any)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField label="First Name" error={personalForm.formState.errors.firstName?.message} required>
                  <Input {...personalForm.register('firstName')} placeholder="Priya" />
                </FormField>
                <FormField label="Last Name" error={personalForm.formState.errors.lastName?.message} required>
                  <Input {...personalForm.register('lastName')} placeholder="Sharma" />
                </FormField>
              </div>
              <FormField label="Email Address" error={personalForm.formState.errors.email?.message} required>
                <Input {...personalForm.register('email')} type="email" placeholder="priya.sharma@company.com" />
              </FormField>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Phone Number">
                  <Input {...personalForm.register('phone')} placeholder="+91 98765 43210" />
                </FormField>
                <FormField label="Gender" error={personalForm.formState.errors.gender?.message} required>
                  <Select
                    value={personalForm.watch('gender')}
                    onValueChange={v => personalForm.setValue('gender', v as 'MALE' | 'FEMALE' | 'OTHER')}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="MALE">Male</SelectItem>
                      <SelectItem value="FEMALE">Female</SelectItem>
                      <SelectItem value="OTHER">Other</SelectItem>
                    </SelectContent>
                  </Select>
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Date of Birth">
                  <Input {...personalForm.register('dateOfBirth')} type="date" />
                </FormField>
                <FormField label="City">
                  <Input {...personalForm.register('city')} placeholder="Bangalore" />
                </FormField>
              </div>
              <FormField label="Address">
                <Input {...personalForm.register('address')} placeholder="123 MG Road" />
              </FormField>
              <div className="grid grid-cols-3 gap-4">
                <FormField label="State">
                  <Input {...personalForm.register('state')} placeholder="Karnataka" />
                </FormField>
                <FormField label="Country">
                  <Input {...personalForm.register('country')} placeholder="India" />
                </FormField>
                <FormField label="Pincode">
                  <Input {...personalForm.register('pincode')} placeholder="560034" />
                </FormField>
              </div>
              <div className="flex justify-end pt-2">
                <Button type="submit">
                  Next: Employment Details
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Step 2: Employment */}
      {currentStep === 2 && (
        <Card>
          <CardHeader>
            <CardTitle>Employment Details</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={employmentForm.handleSubmit(handleEmploymentSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Employee ID" error={employmentForm.formState.errors.employeeId?.message} required>
                  <Input {...employmentForm.register('employeeId')} placeholder="EMP001" />
                </FormField>
                <FormField label="Join Date" error={employmentForm.formState.errors.joinDate?.message} required>
                  <Input {...employmentForm.register('joinDate')} type="date" />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Department" error={employmentForm.formState.errors.departmentId?.message} required>
                  <Select
                    value={employmentForm.watch('departmentId')}
                    onValueChange={v => employmentForm.setValue('departmentId', v)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select department" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="eng">Engineering</SelectItem>
                      <SelectItem value="prod">Product</SelectItem>
                      <SelectItem value="mkt">Marketing</SelectItem>
                      <SelectItem value="sales">Sales</SelectItem>
                      <SelectItem value="hr">HR</SelectItem>
                      <SelectItem value="fin">Finance</SelectItem>
                      <SelectItem value="ops">Operations</SelectItem>
                    </SelectContent>
                  </Select>
                </FormField>
                <FormField label="Designation" error={employmentForm.formState.errors.designationId?.message} required>
                  <Select
                    value={employmentForm.watch('designationId')}
                    onValueChange={v => employmentForm.setValue('designationId', v)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select designation" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="se">Software Engineer</SelectItem>
                      <SelectItem value="sse">Senior Engineer</SelectItem>
                      <SelectItem value="tl">Tech Lead</SelectItem>
                      <SelectItem value="pm">Product Manager</SelectItem>
                      <SelectItem value="hr">HR Manager</SelectItem>
                    </SelectContent>
                  </Select>
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Employment Type" error={employmentForm.formState.errors.employmentType?.message} required>
                  <Select
                    value={employmentForm.watch('employmentType')}
                    onValueChange={v => employmentForm.setValue('employmentType', v as 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERN')}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="FULL_TIME">Full Time</SelectItem>
                      <SelectItem value="PART_TIME">Part Time</SelectItem>
                      <SelectItem value="CONTRACT">Contract</SelectItem>
                      <SelectItem value="INTERN">Intern</SelectItem>
                    </SelectContent>
                  </Select>
                </FormField>
                <FormField label="Location">
                  <Select
                    value={employmentForm.watch('locationId') || ''}
                    onValueChange={v => employmentForm.setValue('locationId', v)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select location" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="blr">Bangalore</SelectItem>
                      <SelectItem value="mum">Mumbai</SelectItem>
                      <SelectItem value="del">Delhi</SelectItem>
                      <SelectItem value="hyd">Hyderabad</SelectItem>
                      <SelectItem value="rem">Remote</SelectItem>
                    </SelectContent>
                  </Select>
                </FormField>
              </div>
              <div className="flex justify-between pt-2">
                <Button type="button" variant="outline" onClick={() => setCurrentStep(1)}>
                  <ArrowLeft className="h-4 w-4" />
                  Back
                </Button>
                <Button type="submit">
                  Next: Bank Details
                  <ArrowRight className="h-4 w-4" />
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      {/* Step 3: Bank & Documents */}
      {currentStep === 3 && (
        <Card>
          <CardHeader>
            <CardTitle>Bank & Document Details</CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={bankForm.handleSubmit(handleFinalSubmit)} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <FormField label="Bank Name">
                  <Input {...bankForm.register('bankName')} placeholder="HDFC Bank" />
                </FormField>
                <FormField label="Account Number">
                  <Input {...bankForm.register('bankAccountNumber')} placeholder="1234567890" />
                </FormField>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField label="IFSC Code">
                  <Input {...bankForm.register('ifscCode')} placeholder="HDFC0001234" />
                </FormField>
                <FormField label="PAN Number">
                  <Input {...bankForm.register('panNumber')} placeholder="ABCDE1234F" />
                </FormField>
              </div>
              <div className="flex justify-between pt-2">
                <Button type="button" variant="outline" onClick={() => setCurrentStep(2)}>
                  <ArrowLeft className="h-4 w-4" />
                  Back
                </Button>
                <Button type="submit" loading={createMutation.isPending}>
                  {createMutation.isPending ? 'Creating...' : 'Create Employee'}
                  <CheckCircle2 className="h-4 w-4" />
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
