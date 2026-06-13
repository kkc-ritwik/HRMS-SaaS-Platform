import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Shield, Download, Trash2, Lock, FileCheck } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { gdprService } from '@/services/extendedServices'
import { toast } from 'sonner'

/**
 * Self-service DSAR (Data Subject Access Request) — backend GdprController is
 * scoped to the current authenticated user (/api/v1/me/{data,erase,restrict,consent}).
 */
export function GdprPage() {
  const [reason, setReason] = useState('')
  const [consentType, setConsentType] = useState('')

  const exportData = useMutation({
    mutationFn: () => gdprService.myData(),
    onSuccess: (d) => {
      const blob = new Blob([JSON.stringify(d, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a'); a.href = url; a.download = 'my-data-export.json'; a.click()
      URL.revokeObjectURL(url)
      toast.success('Your data export has been downloaded')
    },
    onError: () => toast.error('Export failed'),
  })
  const erase = useMutation({
    mutationFn: () => gdprService.eraseEmployee(reason),
    onSuccess: () => toast.success('Erasure request submitted (30-day soft delete)'),
    onError: () => toast.error('Request failed'),
  })
  const restrict = useMutation({
    mutationFn: () => gdprService.restrict({ reason }),
    onSuccess: () => toast.success('Processing restriction applied'),
    onError: () => toast.error('Request failed'),
  })
  const consent = useMutation({
    mutationFn: () => gdprService.recordConsent({ consentType, granted: true }),
    onSuccess: () => { toast.success('Consent recorded'); setConsentType('') },
    onError: () => toast.error('Failed to record consent'),
  })

  return (
    <div className="space-y-6 max-w-2xl">
      <PageHeader title="My Privacy & Data (GDPR / DPDP)" description="Exercise your right to access, erasure, restriction, and consent" />

      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><Download className="h-5 w-5" /> Right to access</CardTitle></CardHeader>
        <CardContent>
          <p className="text-sm text-slate-500 mb-3">Download a machine-readable copy of all personal data we hold about you.</p>
          <Button onClick={() => exportData.mutate()} disabled={exportData.isPending}><Download className="h-4 w-4 mr-1" /> Export my data</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><FileCheck className="h-5 w-5" /> Consent</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          <div>
            <Label>Consent type</Label>
            <Input value={consentType} onChange={e => setConsentType(e.target.value)} placeholder="e.g. MARKETING_EMAILS, DATA_PROCESSING" />
          </div>
          <Button onClick={() => consent.mutate()} disabled={!consentType || consent.isPending}>Grant consent</Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="flex items-center gap-2"><Lock className="h-5 w-5" /> Restriction & erasure</CardTitle></CardHeader>
        <CardContent className="space-y-3">
          <div>
            <Label>Reason</Label>
            <Input value={reason} onChange={e => setReason(e.target.value)} placeholder="e.g. no longer employed" />
          </div>
          <div className="flex gap-2">
            <Button variant="outline" onClick={() => restrict.mutate()} disabled={!reason || restrict.isPending}><Lock className="h-4 w-4 mr-1" /> Restrict processing</Button>
            <Button variant="outline" onClick={() => erase.mutate()} disabled={!reason || erase.isPending}><Trash2 className="h-4 w-4 mr-1" /> Right to be forgotten</Button>
          </div>
        </CardContent>
      </Card>

      <div className="flex items-center gap-2 text-xs text-slate-500 bg-slate-50 border rounded p-3">
        <Shield className="h-4 w-4" /> Requests are logged and processed per your tenant's data-retention policy.
      </div>
    </div>
  )
}
