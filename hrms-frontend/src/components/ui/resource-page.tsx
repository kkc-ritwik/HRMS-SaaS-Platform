/**
 * ResourcePage<T> — declarative full-CRUD list page.
 *
 * Combines DataList + FormDialog + ConfirmDialog into a single component so any
 * backend resource can get list / create / edit / delete / custom state-transition
 * actions with a few lines of config. This is the standard way to surface a
 * backend controller's full verb set (GET/POST/PUT/DELETE + action endpoints) in
 * the UI without bespoke wiring per page.
 *
 *   <ResourcePage<LeaveType>
 *     title="Leave Types" icon={<Calendar/>}
 *     queryKey={['leave-types']}
 *     fetcher={() => Catalog.leaves.types.list()}
 *     columns={[{ key:'name', label:'Name' }, ...]}
 *     formFields={[{ name:'name', label:'Name', type:'text', required:true }, ...]}
 *     onCreate={(v) => Catalog.leaves.types.create(v)}
 *     onUpdate={(id,v) => Catalog.leaves.types.update(id, v)}
 *     onDelete={(id) => Catalog.leaves.types.delete(id)}
 *     rowActions={(r) => [{ label:'Activate', show: r.status==='DRAFT', run: () => Catalog.x.activate(r.id) }]}
 *   />
 */
import { useState, type ReactNode } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { Plus, Pencil, Trash2, MoreHorizontal } from 'lucide-react'
import { Button } from './button'
import { DataList, type Column } from './data-list'
import { FormDialog, type FormField } from './form-dialog'
import { ConfirmDialog } from './confirm-dialog'
import {
  DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger,
} from './dropdown-menu'
import { toast } from 'sonner'

type AnyRow = Record<string, unknown>

/** Unwrap {content} | {data:{content}} | {data:[]} | [] into a flat array. */
export function unwrapRows<T = AnyRow>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[]; data?: { content?: T[] } | T[] } | undefined
  if (!o) return []
  if (Array.isArray(o.content)) return o.content
  if (Array.isArray(o.data)) return o.data as T[]
  if (o.data && Array.isArray((o.data as { content?: T[] }).content)) return (o.data as { content: T[] }).content
  return []
}

export interface RowAction<T> {
  label: string
  icon?: ReactNode
  /** Whether to show this action for the given row (default: always). */
  show?: boolean
  /** Destructive styling + confirm prompt. */
  destructive?: boolean
  /** Confirmation prompt; if set, a confirm dialog is shown before running. */
  confirm?: string
  run: (row: T) => Promise<unknown> | unknown
}

interface ResourcePageProps<T> {
  title: string
  description?: string
  icon?: ReactNode
  queryKey: unknown[]
  fetcher: () => Promise<unknown>
  columns: Column<T>[]
  emptyTitle?: string
  emptyDescription?: string
  filters?: Record<string, string[]>

  /** Create */
  formFields?: FormField[]
  createTitle?: string
  onCreate?: (values: Record<string, unknown>) => Promise<unknown> | unknown

  /** Edit (defaults formFields, prefills from row) */
  editFields?: FormField[]
  editTitle?: string
  onUpdate?: (id: string, values: Record<string, unknown>) => Promise<unknown> | unknown
  /** Build the initial form values from a row when editing. */
  toForm?: (row: T) => Record<string, unknown>

  /** Delete */
  onDelete?: (id: string, row: T) => Promise<unknown> | unknown
  deletePrompt?: string

  /** Custom state-transition actions (approve / reject / activate / close / …). */
  rowActions?: (row: T) => RowAction<T>[]

  /** Navigate to a detail page on row click. */
  rowHref?: (row: T) => string

  /** Extra header buttons (left of the New button). */
  headerExtra?: ReactNode
  rowKey?: string
}

export function ResourcePage<T extends AnyRow>({
  title, description, icon, queryKey, fetcher, columns,
  emptyTitle, emptyDescription, filters,
  formFields, createTitle, onCreate,
  editFields, editTitle, onUpdate, toForm,
  onDelete, deletePrompt,
  rowActions, rowHref, headerExtra, rowKey = 'id',
}: ResourcePageProps<T>) {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const [creating, setCreating] = useState(false)
  const [editRow, setEditRow] = useState<T | null>(null)
  const [deleteRow, setDeleteRow] = useState<T | null>(null)
  const [confirmAction, setConfirmAction] = useState<{ action: RowAction<T>; row: T } | null>(null)

  const { data, isLoading } = useQuery({ queryKey, queryFn: fetcher })
  const rows = unwrapRows<T>(data)

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
  const actionMut = useMutation({
    mutationFn: ({ action, row }: { action: RowAction<T>; row: T }) => Promise.resolve(action.run(row)),
    onSuccess: (_d, vars) => { toast.success(`${vars.action.label} done`); invalidate(); setConfirmAction(null) },
    onError: (_e, vars) => { toast.error(`${vars.action.label} failed`); setConfirmAction(null) },
  })

  const hasRowControls = !!(onUpdate || onDelete || rowActions)

  const runAction = (action: RowAction<T>, row: T) => {
    if (action.confirm) setConfirmAction({ action, row })
    else actionMut.mutate({ action, row })
  }

  const allColumns: Column<T>[] = hasRowControls
    ? [
        ...columns,
        {
          key: '__actions', label: '', sortable: false, align: 'right', width: '60px',
          render: (row: T) => {
            const acts = (rowActions?.(row) ?? []).filter(a => a.show !== false)
            const hasMenu = acts.length > 0 || !!onUpdate || !!onDelete
            if (!hasMenu) return null
            return (
              <div onClick={e => e.stopPropagation()} className="flex justify-end">
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button size="sm" variant="ghost" className="h-8 w-8 p-0"><MoreHorizontal className="h-4 w-4" /></Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end">
                    {onUpdate && (
                      <DropdownMenuItem onClick={() => setEditRow(row)}><Pencil className="h-3.5 w-3.5 mr-2" /> Edit</DropdownMenuItem>
                    )}
                    {acts.map((a, i) => (
                      <DropdownMenuItem key={i} onClick={() => runAction(a, row)} className={a.destructive ? 'text-red-600' : ''}>
                        {a.icon && <span className="mr-2">{a.icon}</span>}{a.label}
                      </DropdownMenuItem>
                    ))}
                    {onDelete && (
                      <DropdownMenuItem onClick={() => setDeleteRow(row)} className="text-red-600"><Trash2 className="h-3.5 w-3.5 mr-2" /> Delete</DropdownMenuItem>
                    )}
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>
            )
          },
        },
      ]
    : columns

  const fieldsForEdit = editFields || formFields

  return (
    <>
      <DataList<T>
        title={title}
        description={description}
        rowKey={rowKey}
        action={(onCreate || headerExtra) ? (
          <div className="flex items-center gap-2">
            {headerExtra}
            {onCreate && formFields && (
              <Button size="sm" onClick={() => setCreating(true)}><Plus className="h-4 w-4 mr-1" /> New</Button>
            )}
          </div>
        ) : undefined}
        data={rows}
        isLoading={isLoading}
        columns={allColumns}
        filters={filters}
        emptyIcon={icon}
        emptyTitle={emptyTitle || `No ${title.toLowerCase()}`}
        emptyDescription={emptyDescription}
        onRowClick={rowHref ? (row: T) => navigate(rowHref(row)) : undefined}
      />

      {onCreate && formFields && (
        <FormDialog
          open={creating} onOpenChange={setCreating}
          title={createTitle || `Create ${title.replace(/s$/, '')}`}
          fields={formFields}
          onSubmit={v => createMut.mutateAsync(v)}
        />
      )}

      {onUpdate && fieldsForEdit && editRow && (
        <FormDialog
          open={!!editRow} onOpenChange={o => !o && setEditRow(null)}
          title={editTitle || `Edit ${title.replace(/s$/, '')}`}
          fields={fieldsForEdit}
          initialValues={toForm ? toForm(editRow) : (editRow as Record<string, unknown>)}
          onSubmit={v => updateMut.mutateAsync({ id: String(editRow[rowKey]), v })}
        />
      )}

      {onDelete && (
        <ConfirmDialog
          open={!!deleteRow} onOpenChange={o => !o && setDeleteRow(null)}
          title="Delete this record?"
          description={deletePrompt || 'This action cannot be undone.'}
          confirmLabel="Delete" variant="destructive"
          loading={deleteMut.isPending}
          onConfirm={() => deleteRow && deleteMut.mutate(deleteRow)}
        />
      )}

      {confirmAction && (
        <ConfirmDialog
          open={!!confirmAction} onOpenChange={o => !o && setConfirmAction(null)}
          title={confirmAction.action.label}
          description={confirmAction.action.confirm}
          confirmLabel={confirmAction.action.label}
          variant={confirmAction.action.destructive ? 'destructive' : 'default'}
          loading={actionMut.isPending}
          onConfirm={() => actionMut.mutate(confirmAction)}
        />
      )}
    </>
  )
}
