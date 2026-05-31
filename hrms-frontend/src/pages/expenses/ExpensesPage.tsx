import { useQuery } from '@tanstack/react-query'
import { Plus, Receipt } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { DataList } from '@/components/ui/data-list'
import { expenseService, type ExpenseClaim } from '@/services/expenseService'

export function ExpensesPage() {
  const navigate = useNavigate()
  const { data, isLoading } = useQuery({ queryKey: ['expenses'], queryFn: () => expenseService.listClaims() })
  const items: ExpenseClaim[] = (data as { content?: ExpenseClaim[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  return (
    <DataList<ExpenseClaim>
      title="Expense Claims"
      description="Reimbursements you can track and approve"
      action={<Button onClick={() => navigate('/expenses/new')}><Plus className="h-4 w-4 mr-1" /> New Claim</Button>}
      data={items}
      isLoading={isLoading}
      emptyIcon={<Receipt className="h-10 w-10" />}
      emptyTitle="No claims yet"
      columns={[
        { key: 'claimNumber', label: 'Claim' },
        { key: 'title', label: 'Title' },
        { key: 'totalAmount', label: 'Amount', align: 'right', render: c => `${c.currency} ${c.totalAmount?.toLocaleString()}` },
        { key: 'status', label: 'Status', render: c => <Badge>{c.status}</Badge> },
        { key: 'submittedAt', label: 'Submitted' },
      ]}
    />
  )
}
