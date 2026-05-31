import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useRef } from 'react'
import { FileText, Upload, Download } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { documentService, type Document } from '@/services/documentService'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

export function DocumentsPage() {
  const qc = useQueryClient()
  const fileRef = useRef<HTMLInputElement>(null)
  const { data, isLoading } = useQuery({ queryKey: ['documents'], queryFn: () => documentService.list() })
  const docs: Document[] = (data as { content?: Document[] } | undefined)?.content
    || (Array.isArray(data) ? data : [])

  const upload = useMutation({
    mutationFn: (file: File) => documentService.upload(file, { name: file.name }),
    onSuccess: () => { toast.success('Uploaded'); qc.invalidateQueries({ queryKey: ['documents'] }) },
  })

  return (
    <div className="space-y-6">
      <PageHeader
        title="Documents"
        description="Your personal and shared documents"
        action={
          <>
            <input ref={fileRef} type="file" hidden onChange={e => {
              const f = e.target.files?.[0]; if (f) upload.mutate(f)
            }} />
            <Button onClick={() => fileRef.current?.click()}><Upload className="h-4 w-4 mr-1" /> Upload</Button>
          </>
        }
      />

      {isLoading ? (
        <Skeleton className="h-64" />
      ) : docs.length === 0 ? (
        <EmptyState icon={<FileText className="h-10 w-10" />} title="No documents yet" />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
          {docs.map(d => (
            <Card key={d.id}>
              <CardContent className="p-3 flex items-start gap-3">
                <FileText className="h-8 w-8 text-violet-500 flex-shrink-0" />
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{d.name}</p>
                  <p className="text-xs text-slate-500">{d.category || '—'} · {formatDate(d.uploadedAt, 'PP')}</p>
                </div>
                <Button variant="ghost" size="sm" onClick={async () => {
                  const blob = await documentService.download(d.id)
                  const url = URL.createObjectURL(blob)
                  const a = window.document.createElement('a')
                  a.href = url; a.download = d.name; a.click()
                  URL.revokeObjectURL(url)
                }}>
                  <Download className="h-4 w-4" />
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
