import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { MessageSquarePlus, Plus } from 'lucide-react'
import { Avatar } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { FormDialog } from '@/components/ui/form-dialog'
import { performanceService } from '@/services/performanceService'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function ContinuousFeedbackPage() {
  const qc = useQueryClient()
  const [tab, setTab] = useState<'wall' | 'received' | 'given'>('wall')
  const [giving, setGiving] = useState(false)

  const wallQ = useQuery({ queryKey: ['feedback', 'wall'], queryFn: () => performanceService.feedbackWall(), enabled: tab === 'wall' })
  const recvQ = useQuery({ queryKey: ['feedback', 'received'], queryFn: () => performanceService.myFeedbackReceived(), enabled: tab === 'received' })
  const givenQ = useQuery({ queryKey: ['feedback', 'given'], queryFn: () => performanceService.myFeedbackGiven(), enabled: tab === 'given' })

  const give = useMutation({
    mutationFn: (v: Record<string, unknown>) => performanceService.giveFeedback(v),
    onSuccess: () => { toast.success('Feedback shared'); qc.invalidateQueries({ queryKey: ['feedback'] }); setGiving(false) },
  })

  const data = tab === 'wall' ? wallQ.data : tab === 'received' ? recvQ.data : givenQ.data
  const loading = tab === 'wall' ? wallQ.isLoading : tab === 'received' ? recvQ.isLoading : givenQ.isLoading
  const items = rows<AnyObj>(data)

  return (
    <div className="space-y-5 max-w-3xl mx-auto">
      <PageHeader title="Continuous Feedback" description="360° peer and manager feedback"
        action={<Button onClick={() => setGiving(true)}><Plus className="h-4 w-4 mr-1" /> Give Feedback</Button>} />

      <Tabs value={tab} onValueChange={v => setTab(v as 'wall' | 'received' | 'given')}>
        <TabsList>
          <TabsTrigger value="wall">Feedback Wall</TabsTrigger>
          <TabsTrigger value="received">Received</TabsTrigger>
          <TabsTrigger value="given">Given</TabsTrigger>
        </TabsList>
      </Tabs>

      {loading ? <Skeleton className="h-48" /> : items.length === 0 ? (
        <Card><CardContent className="py-10"><EmptyState icon={<MessageSquarePlus className="h-8 w-8" />} title="No feedback yet" /></CardContent></Card>
      ) : (
        <div className="space-y-3">
          {items.map(f => (
            <Card key={String(f.id)}>
              <CardContent className="p-4 flex items-start gap-3">
                <Avatar name={String(f.fromName ?? f.giverName ?? 'Anonymous')} size="sm" />
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <p className="text-sm font-medium">{String(f.fromName ?? f.giverName ?? 'Anonymous')} → {String(f.toName ?? f.recipientName ?? '')}</p>
                    {f.type ? <Badge variant="secondary">{String(f.type)}</Badge> : null}
                  </div>
                  <p className="text-sm text-slate-700 mt-1">{String(f.message ?? f.body ?? '')}</p>
                  <p className="text-xs text-slate-400 mt-1">{f.createdAt ? formatDate(String(f.createdAt), 'PP') : ''}</p>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <FormDialog
        open={giving} onOpenChange={setGiving} title="Give feedback" submitLabel="Share"
        fields={[
          { name: 'recipientId', label: 'Recipient employee ID', type: 'text', required: true, span: 2 },
          { name: 'type', label: 'Type', type: 'select', options: [
            { value: 'PRAISE', label: 'Praise' }, { value: 'CONSTRUCTIVE', label: 'Constructive' }, { value: 'GENERAL', label: 'General' },
          ] },
          { name: 'visibility', label: 'Visibility', type: 'select', options: [
            { value: 'PRIVATE', label: 'Private' }, { value: 'MANAGER', label: 'Share with manager' }, { value: 'PUBLIC', label: 'Public (wall)' },
          ] },
          { name: 'message', label: 'Feedback', type: 'textarea', required: true, span: 2 },
        ]}
        onSubmit={v => give.mutateAsync(v)}
      />
    </div>
  )
}
