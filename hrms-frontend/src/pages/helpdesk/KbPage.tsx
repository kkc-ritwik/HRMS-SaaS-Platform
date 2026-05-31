import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { BookOpen, Search } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { kbService } from '@/services/extendedServices'

interface Article { id: string; title: string; category: string; views: number; helpful: number }

export function KbPage() {
  const [q, setQ] = useState('')
  const { data, isLoading } = useQuery({ queryKey: ['kb', q], queryFn: () => kbService.list(undefined, q) })
  const items: Article[] = Array.isArray(data) ? data as Article[] : []
  return (
    <div className="space-y-6">
      <PageHeader title="Knowledge Base" description="Self-service articles + HR FAQs" />
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
        <Input className="pl-10" placeholder="Search articles..." value={q} onChange={e => setQ(e.target.value)} />
      </div>
      {isLoading ? <Skeleton className="h-64" /> : items.length === 0 ? (
        <EmptyState icon={<BookOpen className="h-10 w-10" />} title="No articles" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          {items.map(a => (
            <Card key={a.id} className="cursor-pointer hover:shadow-md transition">
              <CardContent className="p-4">
                <p className="text-xs text-slate-500 uppercase">{a.category}</p>
                <h3 className="font-semibold mt-1">{a.title}</h3>
                <div className="text-xs text-slate-400 mt-2 flex items-center gap-3">
                  <span>{a.views} views</span>
                  <span>👍 {a.helpful}</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
