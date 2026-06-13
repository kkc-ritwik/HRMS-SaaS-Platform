import { useQuery } from '@tanstack/react-query'
import { Download } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { PageHeader } from '@/components/ui/page-header'
import { Catalog } from '@/services/catalog'
import { toast } from 'sonner'

async function downloadPayslipPdf(id: string, period: string) {
  try {
    const blob = await Catalog.payslips.myPdf(id)
    const url = URL.createObjectURL(blob as unknown as Blob)
    const a = document.createElement('a')
    a.href = url; a.download = `payslip-${period}.pdf`; a.click()
    URL.revokeObjectURL(url)
  } catch { toast.error('Could not download payslip') }
}

interface Payslip {
  id: string
  payPeriod: string
  grossSalary: number
  netSalary: number
  status: string
  generatedAt: string
  downloadUrl?: string
}

export function MyPayslipsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['payslips', 'me'],
    queryFn: () => Catalog.payslips.mine(),
  })
  const items: Payslip[] = ((data as { data?: Payslip[]; content?: Payslip[] } | undefined)?.data
    || (data as { content?: Payslip[] } | undefined)?.content
    || (Array.isArray(data) ? (data as Payslip[]) : [])) as Payslip[]

  return (
    <div className="space-y-6">
      <PageHeader title="My Payslips" description="Download your monthly payslips" />
      <Card>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-4 space-y-2">{Array.from({ length: 5 }).map((_, i) => <Skeleton key={i} className="h-14" />)}</div>
          ) : items.length === 0 ? (
            <p className="p-6 text-sm text-slate-500">No payslips yet.</p>
          ) : (
            <table className="w-full">
              <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                <tr>
                  <th className="p-3 text-left">Period</th>
                  <th className="p-3 text-right">Gross</th>
                  <th className="p-3 text-right">Net</th>
                  <th className="p-3 text-left">Status</th>
                  <th className="p-3"></th>
                </tr>
              </thead>
              <tbody className="text-sm">
                {items.map(p => (
                  <tr key={p.id} className="border-t border-slate-100">
                    <td className="p-3 font-medium">{p.payPeriod}</td>
                    <td className="p-3 text-right">₹{p.grossSalary?.toLocaleString()}</td>
                    <td className="p-3 text-right">₹{p.netSalary?.toLocaleString()}</td>
                    <td className="p-3"><Badge>{p.status}</Badge></td>
                    <td className="p-3 text-right">
                      <Button variant="ghost" size="sm" onClick={() => downloadPayslipPdf(p.id, p.payPeriod)}>
                        <Download className="h-4 w-4" />
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
