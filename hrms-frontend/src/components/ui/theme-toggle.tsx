import { Sun, Moon, Monitor } from 'lucide-react'
import { useThemeStore } from '@/store/themeStore'
import { Button } from './button'
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from './dropdown-menu'

export function ThemeToggle() {
  const { theme, resolved, setTheme } = useThemeStore()
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="ghost" size="icon" className="text-slate-500 hover:text-slate-700" title={`Theme: ${theme}`}>
          {resolved === 'dark' ? <Moon className="h-5 w-5" /> : <Sun className="h-5 w-5" />}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end" className="w-36">
        <DropdownMenuItem onClick={() => setTheme('light')}><Sun className="h-4 w-4 mr-2" />Light</DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme('dark')}><Moon className="h-4 w-4 mr-2" />Dark</DropdownMenuItem>
        <DropdownMenuItem onClick={() => setTheme('system')}><Monitor className="h-4 w-4 mr-2" />System</DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}
