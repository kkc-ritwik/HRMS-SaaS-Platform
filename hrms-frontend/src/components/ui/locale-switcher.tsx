import { Globe } from 'lucide-react'
import { useLocaleStore, LOCALES } from '@/store/localeStore'
import { Button } from './button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from './dropdown-menu'

export function LocaleSwitcher() {
  const { locale, setLocale } = useLocaleStore()
  const current = LOCALES.find(l => l.code === locale)
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="sm" className="text-slate-500 hover:text-slate-700 gap-1.5">
          <Globe className="h-4 w-4" /> {current?.code.toUpperCase()}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-44 max-h-96 overflow-y-auto">
        {LOCALES.map(l => (
          <DropdownMenuItem key={l.code} onClick={() => setLocale(l.code)}>
            <span className="w-8 text-xs text-slate-400">{l.code.toUpperCase()}</span>
            <span>{l.label}</span>
            {l.code === locale && <span className="ml-auto text-xs text-violet-600">✓</span>}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
