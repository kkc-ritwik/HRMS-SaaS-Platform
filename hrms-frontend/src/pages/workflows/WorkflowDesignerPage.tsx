import { useState, useRef, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  PlayCircle, StopCircle, GitMerge, GitBranch, CheckSquare, Mail, Webhook,
  Plus, Save, Trash2, ArrowLeft,
} from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { Badge } from '@/components/ui/badge'
import { workflowDefinitionService } from '@/services/extendedServices'
import { toast } from 'sonner'

type NodeType = 'START' | 'APPROVAL' | 'CONDITION' | 'PARALLEL' | 'EMAIL' | 'WEBHOOK' | 'END'

interface FlowNode {
  id: string
  type: NodeType
  label: string
  x: number
  y: number
  config?: Record<string, string>
}

interface Edge { from: string; to: string }

const NODE_DEFS: Array<{ type: NodeType; label: string; icon: typeof PlayCircle; color: string }> = [
  { type: 'START', label: 'Start', icon: PlayCircle, color: 'bg-green-100 text-green-700 border-green-300' },
  { type: 'APPROVAL', label: 'Approval', icon: CheckSquare, color: 'bg-blue-100 text-blue-700 border-blue-300' },
  { type: 'CONDITION', label: 'If/Else', icon: GitBranch, color: 'bg-amber-100 text-amber-700 border-amber-300' },
  { type: 'PARALLEL', label: 'Parallel', icon: GitMerge, color: 'bg-violet-100 text-violet-700 border-violet-300' },
  { type: 'EMAIL', label: 'Send email', icon: Mail, color: 'bg-pink-100 text-pink-700 border-pink-300' },
  { type: 'WEBHOOK', label: 'Webhook', icon: Webhook, color: 'bg-cyan-100 text-cyan-700 border-cyan-300' },
  { type: 'END', label: 'End', icon: StopCircle, color: 'bg-red-100 text-red-700 border-red-300' },
]

export function WorkflowDesignerPage() {
  const navigate = useNavigate()
  const [name, setName] = useState('Untitled workflow')
  const [module, setModule] = useState('LEAVE')
  const [nodes, setNodes] = useState<FlowNode[]>([
    { id: 'n1', type: 'START', label: 'Trigger', x: 100, y: 100 },
    { id: 'n2', type: 'END', label: 'Completed', x: 100, y: 400 },
  ])
  const [edges, setEdges] = useState<Edge[]>([{ from: 'n1', to: 'n2' }])
  const [selected, setSelected] = useState<string | null>(null)
  const [connecting, setConnecting] = useState<string | null>(null)
  const dragState = useRef<{ id: string; offX: number; offY: number } | null>(null)

  const addNode = (type: NodeType) => {
    const def = NODE_DEFS.find(d => d.type === type)!
    setNodes(n => [...n, {
      id: `n${Date.now()}`,
      type, label: def.label,
      x: 300, y: 100 + n.length * 80,
    }])
  }

  const onMouseDown = (e: React.MouseEvent, id: string) => {
    const node = nodes.find(n => n.id === id)
    if (!node) return
    dragState.current = { id, offX: e.clientX - node.x, offY: e.clientY - node.y }
    setSelected(id)
  }
  const onMouseMove = useCallback((e: React.MouseEvent) => {
    if (!dragState.current) return
    const { id, offX, offY } = dragState.current
    setNodes(n => n.map(x => x.id === id ? { ...x, x: e.clientX - offX, y: e.clientY - offY } : x))
  }, [])
  const onMouseUp = () => { dragState.current = null }

  const connectClick = (id: string) => {
    if (connecting && connecting !== id) {
      setEdges(e => [...e, { from: connecting, to: id }])
      setConnecting(null)
    } else {
      setConnecting(id)
    }
  }

  const deleteNode = (id: string) => {
    setNodes(n => n.filter(x => x.id !== id))
    setEdges(e => e.filter(x => x.from !== id && x.to !== id))
    setSelected(null)
  }

  const save = async () => {
    try {
      await workflowDefinitionService.create({ name, module, nodes, edges })
      toast.success('Workflow saved')
      navigate('/workflows')
    } catch (e) {
      toast.error(e instanceof Error ? e.message : 'Save failed')
    }
  }

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={() => navigate('/workflows')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <PageHeader title="Workflow Designer" description="Drag nodes onto the canvas, click to connect"
        action={<Button onClick={save}><Save className="h-4 w-4 mr-1" /> Save</Button>}
      />

      <div className="grid grid-cols-12 gap-4">
        {/* Palette */}
        <Card className="col-span-2">
          <CardContent className="p-3 space-y-2">
            <p className="text-xs uppercase font-semibold text-slate-500 mb-2">Nodes</p>
            {NODE_DEFS.map(d => {
              const Icon = d.icon
              return (
                <button
                  key={d.type}
                  onClick={() => addNode(d.type)}
                  className={`w-full flex items-center gap-2 p-2 rounded-lg border text-left text-sm ${d.color} hover:scale-[1.02] transition`}
                >
                  <Icon className="h-4 w-4" /> {d.label}
                  <Plus className="h-3 w-3 ml-auto" />
                </button>
              )
            })}
          </CardContent>
        </Card>

        {/* Canvas */}
        <Card className="col-span-7">
          <CardContent
            className="p-0 relative overflow-hidden"
            style={{ height: '600px' }}
            onMouseMove={onMouseMove}
            onMouseUp={onMouseUp}
            onMouseLeave={onMouseUp}
          >
            {/* Edges */}
            <svg className="absolute inset-0 pointer-events-none" width="100%" height="100%">
              {edges.map((e, i) => {
                const a = nodes.find(n => n.id === e.from)
                const b = nodes.find(n => n.id === e.to)
                if (!a || !b) return null
                return (
                  <line
                    key={i}
                    x1={a.x + 80} y1={a.y + 30}
                    x2={b.x + 80} y2={b.y + 30}
                    stroke="#94a3b8" strokeWidth="2" strokeDasharray="0"
                    markerEnd="url(#arrow)"
                  />
                )
              })}
              <defs>
                <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
                  <path d="M 0 0 L 10 5 L 0 10 z" fill="#94a3b8" />
                </marker>
              </defs>
            </svg>

            {/* Nodes */}
            {nodes.map(n => {
              const def = NODE_DEFS.find(d => d.type === n.type)!
              const Icon = def.icon
              return (
                <div
                  key={n.id}
                  className={`absolute rounded-lg border-2 px-3 py-2 cursor-move select-none ${def.color} ${selected === n.id ? 'ring-2 ring-violet-500' : ''} ${connecting === n.id ? 'ring-2 ring-amber-500' : ''}`}
                  style={{ left: n.x, top: n.y, width: 160 }}
                  onMouseDown={e => onMouseDown(e, n.id)}
                  onDoubleClick={() => connectClick(n.id)}
                >
                  <div className="flex items-center gap-2">
                    <Icon className="h-4 w-4" />
                    <span className="text-sm font-medium">{n.label}</span>
                  </div>
                </div>
              )
            })}

            {connecting && (
              <div className="absolute top-3 left-3 text-xs bg-amber-100 text-amber-800 px-2 py-1 rounded">
                Double-click the target node to connect
                <button className="ml-2 underline" onClick={() => setConnecting(null)}>Cancel</button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Inspector */}
        <Card className="col-span-3">
          <CardContent className="p-3 space-y-3">
            <div>
              <Label>Workflow name</Label>
              <Input value={name} onChange={e => setName(e.target.value)} />
            </div>
            <div>
              <Label>Module</Label>
              <Input value={module} onChange={e => setModule(e.target.value)} />
            </div>
            {selected && (
              <div className="pt-3 border-t space-y-2">
                <p className="text-xs uppercase font-semibold text-slate-500">Selected node</p>
                <Badge>{nodes.find(n => n.id === selected)?.type}</Badge>
                <Input
                  value={nodes.find(n => n.id === selected)?.label || ''}
                  onChange={e => setNodes(ns => ns.map(x => x.id === selected ? { ...x, label: e.target.value } : x))}
                />
                <div className="flex gap-2">
                  <Button size="sm" variant="outline" onClick={() => connectClick(selected)}>Connect →</Button>
                  <Button size="sm" variant="outline" onClick={() => deleteNode(selected)}><Trash2 className="h-3.5 w-3.5" /></Button>
                </div>
              </div>
            )}
            <div className="text-xs text-slate-400 pt-3 border-t">
              <p>💡 Tip: drag nodes, double-click two nodes to connect them, click Save to publish.</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
