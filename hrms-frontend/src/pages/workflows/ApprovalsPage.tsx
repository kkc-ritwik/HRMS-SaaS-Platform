/**
 * Unified approvals inbox — aggregates every pending approval routed to the
 * current user across leave, expense, travel, timesheet, asset, separation,
 * workflow, and generic-workflow modules via /api/v1/me/approvals, with a
 * fallback to the workflow service's own approval list.
 */
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { CheckSquare, Check, X, Filter, ExternalLink } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { PageHeader } from '@/components/ui/page-header'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Avatar } from '@/components/ui/avatar'
import { workflowService } from '@/services/workflowService'
import { Catalog } from '@/services/catalog'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const obj = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!obj) return []
  if (Array.isArray(obj.content)) return obj.content
  if (Array.isArray(obj.data)) return obj.data as T[]
  return []
}

/** Map an approval's entity type → the detail route to open it. */
function routeFor(a: AnyObj): string | null {
  const type = String(a.entityType || a.type || '').toUpperCase()
  const eid = String(a.entityId || a.referenceId || '')
  if (!eid) return null
  if (type.includes('LEAVE')) return '/leave-approvals'
  if (type.includes('EXPENSE')) return '/expenses'
  if (type.includes('TRAVEL')) return '/travel'
  if (type.includes('TIMESHEET')) return '/timesheet'
  if (type.includes('ASSET')) return `/assets/${eid}`
  if (type.includes('SEPARATION') || type.includes('OFFBOARD')) return `/separations/${eid}`
  if (type.includes('JOB') || type.includes('REQUISITION')) return `/jobs/${eid}`
  if (type.includes('CANDIDATE') || type.includes('OFFER')) return `/offers`
  return null
}

export function ApprovalsPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [filter, setFilter] = useState<string>('all')

  // Unified inbox; fall back to workflow approvals if /me/approvals isn't populated.
  const meQ = useQuery({ queryKey: ['me-approvals'], queryFn: () => Catalog.me.approvals() })
  const wfQ = useQuery({ queryKey: ['wf-approvals'], queryFn: workflowService.approvalsForMe })

  const items = useMemo(() => {
    const merged = [...rows<AnyObj>(meQ.data), ...rows<AnyObj>(wfQ.data)]
    // de-dupe by id
    const seen = new Set<string>()
    return merged.filter(a => {
      const k = String(a.id ?? `${a.entityType}-${a.entityId}`)
      if (seen.has(k)) return false
      seen.add(k)
      return true
    })
  }, [meQ.data, wfQ.data])

  const categories = useMemo(() => {
    const set = new Set<string>()
    items.forEach(a => set.add(String(a.entityType || a.type || 'OTHER')))
    return ['all', ...Array.from(set)]
  }, [items])

  const filtered = filter === 'all' ? items : items.filter(a => String(a.entityType || a.type || 'OTHER') === filter)

  const approve = useMutation({
    mutationFn: (id: string) => workflowService.approve(id),
    onSuccess: () => { toast.success('Approved'); qc.invalidateQueries({ queryKey: ['me-approvals'] }); qc.invalidateQueries({ queryKey: ['wf-approvals'] }) },
  })
  const reject = useMutation({
    mutationFn: (id: string) => workflowService.reject(id, 'Rejected'),
    onSuccess: () => { toast.success('Rejected'); qc.invalidateQueries({ queryKey: ['me-approvals'] }); qc.invalidateQueries({ queryKey: ['wf-approvals'] }) },
  })

  const loading = meQ.isLoading && wfQ.isLoading

  return (
    <div className="space-y-5 animate-fade-in">
      <PageHeader title="My Approvals" description="Every request awaiting your action, across all modules" breadcrumbs={[{ label: 'Workflow' }, { label: 'Approvals' }]} />

      {/* Category filter chips */}
      <div className="flex items-center gap-2 flex-wrap">
        <Filter className="h-4 w-4 text-slate-400" />
        {categories.map(cat => (
          <button key={cat} onClick={() => setFilter(cat)}
            className={`text-xs px-3 py-1.5 rounded-full border transition-colors ${filter === cat ? 'bg-brand-600 text-white border-brand-600' : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'}`}>
            {cat === 'all' ? 'All' : cat.replace(/_/g, ' ')}
            {cat !== 'all' && <span className="ml-1.5 opacity-70">{items.filter(a => String(a.entityType || a.type || 'OTHER') === cat).length}</span>}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="space-y-3">{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-20 rounded-xl" />)}</div>
      ) : filtered.length === 0 ? (
        <Card><CardContent className="py-10">
          <EmptyState icon={<CheckSquare className="h-10 w-10" />} title="Inbox zero" description="No approvals waiting for you" />
        </CardContent></Card>
      ) : (
        <div className="space-y-2">
          {filtered.map((a: AnyObj) => {
            const route = routeFor(a)
            return (
              <Card key={String(a.id ?? `${a.entityType}-${a.entityId}`)}>
                <CardContent className="p-4 flex items-center gap-4">
                  <Avatar name={String(a.initiatorName || a.requestedByName || 'User')} size="sm" />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <Badge variant="secondary">{String(a.entityType || a.type || 'REQUEST').replace(/_/g, ' ')}</Badge>
                      <p className="text-sm font-medium truncate">{String(a.title || a.description || a.summary || 'Approval request')}</p>
                    </div>
                    <p className="text-xs text-slate-500 mt-0.5">
                      Requested by {String(a.initiatorName || a.requestedByName || '—')}
                      {a.createdAt || a.requestedAt ? ` · ${formatDate(String(a.createdAt || a.requestedAt))}` : ''}
                      {a.state || a.status ? ` · ${String(a.state || a.status)}` : ''}
                    </p>
                  </div>
                  <div className="flex items-center gap-1.5">
                    {route && <Button size="sm" variant="ghost" onClick={() => navigate(route)}><ExternalLink className="h-3.5 w-3.5" /></Button>}
                    <Button size="sm" onClick={() => approve.mutate(String(a.id))} disabled={approve.isPending}><Check className="h-3.5 w-3.5 mr-1" /> Approve</Button>
                    <Button size="sm" variant="outline" onClick={() => reject.mutate(String(a.id))} disabled={reject.isPending}><X className="h-3.5 w-3.5 mr-1" /> Reject</Button>
                  </div>
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}
    </div>
  )
}
