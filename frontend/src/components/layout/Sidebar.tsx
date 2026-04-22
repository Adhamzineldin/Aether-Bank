import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Wallet, ArrowLeftRight, CreditCard, Banknote, Home as HomeIcon,
  PiggyBank, TrendingUp, Globe2, Users, Bell, ShieldCheck, ScrollText, ListChecks, Sparkles, GitBranch,
} from 'lucide-react';
import { cn } from '@shared/utils/cn';
import { useUIStore } from '@stores/uiStore';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';

interface Item { to: string; label: string; icon: React.ReactNode; roles?: string[] }

const items: Item[] = [
  { to: ROUTES.dashboard, label: 'Dashboard', icon: <LayoutDashboard className="h-4 w-4" /> },
  { to: ROUTES.accounts, label: 'Accounts', icon: <Wallet className="h-4 w-4" /> },
  { to: ROUTES.transactions, label: 'Transactions', icon: <ArrowLeftRight className="h-4 w-4" /> },
  { to: ROUTES.cards, label: 'Cards', icon: <CreditCard className="h-4 w-4" /> },
  { to: ROUTES.payments, label: 'Payments', icon: <Banknote className="h-4 w-4" /> },
  { to: ROUTES.loans, label: 'Loans', icon: <ScrollText className="h-4 w-4" /> },
  { to: ROUTES.mortgages, label: 'Mortgages', icon: <HomeIcon className="h-4 w-4" /> },
  { to: ROUTES.savings, label: 'Savings', icon: <PiggyBank className="h-4 w-4" /> },
  { to: ROUTES.investments, label: 'Investments', icon: <TrendingUp className="h-4 w-4" /> },
  { to: ROUTES.fx, label: 'FX Exchange', icon: <Globe2 className="h-4 w-4" /> },
  { to: ROUTES.beneficiaries, label: 'Beneficiaries', icon: <Users className="h-4 w-4" /> },
  { to: ROUTES.notifications, label: 'Notifications', icon: <Bell className="h-4 w-4" /> },
];

/**
 * Roles that can open the approval inbox. `RISK`, `MANAGER` and `DIRECTOR` are
 * assignment-only workflow roles — any user holding one is a potential approver
 * on some workflow step and must be able to see and action their inbox, even if
 * they are not an `EMPLOYEE`.
 */
const WORKFLOW_ROLES = ['EMPLOYEE', 'ADMIN', 'SUPERADMIN', 'RISK', 'MANAGER', 'DIRECTOR'] as const;

const operationsItems: Item[] = [
  { to: ROUTES.workflow, label: 'Workflow', icon: <ListChecks className="h-4 w-4" />, roles: [...WORKFLOW_ROLES] },
  { to: ROUTES.audit, label: 'Audit Logs', icon: <ShieldCheck className="h-4 w-4" />, roles: ['EMPLOYEE', 'ADMIN', 'SUPERADMIN'] },
];

const adminItems: Item[] = [
  { to: ROUTES.adminUsers, label: 'Users', icon: <Users className="h-4 w-4" />, roles: ['ADMIN', 'SUPERADMIN'] },
  { to: ROUTES.templates, label: 'Templates', icon: <Bell className="h-4 w-4" />, roles: ['ADMIN', 'SUPERADMIN'] },
  { to: ROUTES.workflowTemplates, label: 'Workflow rules', icon: <GitBranch className="h-4 w-4" />, roles: ['ADMIN', 'SUPERADMIN'] },
];

export function Sidebar() {
  const collapsed = useUIStore((s) => s.sidebarCollapsed);
  const hasRole = useAuthStore((s) => s.hasRole);

  const visibleOperationsItems = operationsItems.filter((it) => !it.roles || hasRole(...it.roles));

  return (
    <aside
      className={cn(
        'sticky top-0 z-30 flex h-screen flex-col border-r border-border bg-card/60 backdrop-blur-xl transition-all',
        collapsed ? 'w-[68px]' : 'w-64',
      )}
    >
      <div className="flex h-16 items-center gap-2 px-4 border-b border-border">
        <span className="grid h-9 w-9 place-items-center rounded-xl gradient-primary text-white shadow-soft">
          <Sparkles className="h-4 w-4" />
        </span>
        {!collapsed && <span className="text-lg font-bold tracking-tight">Aether</span>}
      </div>
      <nav className="flex-1 overflow-y-auto p-3 space-y-1">
        {items.map((it) => (
          <SidebarItem key={it.to} item={it} collapsed={collapsed} />
        ))}
        {visibleOperationsItems.length > 0 && (
          <>
            <Section collapsed={collapsed} label="Operations" />
            {visibleOperationsItems.map((it) => <SidebarItem key={it.to} item={it} collapsed={collapsed} />)}
          </>
        )}
        {hasRole('ADMIN', 'SUPERADMIN') && (
          <>
            <Section collapsed={collapsed} label="Administration" />
            {adminItems.map((it) => <SidebarItem key={it.to} item={it} collapsed={collapsed} />)}
          </>
        )}
      </nav>
    </aside>
  );
}

function Section({ collapsed, label }: { collapsed: boolean; label: string }) {
  return (
    <div className={cn('mt-4 px-2 pb-1 text-xs font-semibold uppercase tracking-wider text-muted-fg', collapsed && 'sr-only')}>
      {label}
    </div>
  );
}

function SidebarItem({ item, collapsed }: { item: Item; collapsed: boolean }) {
  return (
    <NavLink
      to={item.to}
      className={({ isActive }) =>
        cn(
          'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
          isActive ? 'bg-primary/10 text-primary' : 'text-muted-fg hover:bg-muted hover:text-fg',
          collapsed && 'justify-center',
        )
      }
      end
    >
      {item.icon}
      {!collapsed && <span className="truncate">{item.label}</span>}
    </NavLink>
  );
}

