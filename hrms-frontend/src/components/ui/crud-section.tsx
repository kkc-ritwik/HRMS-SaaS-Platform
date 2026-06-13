/**
 * CrudSection — an embedded, fully-CRUD list for a nested sub-resource inside a
 * detail page (e.g. an employee's addresses, education, family). Renders a Card with
 * an "Add" button, each item with edit/delete actions, a FormDialog for create/edit
 * and a ConfirmDialog for delete. Invalidates `queryKey` after every mutation.
 */
import { useState, type ReactNode } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from './card'
import { Button } from './button'
import { Skeleton } from './skeleton'
import { EmptyState } from './empty-state'
import { FormDialog, type FormField } from './form-dialog'
import { ConfirmDialog } from './confirm-dialog'
import { toast } from 'sonner'

type AnyRow = Record<string, unknown>

interface CrudSectionProps<T extends AnyRow> {
  title: string
  icon?: ReactNode
  items: T[]
  loading?: boolean
  emptyText?: string
  queryKey: unknown[]
  fields: FormField[]
  onCreate?: (values: Record<string, unknown>) => Promise<unknown> | unknown
  onUpdate?: (id: string, values: Record<string, unknown>) => Promise<unknown> | unknown
  onDelete?: (id: string, row: T) => Promise<unknown> | unknown
  renderItem: (row: T) => ReactNode
  toForm?: (row: T) => Record<string, unknown>
  className?: string
  rowKey?: string
}

export function CrudSection<T extends AnyRow>({
  title, icon, items, loading, emptyText, queryKey, fields,
  onCreate, onUpdate, onDelete, renderItem, toForm, className, rowKey = 'id',
}: CrudSectionProps<T>) {
  const qc = useQueryClient()
  const [creating, setCreating] = useState(false)
  const [editRow, setEditRow] = useState<T | null>(null)
  const [deleteRow, setDeleteRow] = useState<T | null>(null)

  const invalidate = () => qc.invalidateQueries({ queryKey })

  const createMut = useMutation({
    mutationFn: (v: Record<string, unknown>) => Promise.resolve(onCreate?.(v)),
    onSuccess: () => { invalidate(); setCreating(false) },
  })
  const updateMut = useMutation({
    mutationFn: ({ id, v }: { id: string; v: Record<string, unknown> }) => Promise.resolve(onUpdate?.(id, v)),
    onSuccess: () => { invalidate(); setEditRow(null) },
  })
  const deleteMut = useMutation({
    mutationFn: (row: T) => Promise.resolve(onDelete?.(String(row[rowKey]), row)),
    onSuccess: () => { toast.success('Deleted'); invalidate(); setDeleteRow(null) },
    onError: () => { toast.error('Delete failed'); setDeleteRow(null) },
  })

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm">{title}</CardTitle>
          {onCreate && (
            <Button size="sm" variant="ghost" className="h-7" onClick={() => setCreating(true)}>
              <Plus className="h-3.5 w-3.5 mr-1" /> Add
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent>
        {loading ? <Skeleton className="h-20" /> : items.length === 0 ? (
          <EmptyState icon={icon} title={emptyText || `No ${title.toLowerCase()}`} />
        ) : (
          <div>
            {items.map(row => (
              <div key={String(row[rowKey])} className="group flex items-start justify-between gap-2 border-b border-slate-100 last:border-0 py-2">
                <div className="min-w-0 flex-1">{renderItem(row)}</div>
                {(onUpdate || onDelete) && (
                  <div className="flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                    {onUpdate && (
                      <Button size="sm" variant="ghost" className="h-7 w-7 p-0" onClick={() => setEditRow(row)}>
                        <Pencil className="h-3.5 w-3.5" />
                      </Button>
                    )}
                    {onDelete && (
                      <Button size="sm" variant="ghost" className="h-7 w-7 p-0 text-red-500" onClick={() => setDeleteRow(row)}>
                        <Trash2 className="h-3.5 w-3.5" />
                      </Button>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </CardContent>

      {onCreate && (
        <FormDialog
          open={creating} onOpenChange={setCreating}
          title={`Add ${title.replace(/s$/, '')}`} fields={fields}
          onSubmit={v => createMut.mutateAsync(v)}
        />
      )}
      {onUpdate && editRow && (
        <FormDialog
          open={!!editRow} onOpenChange={o => !o && setEditRow(null)}
          title={`Edit ${title.replace(/s$/, '')}`} fields={fields}
          initialValues={toForm ? toForm(editRow) : (editRow as Record<string, unknown>)}
          onSubmit={v => updateMut.mutateAsync({ id: String(editRow[rowKey]), v })}
        />
      )}
      {onDelete && (
        <ConfirmDialog
          open={!!deleteRow} onOpenChange={o => !o && setDeleteRow(null)}
          title={`Delete this ${title.replace(/s$/, '').toLowerCase()}?`}
          description="This action cannot be undone." confirmLabel="Delete" variant="destructive"
          loading={deleteMut.isPending}
          onConfirm={() => deleteRow && deleteMut.mutate(deleteRow)}
        />
      )}
    </Card>
  )
}
