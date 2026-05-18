import React, { useState } from 'react'
import { Bell, Search, Settings, LogOut, User, ChevronDown, HelpCircle } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'
import { Avatar } from '@/components/ui/avatar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

interface HeaderProps {
  sidebarCollapsed: boolean
}

const mockNotifications = [
  { id: 1, title: 'Leave request approved', time: '2 min ago', read: false, type: 'success' },
  { id: 2, title: 'New job application received', time: '1 hr ago', read: false, type: 'info' },
  { id: 3, title: 'Performance review due', time: '2 hrs ago', read: true, type: 'warning' },
  { id: 4, title: 'Payroll processed for March', time: '1 day ago', read: true, type: 'success' },
]

export function Header({ sidebarCollapsed }: HeaderProps) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [showSearch, setShowSearch] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [notifOpen, setNotifOpen] = useState(false)

  const unreadCount = mockNotifications.filter(n => !n.read).length

  return (
    <header
      className={cn(
        'h-16 bg-white border-b border-slate-100 flex items-center justify-between px-6',
        'fixed top-0 right-0 z-30 transition-all duration-300',
        'shadow-[0_1px_3px_rgba(0,0,0,0.06)]',
      )}
      style={{
        left: sidebarCollapsed ? 68 : 260,
      }}
    >
      {/* Left: Breadcrumb / Page title area */}
      <div className="flex items-center gap-4">
        {showSearch ? (
          <div className="relative flex items-center">
            <Search className="absolute left-3 h-4 w-4 text-slate-400 pointer-events-none" />
            <input
              autoFocus
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              onBlur={() => { setShowSearch(false); setSearchQuery('') }}
              placeholder="Search employees, leaves, reports..."
              className="h-9 w-72 rounded-lg border border-slate-200 bg-slate-50 pl-9 pr-4 text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent focus:bg-white transition-all"
            />
          </div>
        ) : (
          <button
            onClick={() => setShowSearch(true)}
            className="flex items-center gap-2 h-9 px-3 rounded-lg border border-slate-200 bg-slate-50 text-slate-400 text-sm hover:bg-slate-100 transition-colors"
          >
            <Search className="h-4 w-4" />
            <span className="hidden sm:inline">Search...</span>
            <span className="hidden sm:flex items-center gap-0.5 ml-2 text-xs text-slate-300">
              <kbd className="rounded bg-slate-200 px-1 text-xs text-slate-400">⌘</kbd>
              <kbd className="rounded bg-slate-200 px-1 text-xs text-slate-400">K</kbd>
            </span>
          </button>
        )}
      </div>

      {/* Right: Actions */}
      <div className="flex items-center gap-2">
        {/* Help */}
        <Button variant="ghost" size="icon" className="text-slate-500 hover:text-slate-700">
          <HelpCircle className="h-5 w-5" />
        </Button>

        {/* Notifications */}
        <DropdownMenu open={notifOpen} onOpenChange={setNotifOpen}>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" className="relative text-slate-500 hover:text-slate-700">
              <Bell className="h-5 w-5" />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[9px] font-bold text-white">
                  {unreadCount}
                </span>
              )}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-80">
            <div className="flex items-center justify-between px-3 py-2 border-b border-slate-100">
              <span className="text-sm font-semibold text-slate-800">Notifications</span>
              {unreadCount > 0 && (
                <Badge variant="info" className="text-[10px]">{unreadCount} new</Badge>
              )}
            </div>
            <div className="max-h-80 overflow-y-auto">
              {mockNotifications.map(n => (
                <div
                  key={n.id}
                  className={cn(
                    'flex items-start gap-3 px-3 py-3 hover:bg-slate-50 cursor-pointer border-b border-slate-50 last:border-0 transition-colors',
                    !n.read && 'bg-brand-50/50'
                  )}
                >
                  <div className={cn(
                    'mt-0.5 h-2 w-2 rounded-full flex-shrink-0',
                    n.type === 'success' && 'bg-green-400',
                    n.type === 'info' && 'bg-blue-400',
                    n.type === 'warning' && 'bg-amber-400',
                  )} />
                  <div className="flex-1 min-w-0">
                    <p className={cn('text-sm leading-snug', n.read ? 'text-slate-600' : 'text-slate-800 font-medium')}>
                      {n.title}
                    </p>
                    <p className="text-xs text-slate-400 mt-0.5">{n.time}</p>
                  </div>
                </div>
              ))}
            </div>
            <div className="px-3 py-2 border-t border-slate-100">
              <button
                onClick={() => { navigate('/notifications'); setNotifOpen(false) }}
                className="text-xs text-brand-600 font-medium hover:text-brand-700 w-full text-center"
              >
                View all notifications
              </button>
            </div>
          </DropdownMenuContent>
        </DropdownMenu>

        {/* User menu */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="flex items-center gap-2.5 rounded-lg px-2 py-1.5 hover:bg-slate-100 transition-colors">
              <Avatar name={user?.fullName} size="sm" />
              <div className="hidden sm:block text-left">
                <p className="text-sm font-medium text-slate-800 leading-none">{user?.fullName || 'User'}</p>
                <p className="text-xs text-slate-400 mt-0.5 leading-none">
                  {user?.roles?.[0]?.replace(/_/g, ' ') || 'Employee'}
                </p>
              </div>
              <ChevronDown className="h-3.5 w-3.5 text-slate-400 hidden sm:block" />
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel>
              <div>
                <p className="text-sm font-semibold">{user?.fullName}</p>
                <p className="text-xs text-slate-500 font-normal mt-0.5">{user?.email}</p>
              </div>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={() => navigate('/profile')}>
              <User className="h-4 w-4" />
              My Profile
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => navigate('/settings')}>
              <Settings className="h-4 w-4" />
              Settings
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={logout} destructive>
              <LogOut className="h-4 w-4" />
              Sign out
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  )
}
