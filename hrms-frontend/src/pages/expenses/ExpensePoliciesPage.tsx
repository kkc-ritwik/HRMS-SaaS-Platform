import { ShieldCheck } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { expenseService } from '@/services/expenseService'

interface Policy extends Record<string, unknown> { id: string; name: string; category?: string; maxAmount?: number; requiresReceipt?: boolean; active?: boolean }

export function ExpensePoliciesPage() {
  return (
    <ResourcePage<Policy>
      title="Expense Policies"
      description="Spend limits, receipt rules and approval thresholds"
      icon={<ShieldCheck className="h-10 w-10" />}
      queryKey={['expense-policies']}
      fetcher={() => expenseService.policiesAll()}
      columns={[
        { key: 'name', label: 'Policy' },
        { key: 'category', label: 'Category' },
        { key: 'maxAmount', label: 'Max amount', align: 'right', render: p => p.maxAmount ? `₹${Number(p.maxAmount).toLocaleString()}` : '—' },
        { key: 'requiresReceipt', label: 'Receipt', render: p => p.requiresReceipt ? <Badge>Required</Badge> : '—' },
        { key: 'active', label: 'Active', render: p => <Badge variant={p.active === false ? 'secondary' : 'success'}>{p.active === false ? 'Inactive' : 'Active'}</Badge> },
      ]}
      formFields={[
        { name: 'name', label: 'Policy name', type: 'text', required: true, span: 2 },
        { name: 'category', label: 'Category', type: 'text' },
        { name: 'maxAmount', label: 'Max amount', type: 'currency' },
        { name: 'receiptThreshold', label: 'Receipt required above', type: 'currency' },
        { name: 'requiresReceipt', label: 'Always require receipt', type: 'switch' },
        { name: 'approverLevels', label: 'Approver levels', type: 'number' },
        { name: 'description', label: 'Description', type: 'textarea', span: 2 },
      ]}
      onCreate={v => expenseService.createPolicy(v)}
      onUpdate={(id, v) => expenseService.updatePolicy(id, v)}
      onDelete={id => expenseService.deletePolicy(id)}
    />
  )
}
