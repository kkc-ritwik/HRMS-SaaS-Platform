import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Bell, BellOff } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { notificationService, type Notification } from '@/services/notificationService'
import { formatDate } from '@/lib/utils'

export function NotificationsPage() {
  const qc = useQueryClient()
  const { data, isLoading } = useQuery({ queryKey: ['notifications'], queryFn: () => notificationService.listInbox() })
  const items: Notification[] = (data as { content?: Notification[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  const markAll = useMutation({
    mutationFn: notificationService.markAllRead,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  })
  const mark = useMutation({
    mutationFn: (id: string) => notificationService.markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  })

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <PageHeader
        title="Notifications"
        description="Your inbox of alerts and announcements"
        action={<Button variant="outline" size="sm" onClick={() => markAll.mutate()}>Mark all read</Button>}
      />
      {isLoading ? (
        <Skeleton className="h-64" />
      ) : items.length === 0 ? (
        <Card><CardContent className="p-8 text-center text-slate-500"><BellOff className="mx-auto h-8 w-8 mb-2" /> All caught up</CardContent></Card>
      ) : (
        <div className="space-y-2">
          {items.map(n => (
            <Card key={n.id} className={!n.read ? 'border-l-4 border-l-violet-500' : ''}>
              <CardContent className="p-3 flex items-start gap-3">
                <Bell className={`h-4 w-4 mt-1 ${!n.read ? 'text-violet-600' : 'text-slate-400'}`} />
                <div className="flex-1">
                  <p className="text-sm font-medium">{n.title}</p>
                  <p className="text-xs text-slate-600 mt-0.5">{n.body}</p>
                  <p className="text-xs text-slate-400 mt-1">{formatDate(n.createdAt, 'PPp')}</p>
                </div>
                {!n.read && <Button size="sm" variant="ghost" onClick={() => mark.mutate(n.id)}>Mark read</Button>}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
