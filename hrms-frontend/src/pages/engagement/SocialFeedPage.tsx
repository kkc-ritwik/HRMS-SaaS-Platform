import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Heart, MessageSquare, Share2, Trash2, Send } from 'lucide-react'
import { Card, CardContent, CardHeader } from '@/components/ui/card'
import { Avatar } from '@/components/ui/avatar'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { Skeleton } from '@/components/ui/skeleton'
import { PageHeader } from '@/components/ui/page-header'
import { socialService, type Post, type PostComment } from '@/services/socialService'
import { formatDate } from '@/lib/utils'

function PostComments({ postId }: { postId: string }) {
  const qc = useQueryClient()
  const [text, setText] = useState('')
  const { data, isLoading } = useQuery({ queryKey: ['post-comments', postId], queryFn: () => socialService.comments(postId) })
  const list = (Array.isArray(data) ? data : (data as { content?: PostComment[] } | undefined)?.content ?? []) as PostComment[]
  const add = useMutation({ mutationFn: () => socialService.comment(postId, text), onSuccess: () => { setText(''); qc.invalidateQueries({ queryKey: ['post-comments', postId] }) } })
  const del = useMutation({ mutationFn: (id: string) => socialService.deleteComment(id), onSuccess: () => qc.invalidateQueries({ queryKey: ['post-comments', postId] }) })
  return (
    <div className="pt-2 border-t space-y-2">
      {isLoading ? <Skeleton className="h-10" /> : list.map(c => (
        <div key={c.id} className="group flex items-start gap-2">
          <Avatar name={c.authorName} size="xs" />
          <div className="flex-1 bg-slate-50 rounded-lg px-3 py-1.5">
            <p className="text-xs font-medium">{c.authorName}</p>
            <p className="text-sm">{c.body}</p>
          </div>
          <button onClick={() => del.mutate(c.id)} className="opacity-0 group-hover:opacity-100 text-red-400"><Trash2 className="h-3.5 w-3.5" /></button>
        </div>
      ))}
      <div className="flex items-center gap-2">
        <Input value={text} onChange={e => setText(e.target.value)} placeholder="Write a comment…" className="h-8" onKeyDown={e => { if (e.key === 'Enter' && text.trim()) add.mutate() }} />
        <Button size="sm" variant="ghost" disabled={!text.trim()} onClick={() => add.mutate()}><Send className="h-3.5 w-3.5" /></Button>
      </div>
    </div>
  )
}

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
  const del = useMutation({
    mutationFn: (id: string) => socialService.deletePost(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['social', 'feed'] }),
  })
  const [openComments, setOpenComments] = useState<Record<string, boolean>>({})

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
              <button onClick={() => del.mutate(p.id)} className="text-slate-300 hover:text-red-500"><Trash2 className="h-4 w-4" /></button>
            </CardHeader>
            <CardContent className="space-y-3">
              <p className="text-sm text-slate-700 whitespace-pre-wrap">{p.body}</p>
              {p.imageUri && <img src={p.imageUri} alt="" className="rounded-lg max-h-96 object-cover" />}
              <div className="flex items-center gap-4 pt-2 border-t text-sm text-slate-600">
                <button onClick={() => like.mutate(p.id)} className="flex items-center gap-1 hover:text-pink-600">
                  <Heart className={`h-4 w-4 ${p.isLikedByMe ? 'fill-pink-500 text-pink-500' : ''}`} /> {p.likesCount}
                </button>
                <button onClick={() => setOpenComments(s => ({ ...s, [p.id]: !s[p.id] }))} className="flex items-center gap-1 hover:text-brand-600">
                  <MessageSquare className="h-4 w-4" /> {p.commentsCount}
                </button>
                <span className="flex items-center gap-1"><Share2 className="h-4 w-4" /> {p.sharesCount}</span>
              </div>
              {openComments[p.id] && <PostComments postId={p.id} />}
            </CardContent>
          </Card>
        ))
      )}
    </div>
  )
}
