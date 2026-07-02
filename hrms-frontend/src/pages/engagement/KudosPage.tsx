import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Gift, Plus } from 'lucide-react'
import { Avatar } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FormDialog } from '@/components/ui/form-dialog'
import { engagementService, type Kudos } from '@/services/engagementService'
import { useAuthStore } from '@/store/authStore'
import { formatDate } from '@/lib/utils'

export function KudosPage() {
  const qc = useQueryClient()
  const [giving, setGiving] = useState(false)
  const [tab, setTab] = useState<'feed' | 'received'>('feed')
  const user = useAuthStore(s => s.user)
  const employeeId = user?.employeeId || user?.id || ''

  const feed = useQuery({ queryKey: ['kudos', 'feed'], queryFn: engagementService.myKudosFeed, enabled: tab === 'feed' })
  const received = useQuery({ queryKey: ['kudos', 'received', employeeId], queryFn: () => engagementService.kudosReceived(employeeId), enabled: tab === 'received' && !!employeeId })
  const isLoading = tab === 'feed' ? feed.isLoading : received.isLoading
  const data = tab === 'feed' ? feed.data : received.data

  const give = useMutation({
    mutationFn: (v: Record<string, unknown>) => engagementService.giveKudos(v as { recipientId: string; message: string; value?: string; isPublic?: boolean }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['kudos'] }); setGiving(false) },
  })

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <PageHeader title="Kudos" description="Public peer recognition"
        action={<Button onClick={() => setGiving(true)}><Plus className="h-4 w-4 mr-1" /> Give Kudos</Button>} />
      <Tabs value={tab} onValueChange={v => setTab(v as 'feed' | 'received')}>
        <TabsList>
          <TabsTrigger value="feed">Public feed</TabsTrigger>
          <TabsTrigger value="received">Received by me</TabsTrigger>
        </TabsList>
      </Tabs>
      {isLoading ? (
        <Skeleton className="h-48" />
      ) : (data as Kudos[] || []).length === 0 ? (
        <EmptyState icon={<Gift className="h-10 w-10" />} title="No kudos yet" description="Be the first to recognise a teammate"
          action={{ label: 'Give Kudos', onClick: () => setGiving(true) }} />
      ) : (
        <div className="space-y-3">
          {(data as Kudos[] || []).map(k => (
            <Card key={k.id}>
              <CardContent className="p-4 flex items-start gap-3">
                <Avatar name={k.recipientName} />
                <div className="flex-1">
                  <p className="text-sm"><strong>{k.recipientName || 'Someone'}</strong> received kudos</p>
                  {k.value && <p className="text-xs text-violet-600 font-medium mt-1">{k.value}</p>}
                  <p className="text-sm text-slate-700 mt-2">{k.message}</p>
                  <p className="text-xs text-slate-400 mt-2">{formatDate(k.createdAt, 'PPp')}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <FormDialog
        open={giving} onOpenChange={setGiving} title="Give Kudos"
        submitLabel="Send"
        fields={[
          { name: 'recipientId', label: 'Recipient employee ID', type: 'text', required: true, span: 2 },
          { name: 'value', label: 'Company value', type: 'select', options: [
            { value: 'TEAMWORK', label: 'Teamwork' }, { value: 'OWNERSHIP', label: 'Ownership' },
            { value: 'INNOVATION', label: 'Innovation' }, { value: 'CUSTOMER_FIRST', label: 'Customer first' }, { value: 'INTEGRITY', label: 'Integrity' },
          ] },
          { name: 'isPublic', label: 'Public', type: 'switch', defaultValue: true },
          { name: 'message', label: 'Message', type: 'textarea', required: true, span: 2 },
        ]}
        onSubmit={v => give.mutateAsync(v)}
      />
    </div>
  )
}
