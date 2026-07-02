import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Settings as SettingsIcon, Shield, Plug, Building2 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/ui/page-header'
import { settingsService } from '@/services/settingsService'
import { authService } from '@/services/authService'
import { getErrorMessage } from '@/lib/api'
import { toast } from 'sonner'

export function SettingsPage() {
  const qc = useQueryClient()
  const [pw, setPw] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const changePassword = useMutation({
    mutationFn: () => authService.changePassword({ currentPassword: pw.currentPassword, newPassword: pw.newPassword }),
    onSuccess: () => { toast.success('Password changed'); setPw({ currentPassword: '', newPassword: '', confirm: '' }) },
    onError: (e) => toast.error(getErrorMessage(e)),
  })
  const [mfaCode, setMfaCode] = useState('')
  const [enroll, setEnroll] = useState<{ secret: string; qrCode: string } | null>(null)
  const mfa = useQuery({ queryKey: ['settings', 'mfa'], queryFn: settingsService.myMfa })
  const mfaEnabled = (mfa.data as { enabled?: boolean } | undefined)?.enabled
  const startEnroll = useMutation({
    mutationFn: settingsService.enrollMfa,
    onSuccess: (d) => setEnroll(d as { secret: string; qrCode: string }),
    onError: (e) => toast.error(getErrorMessage(e)),
  })
  const confirmMfa = useMutation({
    mutationFn: () => settingsService.verifyMfa(mfaCode),
    onSuccess: () => { toast.success('Two-factor authentication enabled'); setEnroll(null); setMfaCode(''); qc.invalidateQueries({ queryKey: ['settings', 'mfa'] }) },
    onError: (e) => toast.error(getErrorMessage(e)),
  })
  const disableMfa = useMutation({
    mutationFn: () => settingsService.disableMfa(mfaCode),
    onSuccess: () => { toast.success('Two-factor authentication disabled'); setMfaCode(''); qc.invalidateQueries({ queryKey: ['settings', 'mfa'] }) },
    onError: (e) => toast.error(getErrorMessage(e)),
  })
  const tenant = useQuery({ queryKey: ['settings', 'tenant'], queryFn: settingsService.myTenant })
  const integrations = useQuery({ queryKey: ['settings', 'integrations'], queryFn: settingsService.listIntegrations })
  const flags = useQuery({ queryKey: ['settings', 'flags'], queryFn: settingsService.listFlags })
  const sessions = useQuery({ queryKey: ['settings', 'sessions'], queryFn: settingsService.mySessions })

  const [companyName, setCompanyName] = useState('')
  const updateTenant = useMutation({
    mutationFn: () => settingsService.updateTenant({ name: companyName }),
    onSuccess: () => { toast.success('Saved'); qc.invalidateQueries({ queryKey: ['settings', 'tenant'] }) },
  })
  const toggleFlag = useMutation({
    mutationFn: ({ key, enabled }: { key: string; enabled: boolean }) => settingsService.toggleFlag(key, enabled),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['settings', 'flags'] }),
  })

  return (
    <div className="space-y-6">
      <PageHeader title="Settings" description="Tenant configuration, integrations, security" />

      <Tabs defaultValue="company">
        <TabsList>
          <TabsTrigger value="company"><Building2 className="h-4 w-4 mr-1" /> Company</TabsTrigger>
          <TabsTrigger value="security"><Shield className="h-4 w-4 mr-1" /> Security</TabsTrigger>
          <TabsTrigger value="integrations"><Plug className="h-4 w-4 mr-1" /> Integrations</TabsTrigger>
          <TabsTrigger value="flags"><SettingsIcon className="h-4 w-4 mr-1" /> Feature Flags</TabsTrigger>
        </TabsList>

        <TabsContent value="company" className="space-y-4">
          <Card>
            <CardHeader><CardTitle>Company profile</CardTitle></CardHeader>
            <CardContent className="space-y-4">
              {tenant.isLoading ? <Skeleton className="h-16" /> : (
                <>
                  <div>
                    <Label>Name</Label>
                    <Input
                      defaultValue={tenant.data?.name}
                      onChange={e => setCompanyName(e.target.value)}
                      placeholder="Company name"
                    />
                  </div>
                  <Button onClick={() => updateTenant.mutate()} disabled={!companyName.trim()}>Save</Button>
                </>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="security" className="space-y-4">
          <Card>
            <CardHeader><CardTitle>Change password</CardTitle></CardHeader>
            <CardContent className="space-y-3 max-w-md">
              <div>
                <Label>Current password</Label>
                <Input type="password" value={pw.currentPassword}
                  onChange={e => setPw(p => ({ ...p, currentPassword: e.target.value }))} />
              </div>
              <div>
                <Label>New password</Label>
                <Input type="password" value={pw.newPassword}
                  onChange={e => setPw(p => ({ ...p, newPassword: e.target.value }))} />
              </div>
              <div>
                <Label>Confirm new password</Label>
                <Input type="password" value={pw.confirm}
                  onChange={e => setPw(p => ({ ...p, confirm: e.target.value }))} />
              </div>
              <Button
                onClick={() => changePassword.mutate()}
                loading={changePassword.isPending}
                disabled={!pw.currentPassword || pw.newPassword.length < 8 || pw.newPassword !== pw.confirm}>
                Update password
              </Button>
              {pw.newPassword && pw.confirm && pw.newPassword !== pw.confirm &&
                <p className="text-xs text-red-500">Passwords do not match</p>}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                Two-factor authentication (TOTP)
                {mfa.isLoading ? null : <Badge variant={mfaEnabled ? 'default' : 'outline'}>{mfaEnabled ? 'Enabled' : 'Disabled'}</Badge>}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 max-w-md">
              {mfa.isLoading ? <Skeleton className="h-16" /> : mfaEnabled ? (
                <>
                  <p className="text-sm text-slate-500">Enter a current code from your authenticator app to turn off two-factor authentication.</p>
                  <div>
                    <Label>Authenticator code</Label>
                    <Input inputMode="numeric" placeholder="123456" value={mfaCode}
                      onChange={e => setMfaCode(e.target.value.replace(/\D/g, '').slice(0, 6))} />
                  </div>
                  <Button variant="destructive" onClick={() => disableMfa.mutate()}
                    loading={disableMfa.isPending} disabled={mfaCode.length !== 6}>
                    Disable two-factor
                  </Button>
                </>
              ) : enroll ? (
                <>
                  <p className="text-sm text-slate-500">Scan this QR code with Google Authenticator, Authy or 1Password, then enter the 6-digit code to confirm.</p>
                  {enroll.qrCode && <img src={enroll.qrCode} alt="MFA QR code" className="h-44 w-44 rounded border bg-white p-2" />}
                  <p className="text-xs text-slate-500 break-all">Or enter this secret manually: <span className="font-mono">{enroll.secret}</span></p>
                  <div>
                    <Label>Authenticator code</Label>
                    <Input inputMode="numeric" placeholder="123456" value={mfaCode}
                      onChange={e => setMfaCode(e.target.value.replace(/\D/g, '').slice(0, 6))} />
                  </div>
                  <div className="flex gap-2">
                    <Button onClick={() => confirmMfa.mutate()} loading={confirmMfa.isPending} disabled={mfaCode.length !== 6}>
                      Verify &amp; enable
                    </Button>
                    <Button variant="outline" onClick={() => { setEnroll(null); setMfaCode('') }}>Cancel</Button>
                  </div>
                </>
              ) : (
                <>
                  <p className="text-sm text-slate-500">Add an extra layer of security by requiring a one-time code at sign-in.</p>
                  <Button onClick={() => startEnroll.mutate()} loading={startEnroll.isPending}>Set up two-factor</Button>
                </>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Active sessions</CardTitle></CardHeader>
            <CardContent>
              {sessions.isLoading ? <Skeleton className="h-32" /> : (
                <ul className="space-y-2 text-sm">
                  {((sessions.data as Array<{ id: string; userAgent: string; ip: string; lastSeenAt: string }> | undefined) || []).map(s => (
                    <li key={s.id} className="flex items-center justify-between p-2 rounded border">
                      <div>
                        <p className="font-medium">{s.userAgent}</p>
                        <p className="text-xs text-slate-500">{s.ip} · last seen {s.lastSeenAt}</p>
                      </div>
                      <Button size="sm" variant="outline" onClick={() => settingsService.revokeSession(s.id)}>Revoke</Button>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="integrations" className="space-y-3">
          {integrations.isLoading ? <Skeleton className="h-32" /> : (
            ((integrations.data as Array<{ id: string; provider: string; type: string; status: string }> | undefined) || []).map(i => (
              <Card key={i.id}>
                <CardContent className="p-4 flex items-center justify-between">
                  <div>
                    <p className="font-medium">{i.provider}</p>
                    <p className="text-xs text-slate-500">{i.type}</p>
                  </div>
                  <Badge>{i.status}</Badge>
                </CardContent>
              </Card>
            ))
          )}
        </TabsContent>

        <TabsContent value="flags" className="space-y-3">
          {flags.isLoading ? <Skeleton className="h-32" /> : (
            ((flags.data as Array<{ key: string; description?: string; enabled: boolean; rolloutPercent?: number }> | undefined) || []).map(f => (
              <Card key={f.key}>
                <CardContent className="p-4 flex items-center justify-between">
                  <div>
                    <p className="font-medium">{f.key}</p>
                    {f.description && <p className="text-xs text-slate-500">{f.description}</p>}
                  </div>
                  <div className="flex items-center gap-2">
                    {f.rolloutPercent !== undefined && <Badge>{f.rolloutPercent}%</Badge>}
                    <Button size="sm" variant={f.enabled ? 'default' : 'outline'} onClick={() => toggleFlag.mutate({ key: f.key, enabled: !f.enabled })}>
                      {f.enabled ? 'Enabled' : 'Disabled'}
                    </Button>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
