import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Banknote } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { DataList } from '@/components/ui/data-list'
import { FormDialog } from '@/components/ui/form-dialog'
import { loanService } from '@/services/extendedServices'

interface Loan {
  id: string; employeeId: string; loanType: string; principalAmount: number;
  emi: number; outstandingBalance: number; status: string; disbursedOn?: string
}

export function LoansPage() {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const { data, isLoading } = useQuery({ queryKey: ['loans'], queryFn: () => loanService.list() })
  const items: Loan[] = (data as { content?: Loan[] } | undefined)?.content
    || (Array.isArray(data) ? data as Loan[] : [])

  const create = useMutation({
    mutationFn: (v: Record<string, unknown>) => loanService.apply(v),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['loans'] }),
  })

  return (
    <>
      <DataList<Loan>
        title="Loans & Advances" description="Salary advances and employee loans"
        action={<Button onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New Loan</Button>}
        data={items} isLoading={isLoading}
        emptyIcon={<Banknote className="h-10 w-10" />} emptyTitle="No loans on record"
        filters={{ status: ['PENDING', 'APPROVED', 'DISBURSED', 'IN_REPAYMENT', 'CLOSED', 'REJECTED'] }}
        columns={[
          { key: 'employeeId', label: 'Employee' },
          { key: 'loanType', label: 'Type' },
          { key: 'principalAmount', label: 'Principal', align: 'right', render: l => `₹${l.principalAmount?.toLocaleString()}` },
          { key: 'emi', label: 'EMI', align: 'right', render: l => `₹${l.emi?.toLocaleString()}` },
          { key: 'outstandingBalance', label: 'Outstanding', align: 'right', render: l => `₹${l.outstandingBalance?.toLocaleString()}` },
          { key: 'status', label: 'Status', render: l => <Badge>{l.status}</Badge> },
        ]}
      />
      <FormDialog
        open={creating} onOpenChange={setCreating} title="Apply for loan"
        onSubmit={v => create.mutateAsync(v)}
        fields={[
          { name: 'loanType', label: 'Loan type', type: 'select', required: true, options: [
            { value: 'SALARY_ADVANCE', label: 'Salary advance' }, { value: 'PERSONAL', label: 'Personal loan' },
            { value: 'EMERGENCY', label: 'Emergency loan' }, { value: 'EDUCATION', label: 'Education loan' },
          ] },
          { name: 'principalAmount', label: 'Amount', type: 'currency', required: true },
          { name: 'tenureMonths', label: 'Tenure (months)', type: 'number', required: true },
          { name: 'interestRate', label: 'Interest rate (%)', type: 'number' },
          { name: 'reason', label: 'Reason', type: 'textarea', span: 2, required: true },
        ]}
      />
    </>
  )
}
