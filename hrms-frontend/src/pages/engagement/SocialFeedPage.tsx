import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Heart, MessageSquare, Share2 } from 'lucide-react'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Avatar } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { socialService, type Post } from '@/services/socialService'
import { formatDate } from '@/lib/utils'

export function SocialFeedPage() {
  const qc = useQueryClient()
  const [body, setBody] = useState('')
  const { data, isLoading } = useQuery({ queryKey: ['social', 'feed'], queryFn: () => socialService.feed() })
  const posts: Post[] = (data as { content?: Post[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  const create = useMutation({
    mutationFn: () => socialService.createPost({ body, visibility: 'PUBLIC' }),
    onSuccess: () => { setBody(''); qc.invalidateQueries({ queryKey: ['social', 'feed'] }) },
  })
  const like = useMutation({
    mutationFn: (id: string) => socialService.likePost(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['social', 'feed'] }),
  })

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <PageHeader title="Social Feed" description="What's happening across the team" />

      <Card>
        <CardContent className="p-4 space-y-2">
          <Textarea
            placeholder="Share something with your team..."
            value={body}
            onChange={e => setBody(e.target.value)}
            rows={3}
          />
          <div className="flex justify-end">
            <Button size="sm" onClick={() => create.mutate()} disabled={!body.trim()}>Post</Button>
          </div>
        </CardContent>
      </Card>

      {isLoading ? (
        <Skeleton className="h-48" />
      ) : (
        posts.map(p => (
          <Card key={p.id}>
            <CardHeader className="flex flex-row items-center gap-3 pb-2">
              <Avatar name={p.authorName} size="sm" />
              <div className="flex-1">
                <p className="text-sm font-medium">{p.authorName}</p>
                <p className="text-xs text-slate-400">{formatDate(p.createdAt, 'PPp')}</p>
              </div>
            </CardHeader>
            <CardContent className="space-y-3">
              <p className="text-sm text-slate-700 whitespace-pre-wrap">{p.body}</p>
              {p.imageUri && <img src={p.imageUri} alt="" className="rounded-lg max-h-96 object-cover" />}
              <div className="flex items-center gap-4 pt-2 border-t text-sm text-slate-600">
                <button onClick={() => like.mutate(p.id)} className="flex items-center gap-1 hover:text-pink-600">
                  <Heart className={`h-4 w-4 ${p.isLikedByMe ? 'fill-pink-500 text-pink-500' : ''}`} /> {p.likesCount}
                </button>
                <span className="flex items-center gap-1"><MessageSquare className="h-4 w-4" /> {p.commentsCount}</span>
                <span className="flex items-center gap-1"><Share2 className="h-4 w-4" /> {p.sharesCount}</span>
              </div>
            </CardContent>
          </Card>
        ))
      )}
    </div>
  )
}
