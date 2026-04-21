import { useNavigate } from 'react-router-dom';
import { Bell, Menu, Moon, Search, Sun, LogOut, User as UserIcon } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useUIStore } from '@stores/uiStore';
import { useAuthStore } from '@stores/authStore';
import { useTheme } from '@shared/hooks/useTheme';
import { useDisclosure } from '@shared/hooks/useDisclosure';
import { ROUTES } from '@app/routes';
import { cn } from '@shared/utils/cn';

export function Topbar() {
  const toggleSidebar = useUIStore((s) => s.toggleSidebar);
  const { theme, toggle } = useTheme();
  const user = useAuthStore((s) => s.user);
  const clear = useAuthStore((s) => s.clear);
  const navigate = useNavigate();
  const menu = useDisclosure();

  const initials = user?.userName?.slice(0, 2).toUpperCase() || 'AB';

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center gap-3 border-b border-border bg-bg/80 px-4 backdrop-blur-xl">
      <button onClick={toggleSidebar} className="rounded-md p-2 text-muted-fg hover:bg-muted" aria-label="Toggle sidebar">
        <Menu className="h-5 w-5" />
      </button>
      <div className="relative flex-1 max-w-xl">
        <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-fg" />
        <input
          placeholder="Search accounts, transactions, cards…"
          className="h-10 w-full rounded-lg border border-border bg-card pl-10 pr-3 text-sm outline-none placeholder:text-muted-fg focus:border-primary focus:ring-2 focus:ring-primary/20"
        />
      </div>
      <button onClick={toggle} className="rounded-md p-2 text-muted-fg hover:bg-muted" aria-label="Toggle theme">
        {theme === 'dark' ? <Sun className="h-5 w-5" /> : <Moon className="h-5 w-5" />}
      </button>
      <Link to={ROUTES.notifications} className="rounded-md p-2 text-muted-fg hover:bg-muted relative" aria-label="Notifications">
        <Bell className="h-5 w-5" />
      </Link>
      <div className="relative">
        <button
          onClick={menu.toggle}
          className="flex items-center gap-2 rounded-lg border border-border bg-card pl-2 pr-3 py-1.5 hover:bg-muted"
        >
          <span className="grid h-7 w-7 place-items-center rounded-full bg-primary text-primary-fg text-xs font-semibold">
            {initials}
          </span>
          <span className="hidden text-sm font-medium md:inline">{user?.userName || 'Guest'}</span>
        </button>
        {menu.isOpen && (
          <div
            className={cn(
              'absolute right-0 top-full mt-2 w-56 rounded-lg border border-border bg-card p-1 shadow-soft animate-fade-in',
            )}
            onClick={menu.close}
          >
            <Link to={ROUTES.profile} className="flex items-center gap-2 rounded-md px-3 py-2 text-sm hover:bg-muted">
              <UserIcon className="h-4 w-4" /> Profile
            </Link>
            <button
              onClick={() => { clear(); navigate(ROUTES.login); }}
              className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm text-danger hover:bg-danger/10"
            >
              <LogOut className="h-4 w-4" /> Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  );
}

