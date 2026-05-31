import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Type, Hash, Mail, Calendar, ChevronDown, CheckSquare, ToggleLeft,
  FileText, AlignLeft, Plus, Trash2, GripVertical, ArrowLeft, Save, Eye,
} from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { Switch } from '@/components/ui/switch'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { formService } from '@/services/extendedServices'
import { toast } from 'sonner'

type FieldKind = 'text' | 'longtext' | 'number' | 'email' | 'date' | 'select' | 'checkbox' | 'toggle' | 'file' | 'heading'

interface BuilderField {
  id: string
  kind: FieldKind
  label: string
  placeholder?: string
  required?: boolean
  options?: string[]
}

const PALETTE: Array<{ kind: FieldKind; label: string; icon: typeof Type }> = [
  { kind: 'text', label: 'Short text', icon: Type },
  { kind: 'longtext', label: 'Long text', icon: AlignLeft },
  { kind: 'number', label: 'Number', icon: Hash },
  { kind: 'email', label: 'Email', icon: Mail },
  { kind: 'date', label: 'Date', icon: Calendar },
  { kind: 'select', label: 'Dropdown', icon: ChevronDown },
  { kind: 'checkbox', label: 'Checkbox', icon: CheckSquare },
  { kind: 'toggle', label: 'Toggle', icon: ToggleLeft },
  { kind: 'file', label: 'File upload', icon: FileText },
  { kind: 'heading', label: 'Section heading', icon: Type },
]

export function FormBuilderPage() {
  const navigate = useNavigate()
  const [formName, setFormName] = useState('Untitled form')
  const [category, setCategory] = useState('HR')
  const [fields, setFields] = useState<BuilderField[]>([])
  const [selected, setSelected] = useState<string | null>(null)

  const add = (kind: FieldKind) => {
    const def = PALETTE.find(p => p.kind === kind)!
    setFields(f => [...f, {
      id: `f${Date.now()}`,
      kind,
      label: def.label,
      options: kind === 'select' ? ['Option 1', 'Option 2'] : undefined,
    }])
  }

  const update = (id: string, patch: Partial<BuilderField>) => {
    setFields(f => f.map(x => x.id === id ? { ...x, ...patch } : x))
  }
  const remove = (id: string) => setFields(f => f.filter(x => x.id !== id))
  const move = (id: string, dir: -1 | 1) => {
    setFields(f => {
      const idx = f.findIndex(x => x.id === id)
      if (idx < 0) return f
      const ni = idx + dir
      if (ni < 0 || ni >= f.length) return f
      const next = [...f]
      const [it] = next.splice(idx, 1)
      next.splice(ni, 0, it)
      return next
    })
  }

  const save = async () => {
    try {
      await formService.submit('NEW', { name: formName, category, schema: fields })
      toast.success('Form saved')
      navigate('/forms')
    } catch (e) { toast.error(e instanceof Error ? e.message : 'Save failed') }
  }

  const sel = fields.find(f => f.id === selected)

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={() => navigate('/forms')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <PageHeader title="Form Builder" description="Drag fields onto the canvas to build a form"
        action={<Button onClick={save}><Save className="h-4 w-4 mr-1" /> Save</Button>}
      />

      <div className="grid grid-cols-12 gap-4">
        {/* Palette */}
        <Card className="col-span-3">
          <CardContent className="p-3 space-y-2">
            <p className="text-xs uppercase font-semibold text-slate-500 mb-2">Field types</p>
            {PALETTE.map(p => {
              const Icon = p.icon
              return (
                <button key={p.kind} onClick={() => add(p.kind)}
                  className="w-full flex items-center gap-2 p-2 rounded-lg border border-slate-200 hover:bg-violet-50 hover:border-violet-300 text-sm text-left transition">
                  <Icon className="h-4 w-4 text-slate-500" />{p.label}
                  <Plus className="h-3 w-3 ml-auto text-slate-400" />
                </button>
              )
            })}
          </CardContent>
        </Card>

        {/* Canvas + Preview */}
        <div className="col-span-6">
          <Tabs defaultValue="design">
            <TabsList>
              <TabsTrigger value="design">Design</TabsTrigger>
              <TabsTrigger value="preview"><Eye className="h-3.5 w-3.5 mr-1" />Preview</TabsTrigger>
            </TabsList>
            <TabsContent value="design">
              <Card>
                <CardContent className="p-4 min-h-[500px] space-y-3">
                  {fields.length === 0 ? (
                    <div className="text-center py-16 text-slate-400 text-sm border-2 border-dashed rounded-lg">
                      Click a field type on the left to add it
                    </div>
                  ) : fields.map(f => (
                    <div key={f.id}
                      onClick={() => setSelected(f.id)}
                      className={`p-3 rounded-lg border cursor-pointer flex items-start gap-2 ${selected === f.id ? 'border-violet-400 bg-violet-50' : 'border-slate-200 hover:bg-slate-50'}`}
                    >
                      <GripVertical className="h-4 w-4 text-slate-300 mt-1" />
                      <div className="flex-1">
                        {f.kind === 'heading' ? (
                          <h3 className="font-semibold text-slate-700">{f.label}</h3>
                        ) : (
                          <>
                            <div className="text-xs text-slate-500 uppercase">
                              {f.label}{f.required && <span className="text-red-500 ml-1">*</span>}
                            </div>
                            <div className="text-xs text-slate-400 mt-1">[{f.kind}]</div>
                          </>
                        )}
                      </div>
                      <button onClick={e => { e.stopPropagation(); move(f.id, -1) }} className="text-xs text-slate-400 hover:text-slate-700">↑</button>
                      <button onClick={e => { e.stopPropagation(); move(f.id, 1) }} className="text-xs text-slate-400 hover:text-slate-700">↓</button>
                      <button onClick={e => { e.stopPropagation(); remove(f.id) }} className="text-xs text-red-400 hover:text-red-600">
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </TabsContent>
            <TabsContent value="preview">
              <Card>
                <CardContent className="p-6 space-y-4 max-w-md">
                  <h2 className="font-bold text-xl">{formName}</h2>
                  {fields.map(f => {
                    if (f.kind === 'heading') return <h3 key={f.id} className="font-semibold text-slate-700 mt-4">{f.label}</h3>
                    return (
                      <div key={f.id}>
                        <Label>{f.label}{f.required && <span className="text-red-500 ml-1">*</span>}</Label>
                        {f.kind === 'longtext' ? <textarea className="w-full mt-1 rounded border p-2 text-sm" rows={3} /> :
                          f.kind === 'select' ? <select className="w-full mt-1 rounded border p-2 text-sm">{f.options?.map(o => <option key={o}>{o}</option>)}</select> :
                          f.kind === 'checkbox' ? <input type="checkbox" className="mt-2" /> :
                          f.kind === 'toggle' ? <Switch /> :
                          f.kind === 'file' ? <input type="file" className="mt-1 text-sm" /> :
                          <Input type={f.kind === 'number' ? 'number' : f.kind === 'email' ? 'email' : f.kind === 'date' ? 'date' : 'text'} placeholder={f.placeholder} />
                        }
                      </div>
                    )
                  })}
                  <Button>Submit</Button>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* Inspector */}
        <Card className="col-span-3">
          <CardContent className="p-3 space-y-3">
            <div><Label>Form name</Label><Input value={formName} onChange={e => setFormName(e.target.value)} /></div>
            <div><Label>Category</Label><Input value={category} onChange={e => setCategory(e.target.value)} /></div>
            {sel && (
              <div className="pt-3 border-t space-y-2">
                <p className="text-xs uppercase font-semibold text-slate-500">Selected field</p>
                <div><Label>Label</Label><Input value={sel.label} onChange={e => update(sel.id, { label: e.target.value })} /></div>
                {sel.kind !== 'heading' && (
                  <>
                    <div><Label>Placeholder</Label><Input value={sel.placeholder || ''} onChange={e => update(sel.id, { placeholder: e.target.value })} /></div>
                    <label className="flex items-center gap-2 text-sm">
                      <input type="checkbox" checked={!!sel.required} onChange={e => update(sel.id, { required: e.target.checked })} /> Required
                    </label>
                  </>
                )}
                {sel.kind === 'select' && (
                  <div>
                    <Label>Options (one per line)</Label>
                    <textarea
                      className="w-full mt-1 rounded border p-2 text-sm" rows={4}
                      value={sel.options?.join('\n') || ''}
                      onChange={e => update(sel.id, { options: e.target.value.split('\n').filter(Boolean) })}
                    />
                  </div>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
