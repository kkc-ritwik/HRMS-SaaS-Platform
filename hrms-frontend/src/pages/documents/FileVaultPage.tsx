import { useRef, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { FolderPlus, Upload, Folder, File as FileIcon, Share2, Download, ChevronRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { EmptyState } from '@/components/ui/empty-state'
import { PageHeader } from '@/components/ui/page-header'
import { FormDialog } from '@/components/ui/form-dialog'
import { fileVaultCatalog } from '@/services/catalog'
import { toast } from 'sonner'
import { formatDate } from '@/lib/utils'

type AnyObj = Record<string, unknown>
function rows<T = AnyObj>(d: unknown): T[] {
  if (Array.isArray(d)) return d as T[]
  const o = d as { content?: T[] } | undefined
  return Array.isArray(o?.content) ? o!.content : []
}

export function FileVaultPage() {
  const qc = useQueryClient()
  const fileRef = useRef<HTMLInputElement>(null)
  const [folder, setFolder] = useState<{ id?: string; name: string }[]>([{ name: 'Home' }])
  const [newFolder, setNewFolder] = useState(false)
  const [shareFor, setShareFor] = useState<AnyObj | null>(null)
  const current = folder[folder.length - 1]

  const filesQ = useQuery({ queryKey: ['vault-mine'], queryFn: () => fileVaultCatalog.mine() })
  const foldersQ = useQuery({ queryKey: ['vault-folders', current.id], queryFn: () => fileVaultCatalog.folders(current.id) })

  const invalidate = () => { qc.invalidateQueries({ queryKey: ['vault-mine'] }); qc.invalidateQueries({ queryKey: ['vault-folders'] }) }
  const upload = useMutation({ mutationFn: (f: File) => fileVaultCatalog.upload(f, { folderId: current.id }), onSuccess: () => { toast.success('Uploaded'); invalidate() } })
  const mkFolder = useMutation({ mutationFn: (v: Record<string, unknown>) => fileVaultCatalog.createFolder({ ...v, parentId: current.id }), onSuccess: () => { toast.success('Folder created'); invalidate(); setNewFolder(false) } })
  const share = useMutation({ mutationFn: (v: Record<string, unknown>) => fileVaultCatalog.share(String(shareFor?.id), v), onSuccess: () => { toast.success('Shared'); setShareFor(null) } })

  const download = async (id: string, name: string) => {
    try {
      const blob = await fileVaultCatalog.download(id)
      const url = URL.createObjectURL(blob as unknown as Blob)
      const a = document.createElement('a'); a.href = url; a.download = name; a.click(); URL.revokeObjectURL(url)
    } catch { toast.error('Download failed') }
  }

  const folders = rows<AnyObj>(foldersQ.data)
  const files = rows<AnyObj>(filesQ.data)
  const loading = filesQ.isLoading || foldersQ.isLoading

  return (
    <div className="space-y-5">
      <PageHeader title="File Vault" description="Secure personal & shared file storage"
        action={<div className="flex gap-2">
          <Button variant="outline" size="sm" onClick={() => setNewFolder(true)}><FolderPlus className="h-4 w-4 mr-1" /> New Folder</Button>
          <input ref={fileRef} type="file" hidden onChange={e => { const f = e.target.files?.[0]; if (f) upload.mutate(f) }} />
          <Button size="sm" onClick={() => fileRef.current?.click()}><Upload className="h-4 w-4 mr-1" /> Upload</Button>
        </div>} />

      <div className="flex items-center gap-1 text-sm text-slate-500">
        {folder.map((f, i) => (
          <span key={i} className="flex items-center gap-1">
            {i > 0 && <ChevronRight className="h-3.5 w-3.5" />}
            <button className="hover:text-brand-600" onClick={() => setFolder(folder.slice(0, i + 1))}>{f.name}</button>
          </span>
        ))}
      </div>

      {loading ? <Skeleton className="h-64" /> : folders.length === 0 && files.length === 0 ? (
        <EmptyState icon={<Folder className="h-10 w-10" />} title="This folder is empty" />
      ) : (
        <Card><CardContent className="p-0 divide-y divide-slate-100">
          {folders.map(f => (
            <button key={String(f.id)} className="w-full flex items-center gap-3 p-3 hover:bg-slate-50 text-left" onClick={() => setFolder([...folder, { id: String(f.id), name: String(f.name ?? 'Folder') }])}>
              <Folder className="h-5 w-5 text-amber-500" />
              <span className="text-sm font-medium flex-1">{String(f.name ?? '')}</span>
              <ChevronRight className="h-4 w-4 text-slate-400" />
            </button>
          ))}
          {files.map(f => (
            <div key={String(f.id)} className="flex items-center gap-3 p-3 hover:bg-slate-50">
              <FileIcon className="h-5 w-5 text-brand-500" />
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{String(f.name ?? f.fileName ?? '')}</p>
                <p className="text-xs text-slate-400">{f.sizeBytes ? `${Math.round(Number(f.sizeBytes) / 1024)} KB` : ''} {f.uploadedAt ? `· ${formatDate(String(f.uploadedAt))}` : ''}</p>
              </div>
              <Button variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => setShareFor(f)}><Share2 className="h-4 w-4" /></Button>
              <Button variant="ghost" size="sm" className="h-7 w-7 p-0" onClick={() => download(String(f.id), String(f.name ?? 'file'))}><Download className="h-4 w-4" /></Button>
            </div>
          ))}
        </CardContent></Card>
      )}

      <FormDialog open={newFolder} onOpenChange={setNewFolder} title="New folder" submitLabel="Create"
        fields={[{ name: 'name', label: 'Folder name', type: 'text', required: true, span: 2 }]}
        onSubmit={v => mkFolder.mutateAsync(v)} />
      <FormDialog open={!!shareFor} onOpenChange={o => !o && setShareFor(null)} title={`Share "${String(shareFor?.name ?? '')}"`} submitLabel="Share"
        fields={[
          { name: 'sharedWithEmployeeId', label: 'Share with employee ID', type: 'text', required: true, span: 2 },
          { name: 'permission', label: 'Permission', type: 'select', options: [
            { value: 'VIEW', label: 'View' }, { value: 'EDIT', label: 'Edit' },
          ] },
          { name: 'expiresAt', label: 'Expires at', type: 'date' },
        ]}
        onSubmit={v => share.mutateAsync(v)} />
    </div>
  )
}
