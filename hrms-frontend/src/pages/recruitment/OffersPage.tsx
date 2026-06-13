import { FileSignature, Send, XCircle } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { ResourcePage } from '@/components/ui/resource-page'
import { offerService } from '@/services/extendedServices'
import { formatDate } from '@/lib/utils'

interface Offer extends Record<string, unknown> {
  id: string; candidateName?: string; designation?: string; ctc?: number; status: string; joiningDate?: string; issuedAt?: string
}

export function OffersPage() {
  return (
    <ResourcePage<Offer>
      title="Offer Letters"
      description="Draft, issue, accept, withdraw offers"
      icon={<FileSignature className="h-10 w-10" />}
      queryKey={['offers']}
      fetcher={() => offerService.list()}
      filters={{ status: ['DRAFT', 'ISSUED', 'ACCEPTED', 'DECLINED', 'WITHDRAWN', 'EXPIRED'] }}
      columns={[
        { key: 'candidateName', label: 'Candidate' },
        { key: 'designation', label: 'Role' },
        { key: 'ctc', label: 'CTC', align: 'right', render: o => o.ctc ? `₹${Number(o.ctc).toLocaleString()}` : '—' },
        { key: 'joiningDate', label: 'Joining', render: o => o.joiningDate ? formatDate(String(o.joiningDate)) : '—' },
        { key: 'status', label: 'Status', render: o => <Badge variant={o.status === 'DECLINED' || o.status === 'WITHDRAWN' ? 'destructive' : o.status === 'ACCEPTED' ? 'success' : 'warning'}>{String(o.status)}</Badge> },
      ]}
      rowActions={o => [
        { label: 'Send to candidate', icon: <Send className="h-3.5 w-3.5" />, show: o.status === 'DRAFT', confirm: 'Send this offer to the candidate?', run: () => offerService.send(o.id) },
        { label: 'Revoke', icon: <XCircle className="h-3.5 w-3.5" />, show: o.status === 'ISSUED' || o.status === 'SENT', destructive: true, confirm: 'Revoke this offer?', run: () => offerService.revoke(o.id, 'Revoked by employer') },
      ]}
    />
  )
}
