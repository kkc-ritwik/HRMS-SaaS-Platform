import { useState, useMemo, useCallback, type ReactNode } from 'react'
import { Card, CardContent } from './card'
import { Skeleton } from './skeleton'
import { EmptyState } from './empty-state'
import { PageHeader } from './page-header'
import { Input } from './input'
import { Button } from './button'
import { Checkbox } from './checkbox'
import { Search, Download, ChevronLeft, ChevronRight, ArrowUpDown, ArrowUp, ArrowDown, X } from 'lucide-react'
import { cn } from '@/lib/utils'

export interface Column<T> {
  key: string
  label: string
  render?: (row: T) => ReactNode
  align?: 'left' | 'right' | 'center'
  width?: string
  sortable?: boolean
  value?: (row: T) => string | number | null | undefined
}

export interface BulkAction<T> {
  label: string
  icon?: ReactNode
  variant?: 'default' | 'outline' | 'destructive'
  onClick: (selected: T[]) => void
}

interface DataListProps<T> {
  title: string
  description?: string
  action?: ReactNode
  data: T[]
  isLoading?: boolean
  columns: Column<T>[]
  emptyTitle?: string
  emptyDescription?: string
  emptyIcon?: ReactNode
  rowKey?: string
  onRowClick?: (row: T) => void
  searchable?: boolean
  paginated?: boolean
  pageSize?: number
  exportable?: boolean
  bulkActions?: BulkAction<T>[]
  filters?: Record<string, string[]>
}

function defaultValue<T>(row: T, key: string): string | number | null | undefined {
  const v = (row as unknown as Record<string, unknown>)[key]
  if (v == null) return null
  if (typeof v === 'string' || typeof v === 'number') return v
  return String(v)
}

function toCsv<T>(rows: T[], columns: Column<T>[]): string {
  const head = columns.map(c => `"${c.label.replace(/"/g, '""')}"`).join(',')
  const body = rows.map(r =>
    columns.map(c => {
      const raw = c.value ? c.value(r) : defaultValue(r, c.key)
      const s = raw == null ? '' : String(raw)
      return `"${s.replace(/"/g, '""')}"`
    }).join(',')
  ).join('\n')
  return head + '\n' + body
}

export function DataList<T>({
  title, description, action, data, isLoading, columns,
  emptyTitle = 'No data', emptyDescription, emptyIcon,
  rowKey = 'id', onRowClick,
  searchable = true, paginated = true, pageSize = 20, exportable = true,
  bulkActions, filters,
}: DataListProps<T>) {
  const [q, setQ] = useState('')
  const [sortKey, setSortKey] = useState<string | null>(null)
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc')
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<Set<string>>(new Set())
  const [columnFilters, setColumnFilters] = useState<Record<string, string>>({})

  const get = useCallback((row: T, key: string) =>
    (row as unknown as Record<string, unknown>)[key], [])
  const id = useCallback((row: T): string => String(get(row, rowKey) ?? ''), [get, rowKey])

  const view = useMemo(() => {
    let rows = data || []
    if (q.trim()) {
      const lower = q.toLowerCase()
      rows = rows.filter(r => columns.some(c => {
        const v = c.value ? c.value(r) : defaultValue(r, c.key)
        return v != null && String(v).toLowerCase().includes(lower)
      }))
    }
    for (const [k, v] of Object.entries(columnFilters)) {
      if (!v) continue
      rows = rows.filter(r => {
        const col = columns.find(c => c.key === k)
        const raw = col?.value ? col.value(r) : defaultValue(r, k)
        return raw != null && String(raw).toLowerCase().includes(v.toLowerCase())
      })
    }
    if (sortKey) {
      const col = columns.find(c => c.key === sortKey)
      rows = [...rows].sort((a, b) => {
        const av = col?.value ? col.value(a) : defaultValue(a, sortKey)
        const bv = col?.value ? col.value(b) : defaultValue(b, sortKey)
        if (av == null && bv == null) return 0
        if (av == null) return 1
        if (bv == null) return -1
        const cmp = typeof av === 'number' && typeof bv === 'number'
          ? av - bv
          : String(av).localeCompare(String(bv))
        return sortDir === 'asc' ? cmp : -cmp
      })
    }
    return rows
  }, [data, q, sortKey, sortDir, columnFilters, columns])

  const totalPages = Math.max(1, Math.ceil(view.length / pageSize))
  const visible = paginated ? view.slice(page * pageSize, page * pageSize + pageSize) : view

  const toggleSort = (key: string) => {
    if (sortKey === key) setSortDir(d => (d === 'asc' ? 'desc' : 'asc'))
    else { setSortKey(key); setSortDir('asc') }
  }

  const allOnPageSelected = visible.length > 0 && visible.every(r => selected.has(id(r)))
  const someSelected = selected.size > 0

  const toggleAllOnPage = (checked: boolean) => {
    const next = new Set(selected)
    visible.forEach(r => { if (checked) next.add(id(r)); else next.delete(id(r)) })
    setSelected(next)
  }

  const handleExport = () => {
    const csv = toCsv(view, columns)
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${title.toLowerCase().replace(/\s+/g, '-')}-${new Date().toISOString().slice(0, 10)}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  const selectedRows = useMemo(() =>
    (data || []).filter(r => selected.has(id(r))), [data, selected, id])

  return (
    <div className="space-y-4">
      <PageHeader title={title} description={description} action={action} />

      <div className="flex flex-wrap items-center gap-2 justify-between">
        <div className="flex items-center gap-2 flex-wrap">
          {searchable && (
            <div className="relative">
              <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
              <Input
                className="pl-8 h-9 w-64"
                placeholder="Search..."
                value={q}
                onChange={e => { setQ(e.target.value); setPage(0) }}
              />
            </div>
          )}
          {filters && Object.entries(filters).map(([key, options]) => (
            <select
              key={key}
              className="h-9 rounded-md border border-slate-200 bg-white px-2 text-sm"
              value={columnFilters[key] || ''}
              onChange={e => { setColumnFilters(f => ({ ...f, [key]: e.target.value })); setPage(0) }}
            >
              <option value="">All {columns.find(c => c.key === key)?.label || key}</option>
              {options.map(o => <option key={o} value={o}>{o}</option>)}
            </select>
          ))}
          {(q || Object.values(columnFilters).some(Boolean)) && (
            <Button size="sm" variant="ghost" onClick={() => { setQ(''); setColumnFilters({}); setPage(0) }}>
              <X className="h-3.5 w-3.5" /> Clear
            </Button>
          )}
        </div>
        <div className="flex items-center gap-2">
          {someSelected && bulkActions?.map((b, i) => (
            <Button key={i} size="sm" variant={b.variant || 'outline'} onClick={() => b.onClick(selectedRows)}>
              {b.icon} {b.label} ({selected.size})
            </Button>
          ))}
          {exportable && view.length > 0 && (
            <Button size="sm" variant="outline" onClick={handleExport}>
              <Download className="h-3.5 w-3.5" /> Export CSV
            </Button>
          )}
        </div>
      </div>

      {isLoading ? (
        <Skeleton className="h-64" />
      ) : view.length === 0 ? (
        <EmptyState icon={emptyIcon} title={emptyTitle} description={emptyDescription} />
      ) : (
        <Card>
          <CardContent className="p-0">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                  <tr>
                    {bulkActions && bulkActions.length > 0 && (
                      <th className="p-3 w-10">
                        <Checkbox checked={allOnPageSelected} onCheckedChange={v => toggleAllOnPage(Boolean(v))} />
                      </th>
                    )}
                    {columns.map(c => (
                      <th
                        key={c.key}
                        className={cn('p-3 select-none', `text-${c.align || 'left'}`, c.sortable !== false && 'cursor-pointer hover:bg-slate-100')}
                        style={c.width ? { width: c.width } : undefined}
                        onClick={() => c.sortable !== false && toggleSort(c.key)}
                      >
                        <div className={cn('flex items-center gap-1', c.align === 'right' && 'justify-end', c.align === 'center' && 'justify-center')}>
                          {c.label}
                          {c.sortable !== false && (
                            sortKey === c.key
                              ? (sortDir === 'asc' ? <ArrowUp className="h-3 w-3" /> : <ArrowDown className="h-3 w-3" />)
                              : <ArrowUpDown className="h-3 w-3 text-slate-300" />
                          )}
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="text-sm">
                  {visible.map((row, idx) => {
                    const rowId = id(row) || String(idx)
                    return (
                      <tr
                        key={rowId}
                        className={cn(
                          'border-t border-slate-100 transition',
                          onRowClick && 'cursor-pointer hover:bg-slate-50',
                          selected.has(rowId) && 'bg-violet-50',
                        )}
                        onClick={onRowClick ? (e) => {
                          if ((e.target as HTMLElement).closest('[data-bulk-cell]')) return
                          onRowClick(row)
                        } : undefined}
                      >
                        {bulkActions && bulkActions.length > 0 && (
                          <td className="p-3" data-bulk-cell>
                            <Checkbox
                              checked={selected.has(rowId)}
                              onCheckedChange={v => {
                                setSelected(prev => {
                                  const n = new Set(prev)
                                  if (v) n.add(rowId); else n.delete(rowId)
                                  return n
                                })
                              }}
                            />
                          </td>
                        )}
                        {columns.map(c => (
                          <td key={c.key} className={`p-3 text-${c.align || 'left'}`}>
                            {c.render ? c.render(row) : String(get(row, c.key) ?? '—')}
                          </td>
                        ))}
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
            {paginated && view.length > pageSize && (
              <div className="flex items-center justify-between p-3 border-t text-xs text-slate-500">
                <span>Showing {page * pageSize + 1}–{Math.min((page + 1) * pageSize, view.length)} of {view.length}</span>
                <div className="flex items-center gap-1">
                  <Button size="sm" variant="ghost" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>
                    <ChevronLeft className="h-3.5 w-3.5" /> Prev
                  </Button>
                  <span className="px-2">Page {page + 1} of {totalPages}</span>
                  <Button size="sm" variant="ghost" onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>
                    Next <ChevronRight className="h-3.5 w-3.5" />
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}
