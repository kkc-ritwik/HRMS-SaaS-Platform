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
import { toast } from 'sonner'

export function SettingsPage() {
  const qc = useQueryClient()
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
