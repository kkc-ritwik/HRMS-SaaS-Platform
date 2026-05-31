import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, MessageSquare, Send } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
import { Avatar } from '@/components/ui/avatar'
import { helpdeskService, type Ticket, type TicketComment } from '@/services/helpdeskService'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

export function TicketDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [comment, setComment] = useState('')
  const [internal, setInternal] = useState(false)

  const ticket = useQuery({ queryKey: ['ticket', id], queryFn: () => helpdeskService.getTicket(id), enabled: !!id })
  const comments = useQuery({ queryKey: ['ticket-comments', id], queryFn: () => helpdeskService.comments(id), enabled: !!id })

  const post = useMutation({
    mutationFn: () => helpdeskService.addComment(id, comment, internal),
    onSuccess: () => { setComment(''); qc.invalidateQueries({ queryKey: ['ticket-comments', id] }); toast.success('Posted') },
  })
  const resolve = useMutation({
    mutationFn: () => helpdeskService.updateStatus(id, 'RESOLVED', 'Issue resolved'),
    onSuccess: () => { toast.success('Resolved'); qc.invalidateQueries({ queryKey: ['ticket', id] }) },
  })

  if (ticket.isLoading) return <Skeleton className="h-96" />
  const t = ticket.data as Ticket
  if (!t) return <p>Ticket not found</p>

  const list = (comments.data as TicketComment[]) || []

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/helpdesk')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <Card>
        <CardContent className="p-6 flex items-start justify-between">
          <div>
            <p className="text-xs text-slate-500">#{t.ticketNumber}</p>
            <h1 className="text-2xl font-bold mt-1">{t.subject}</h1>
            <div className="flex items-center gap-2 mt-2">
              <Badge>{t.category}</Badge>
              <Badge>{t.priority}</Badge>
              <Badge>{t.status}</Badge>
            </div>
            <p className="text-sm mt-3 whitespace-pre-wrap">{t.description}</p>
            <p className="text-xs text-slate-400 mt-2">Raised by {t.raisedByName} on {formatDate(t.createdAt, 'PPp')}</p>
          </div>
          {t.status !== 'RESOLVED' && t.status !== 'CLOSED' && (
            <Button onClick={() => resolve.mutate()}>Mark resolved</Button>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><MessageSquare className="h-5 w-5" /> Comments</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          {list.map(c => (
            <div key={c.id} className={`flex items-start gap-3 ${c.isInternalNote ? 'bg-amber-50 p-2 rounded' : ''}`}>
              <Avatar name={c.authorName} size="sm" />
              <div className="flex-1">
                <p className="text-sm font-medium">{c.authorName} {c.isInternalNote && <span className="text-xs text-amber-700 ml-2">Internal</span>}</p>
                <p className="text-sm text-slate-700 mt-1 whitespace-pre-wrap">{c.body}</p>
                <p className="text-xs text-slate-400 mt-1">{formatDate(c.createdAt, 'PPp')}</p>
              </div>
            </div>
          ))}
          <div className="pt-3 border-t">
            <Textarea rows={3} value={comment} onChange={e => setComment(e.target.value)} placeholder="Type your reply..." />
            <div className="flex items-center justify-between mt-2">
              <label className="text-sm flex items-center gap-2"><input type="checkbox" checked={internal} onChange={e => setInternal(e.target.checked)} />Internal note</label>
              <Button size="sm" onClick={() => post.mutate()} disabled={!comment.trim()}>
                <Send className="h-3.5 w-3.5 mr-1" /> Post
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
