import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft, Save, Bold, Italic, Underline, List, ListOrdered, Heading2,
  Link2, Variable, Eye,
} from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { toast } from 'sonner'

const VARIABLES = [
  { key: 'employeeName', label: 'Employee name' },
  { key: 'employeeCode', label: 'Employee code' },
  { key: 'companyName', label: 'Company name' },
  { key: 'designation', label: 'Designation' },
  { key: 'department', label: 'Department' },
  { key: 'joinDate', label: 'Join date' },
  { key: 'effectiveDate', label: 'Effective date' },
  { key: 'newSalary', label: 'New salary' },
  { key: 'currentSalary', label: 'Current salary' },
  { key: 'signatoryName', label: 'Signatory name' },
  { key: 'signatoryTitle', label: 'Signatory title' },
]

const TEMPLATES = [
  { id: 'employment-offer', label: 'Employment offer', body: '<h2>Offer of Employment</h2><p>Dear {{employeeName}},</p><p>We are pleased to offer you the position of <strong>{{designation}}</strong> at <strong>{{companyName}}</strong>, effective <strong>{{effectiveDate}}</strong>.</p>' },
  { id: 'salary-revision', label: 'Salary revision', body: '<h2>Salary Revision</h2><p>Dear {{employeeName}},</p><p>Effective {{effectiveDate}}, your annual compensation has been revised from {{currentSalary}} to <strong>{{newSalary}}</strong>.</p>' },
  { id: 'welcome-email', label: 'Welcome email', body: '<p>Hi {{employeeName}},</p><p>Welcome to {{companyName}}! We are thrilled to have you on the {{department}} team.</p>' },
  { id: 'leave-approved', label: 'Leave approved (email)', body: '<p>Hi {{employeeName}},</p><p>Your leave request from {{fromDate}} to {{toDate}} has been approved.</p>' },
]

export function TemplateEditorPage() {
  const navigate = useNavigate()
  const [templateId, setTemplateId] = useState(TEMPLATES[0].id)
  const [name, setName] = useState('Custom letter')
  const [subject, setSubject] = useState('Your offer letter')
  const [body, setBody] = useState(TEMPLATES[0].body)

  const exec = (cmd: string, val?: string) => {
    document.execCommand(cmd, false, val)
  }

  const insertVar = (key: string) => {
    document.execCommand('insertText', false, ` {{${key}}} `)
  }

  const loadTemplate = (id: string) => {
    setTemplateId(id)
    const t = TEMPLATES.find(x => x.id === id)
    if (t) { setBody(t.body); setName(t.label) }
  }

  const previewHtml = body
    .replace(/\{\{employeeName\}\}/g, 'John Doe')
    .replace(/\{\{employeeCode\}\}/g, 'EMP-001')
    .replace(/\{\{companyName\}\}/g, 'Acme Corp')
    .replace(/\{\{designation\}\}/g, 'Senior Engineer')
    .replace(/\{\{department\}\}/g, 'Engineering')
    .replace(/\{\{joinDate\}\}/g, '1 Apr 2026')
    .replace(/\{\{effectiveDate\}\}/g, '15 Jun 2026')
    .replace(/\{\{newSalary\}\}/g, '₹18,00,000')
    .replace(/\{\{currentSalary\}\}/g, '₹15,00,000')
    .replace(/\{\{signatoryName\}\}/g, 'Jane Smith')
    .replace(/\{\{signatoryTitle\}\}/g, 'Head of People')

  return (
    <div className="space-y-4">
      <Button variant="ghost" size="sm" onClick={() => navigate('/settings')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <PageHeader title="Template Editor" description="Design letter and email templates with merge variables"
        action={<Button onClick={() => toast.success('Template saved')}><Save className="h-4 w-4 mr-1" /> Save template</Button>}
      />

      <div className="grid grid-cols-12 gap-4">
        {/* Template list */}
        <Card className="col-span-2">
          <CardContent className="p-3 space-y-1">
            <p className="text-xs uppercase font-semibold text-slate-500 mb-2">Templates</p>
            {TEMPLATES.map(t => (
              <button key={t.id} onClick={() => loadTemplate(t.id)}
                className={`w-full text-left text-sm p-2 rounded ${templateId === t.id ? 'bg-violet-100 text-violet-800' : 'hover:bg-slate-50'}`}>
                {t.label}
              </button>
            ))}
          </CardContent>
        </Card>

        {/* Editor */}
        <div className="col-span-7">
          <Card>
            <CardContent className="p-4 space-y-3">
              <div className="grid grid-cols-2 gap-3">
                <div><Label>Template name</Label><Input value={name} onChange={e => setName(e.target.value)} /></div>
                <div><Label>Subject (email)</Label><Input value={subject} onChange={e => setSubject(e.target.value)} /></div>
              </div>
              <Tabs defaultValue="design">
                <TabsList>
                  <TabsTrigger value="design">Design</TabsTrigger>
                  <TabsTrigger value="preview"><Eye className="h-3.5 w-3.5 mr-1" />Preview</TabsTrigger>
                  <TabsTrigger value="source">HTML</TabsTrigger>
                </TabsList>

                <TabsContent value="design">
                  <div className="border rounded-lg">
                    {/* Toolbar */}
                    <div className="flex items-center gap-1 p-2 border-b bg-slate-50 flex-wrap">
                      <Button size="icon" variant="ghost" onClick={() => exec('bold')}><Bold className="h-3.5 w-3.5" /></Button>
                      <Button size="icon" variant="ghost" onClick={() => exec('italic')}><Italic className="h-3.5 w-3.5" /></Button>
                      <Button size="icon" variant="ghost" onClick={() => exec('underline')}><Underline className="h-3.5 w-3.5" /></Button>
                      <Button size="icon" variant="ghost" onClick={() => exec('formatBlock', '<h2>')}><Heading2 className="h-3.5 w-3.5" /></Button>
                      <Button size="icon" variant="ghost" onClick={() => exec('insertUnorderedList')}><List className="h-3.5 w-3.5" /></Button>
                      <Button size="icon" variant="ghost" onClick={() => exec('insertOrderedList')}><ListOrdered className="h-3.5 w-3.5" /></Button>
                      <Button size="icon" variant="ghost" onClick={() => { const u = prompt('URL?'); if (u) exec('createLink', u) }}><Link2 className="h-3.5 w-3.5" /></Button>
                      <div className="ml-auto">
                        <Select onValueChange={insertVar}>
                          <SelectTrigger className="h-8 w-44 text-xs"><Variable className="h-3.5 w-3.5 mr-1" /><SelectValue placeholder="Insert variable..." /></SelectTrigger>
                          <SelectContent>{VARIABLES.map(v => <SelectItem key={v.key} value={v.key}>{v.label}</SelectItem>)}</SelectContent>
                        </Select>
                      </div>
                    </div>
                    {/* contentEditable canvas */}
                    <div
                      contentEditable
                      suppressContentEditableWarning
                      className="min-h-[400px] p-4 prose prose-sm max-w-none focus:outline-none"
                      dangerouslySetInnerHTML={{ __html: body }}
                      onBlur={e => setBody((e.target as HTMLDivElement).innerHTML)}
                    />
                  </div>
                </TabsContent>
                <TabsContent value="preview">
                  <Card className="border-2 border-dashed">
                    <CardContent className="p-6 prose prose-sm max-w-none" dangerouslySetInnerHTML={{ __html: previewHtml }} />
                  </Card>
                </TabsContent>
                <TabsContent value="source">
                  <textarea
                    className="w-full font-mono text-xs p-3 border rounded-lg"
                    rows={20}
                    value={body}
                    onChange={e => setBody(e.target.value)}
                  />
                </TabsContent>
              </Tabs>
            </CardContent>
          </Card>
        </div>

        {/* Variables sidebar */}
        <Card className="col-span-3">
          <CardContent className="p-3 space-y-2">
            <p className="text-xs uppercase font-semibold text-slate-500 mb-2">Merge variables</p>
            {VARIABLES.map(v => (
              <button key={v.key} onClick={() => insertVar(v.key)}
                className="w-full text-left text-xs p-2 rounded border hover:bg-violet-50 hover:border-violet-300">
                <span className="font-mono text-violet-600">{`{{${v.key}}}`}</span>
                <span className="block text-slate-500 mt-0.5">{v.label}</span>
              </button>
            ))}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
