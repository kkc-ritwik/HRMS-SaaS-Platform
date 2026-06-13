/**
 * Course detail — modules, assessments, enrollments, certifications, status.
 */
import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, BookOpen, FileQuestion, Users, Award, BarChart3, Play,
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { Progress } from '@/components/ui/progress'
import { CrudSection } from '@/components/ui/crud-section'
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

export function CourseDetailPage() {
  const { id = '' } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const qc = useQueryClient()
  const [tab, setTab] = useState('overview')

  const courseQ = useQuery({ queryKey: ['course', id], queryFn: () => Catalog.courses.get(id), enabled: !!id })
  const modulesQ = useQuery({ queryKey: ['course-mods', id], queryFn: () => Catalog.courses.modulesForCourse(id), enabled: !!id })
  const assessQ = useQuery({ queryKey: ['course-assess', id], queryFn: () => Catalog.courses.assessmentsForCourse(id), enabled: !!id && tab === 'assessments' })
  const enrollQ = useQuery({ queryKey: ['course-enroll', id], queryFn: () => Catalog.courses.enrollmentsForCourse(id), enabled: !!id && tab === 'enrollments' })
  const statusQ = useQuery({ queryKey: ['course-status', id], queryFn: () => Catalog.courses.status(id), enabled: !!id && tab === 'analytics' })

  const publish = useMutation({ mutationFn: () => Catalog.courses.publish(id), onSuccess: () => { toast.success('Published'); qc.invalidateQueries({ queryKey: ['course', id] }) } })
  const archive = useMutation({ mutationFn: () => Catalog.courses.archive(id), onSuccess: () => { toast.success('Archived'); qc.invalidateQueries({ queryKey: ['course', id] }) } })

  if (courseQ.isLoading) return <Skeleton className="h-96" />
  const c = (courseQ.data as AnyObj) || {}
  if (!c.id) return <p>Course not found</p>

  const enrollments = rows<AnyObj>(enrollQ.data)
  const completed = enrollments.filter(e => Number(e.progressPercent) >= 100).length

  return (
    <div className="space-y-6">
      <Button variant="ghost" size="sm" onClick={() => navigate('/courses')}>
        <ArrowLeft className="h-4 w-4 mr-1" /> Back
      </Button>

      <Card>
        <CardContent className="p-6 flex items-start justify-between">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <BookOpen className="h-5 w-5 text-violet-600" />
              <h1 className="text-2xl font-bold">{String(c.title || c.name || '')}</h1>
              <Badge>{String(c.status || 'DRAFT')}</Badge>
            </div>
            <p className="text-sm text-slate-500">{String(c.category || '')} · {String(c.deliveryMode || '')}</p>
            {c.description ? <p className="text-sm mt-3">{String(c.description)}</p> : null}
          </div>
          <div className="flex flex-col gap-2">
            <Button size="sm" onClick={() => navigate(`/courses/${id}/play`)}><Play className="h-4 w-4 mr-1" /> Preview</Button>
            {c.status === 'DRAFT' && <Button size="sm" variant="outline" onClick={() => publish.mutate()} disabled={publish.isPending}>Publish</Button>}
            {c.status === 'PUBLISHED' && <Button size="sm" variant="outline" onClick={() => archive.mutate()} disabled={archive.isPending}>Archive</Button>}
          </div>
        </CardContent>
      </Card>

      <Tabs value={tab} onValueChange={setTab}>
        <TabsList>
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="modules"><BookOpen className="h-3.5 w-3.5" /> Modules ({rows(modulesQ.data).length})</TabsTrigger>
          <TabsTrigger value="assessments"><FileQuestion className="h-3.5 w-3.5" /> Assessments</TabsTrigger>
          <TabsTrigger value="enrollments"><Users className="h-3.5 w-3.5" /> Enrollments</TabsTrigger>
          <TabsTrigger value="analytics"><BarChart3 className="h-3.5 w-3.5" /> Analytics</TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Modules</p><p className="text-2xl font-bold">{rows(modulesQ.data).length}</p></CardContent></Card>
            <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Enrolled</p><p className="text-2xl font-bold">{enrollments.length}</p></CardContent></Card>
            <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Completed</p><p className="text-2xl font-bold text-green-600">{completed}</p></CardContent></Card>
            <Card><CardContent className="p-4"><p className="text-xs text-slate-500">Pass rate</p><p className="text-2xl font-bold">{enrollments.length ? Math.round((completed / enrollments.length) * 100) : 0}%</p></CardContent></Card>
          </div>
        </TabsContent>

        <TabsContent value="modules">
          <CrudSection
            title="Modules" icon={<BookOpen className="h-6 w-6" />}
            items={rows(modulesQ.data)} loading={modulesQ.isLoading}
            emptyText="No modules added" queryKey={['course-mods', id]}
            fields={[
              { name: 'title', label: 'Module title', type: 'text', required: true, span: 2 },
              { name: 'type', label: 'Type', type: 'select', options: [
                { value: 'VIDEO', label: 'Video' }, { value: 'DOCUMENT', label: 'Document' }, { value: 'SCORM', label: 'SCORM' }, { value: 'QUIZ', label: 'Quiz' }, { value: 'LINK', label: 'Link' },
              ] },
              { name: 'duration', label: 'Duration (min)', type: 'number' },
              { name: 'contentUri', label: 'Content URL', type: 'url', span: 2 },
              { name: 'sortOrder', label: 'Order', type: 'number' },
            ]}
            onCreate={v => Catalog.courses.createModule({ ...v, courseId: id })}
            onUpdate={(mid, v) => Catalog.courses.updateModule(mid, v)}
            onDelete={mid => Catalog.courses.deleteModule(mid)}
            renderItem={(m: AnyObj) => (<>
              <p className="text-sm font-medium">{String(m.title || m.name || '')}</p>
              <p className="text-xs text-slate-500">{String(m.type || 'VIDEO')} · {m.duration ? `${m.duration} min` : ''}</p>
            </>)}
          />
        </TabsContent>

        <TabsContent value="assessments">
          <CrudSection
            title="Assessments" icon={<FileQuestion className="h-6 w-6" />}
            items={rows(assessQ.data)} loading={assessQ.isLoading}
            emptyText="No assessments" queryKey={['course-assess', id]}
            fields={[
              { name: 'title', label: 'Assessment title', type: 'text', required: true, span: 2 },
              { name: 'passingScore', label: 'Passing score (%)', type: 'number', defaultValue: 70 },
              { name: 'timeLimitMinutes', label: 'Time limit (min)', type: 'number' },
              { name: 'maxAttempts', label: 'Max attempts', type: 'number', defaultValue: 3 },
              { name: 'description', label: 'Description', type: 'textarea', span: 2 },
            ]}
            onCreate={v => Catalog.courses.createAssessment({ ...v, courseId: id })}
            onUpdate={(aid, v) => Catalog.courses.updateAssessment(aid, v)}
            onDelete={aid => Catalog.courses.deleteAssessment(aid)}
            renderItem={(a: AnyObj) => (<>
              <p className="text-sm font-medium">{String(a.title || '')}</p>
              <p className="text-xs text-slate-500">{String(a.questionCount || 0)} questions · Passing: {String(a.passingScore || 70)}%</p>
            </>)}
          />
        </TabsContent>

        <TabsContent value="enrollments">
          <Card><CardContent>
            {enrollQ.isLoading ? <Skeleton className="h-20" /> : enrollments.length === 0 ? (
              <EmptyState icon={<Users className="h-6 w-6" />} title="No enrollments yet" />
            ) : (
              <table className="w-full text-sm">
                <thead className="text-xs uppercase text-slate-500 border-b"><tr><th className="text-left p-2">Employee</th><th className="text-left p-2">Progress</th><th className="text-left p-2">Status</th><th className="text-left p-2">Enrolled</th></tr></thead>
                <tbody>{enrollments.map((e: AnyObj) => (
                  <tr key={String(e.id)} className="border-b last:border-0">
                    <td className="p-2">{String(e.employeeName || e.employeeId || '')}</td>
                    <td className="p-2"><div className="flex items-center gap-2 w-40"><Progress value={Number(e.progressPercent ?? 0)} /><span className="text-xs">{Number(e.progressPercent ?? 0)}%</span></div></td>
                    <td className="p-2"><Badge>{String(e.status || '')}</Badge></td>
                    <td className="p-2">{e.enrolledAt ? formatDate(String(e.enrolledAt)) : '—'}</td>
                  </tr>
                ))}</tbody>
              </table>
            )}
          </CardContent></Card>
        </TabsContent>

        <TabsContent value="analytics">
          <Card>
            <CardHeader><CardTitle className="text-sm">Course status</CardTitle></CardHeader>
            <CardContent>
              {statusQ.isLoading ? <Skeleton className="h-20" /> : statusQ.data
                ? <pre className="p-3 bg-slate-50 text-xs rounded overflow-auto">{JSON.stringify(statusQ.data, null, 2)}</pre>
                : <EmptyState icon={<BarChart3 className="h-6 w-6" />} title="No analytics yet" />}
              <div className="mt-4 flex items-center gap-3">
                <Award className="h-5 w-5 text-amber-500" />
                <span className="text-sm text-slate-600">Completion grants a digital certificate · valid configurably</span>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
