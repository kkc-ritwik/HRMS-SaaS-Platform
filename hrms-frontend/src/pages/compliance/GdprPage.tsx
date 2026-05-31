import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Shield, Download, Trash2 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { gdprService } from '@/services/extendedServices'
import { toast } from 'sonner'

export function GdprPage() {
  const [empId, setEmpId] = useState('')
  const [reason, setReason] = useState('')

  const exportData = useMutation({
    mutationFn: () => gdprService.exportEmployee(empId),
    onSuccess: d => { toast.success('Export ready'); navigator.clipboard?.writeText(JSON.stringify(d).slice(0, 200)) },
  })
  const erase = useMutation({
    mutationFn: () => gdprService.eraseEmployee(empId, reason),
    onSuccess: () => toast.success('Erasure scheduled (30-day soft delete)'),
  })

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="GDPR / DPDP" description="Right to access, erasure, restrict, and consent management" />

      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><Shield className="h-5 w-5" /> Employee data request</CardTitle></CardHeader>
        <CardContent className="space-y-4">
          <div>
            <Label>Employee ID</Label>
            <Input value={empId} onChange={e => setEmpId(e.target.value)} placeholder="UUID" />
          </div>
          <div>
            <Label>Reason (for erasure)</Label>
            <Input value={reason} onChange={e => setReason(e.target.value)} placeholder="e.g. employee request" />
          </div>
          <div className="flex gap-2">
            <Button onClick={() => exportData.mutate()} disabled={!empId}><Download className="h-4 w-4 mr-1" /> Export data</Button>
            <Button variant="outline" onClick={() => erase.mutate()} disabled={!empId || !reason}>
              <Trash2 className="h-4 w-4 mr-1" /> Right to be forgotten
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
