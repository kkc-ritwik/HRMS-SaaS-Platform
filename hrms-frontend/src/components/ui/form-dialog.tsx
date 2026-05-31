import { useState, useEffect, type ReactNode } from 'react'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogBody, DialogFooter, DialogDescription } from './dialog'
import { Button } from './button'
import { Input } from './input'
import { Label } from './label'
import { Textarea } from './textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './select'
import { Switch } from './switch'
import { toast } from 'sonner'

export type FieldType =
  | 'text' | 'number' | 'email' | 'tel' | 'url' | 'date' | 'time' | 'datetime-local'
  | 'textarea' | 'select' | 'switch' | 'currency'

export interface FormField {
  name: string
  label: string
  type: FieldType
  required?: boolean
  placeholder?: string
  options?: Array<{ value: string; label: string }>
  defaultValue?: unknown
  span?: 1 | 2
  helper?: string
}

interface FormDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
  fields: FormField[]
  initialValues?: Record<string, unknown>
  onSubmit: (values: Record<string, unknown>) => Promise<unknown> | unknown
  submitLabel?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
  extraContent?: ReactNode
}

export function FormDialog({
  open, onOpenChange, title, description, fields, initialValues,
  onSubmit, submitLabel = 'Save', size = 'lg', extraContent,
}: FormDialogProps) {
  const [values, setValues] = useState<Record<string, unknown>>(initialValues || {})
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (open) {
      const seed: Record<string, unknown> = { ...(initialValues || {}) }
      fields.forEach(f => {
        if (seed[f.name] === undefined && f.defaultValue !== undefined) {
          seed[f.name] = f.defaultValue
        }
      })
      setValues(seed)
    }
  }, [open, initialValues, fields])

  const set = (name: string, v: unknown) => setValues(prev => ({ ...prev, [name]: v }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    // Required validation
    for (const f of fields) {
      if (f.required && (values[f.name] === undefined || values[f.name] === '' || values[f.name] === null)) {
        toast.error(`${f.label} is required`)
        return
      }
    }
    setSubmitting(true)
    try {
      await onSubmit(values)
      toast.success('Saved')
      onOpenChange(false)
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Save failed'
      toast.error(msg)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent size={size}>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>{title}</DialogTitle>
            {description && <DialogDescription>{description}</DialogDescription>}
          </DialogHeader>
          <DialogBody className="max-h-[60vh] overflow-y-auto">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {fields.map(f => (
                <div key={f.name} className={f.span === 2 ? 'md:col-span-2' : ''}>
                  <Label className="text-xs uppercase">
                    {f.label} {f.required && <span className="text-red-500">*</span>}
                  </Label>
                  {f.type === 'textarea' ? (
                    <Textarea
                      value={(values[f.name] as string) ?? ''}
                      onChange={e => set(f.name, e.target.value)}
                      placeholder={f.placeholder}
                      rows={3}
                    />
                  ) : f.type === 'select' ? (
                    <Select value={(values[f.name] as string) ?? ''} onValueChange={v => set(f.name, v)}>
                      <SelectTrigger><SelectValue placeholder={f.placeholder || `Select ${f.label}`} /></SelectTrigger>
                      <SelectContent>
                        {f.options?.map(o => <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>)}
                      </SelectContent>
                    </Select>
                  ) : f.type === 'switch' ? (
                    <div className="pt-2">
                      <Switch checked={Boolean(values[f.name])} onCheckedChange={v => set(f.name, v)} />
                    </div>
                  ) : f.type === 'currency' || f.type === 'number' ? (
                    <Input
                      type="number"
                      value={(values[f.name] as number | string) ?? ''}
                      onChange={e => set(f.name, e.target.value === '' ? '' : Number(e.target.value))}
                      placeholder={f.placeholder}
                    />
                  ) : (
                    <Input
                      type={f.type}
                      value={(values[f.name] as string) ?? ''}
                      onChange={e => set(f.name, e.target.value)}
                      placeholder={f.placeholder}
                    />
                  )}
                  {f.helper && <p className="text-xs text-slate-400 mt-1">{f.helper}</p>}
                </div>
              ))}
            </div>
            {extraContent}
          </DialogBody>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={submitting}>Cancel</Button>
            <Button type="submit" disabled={submitting}>{submitting ? 'Saving...' : submitLabel}</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
