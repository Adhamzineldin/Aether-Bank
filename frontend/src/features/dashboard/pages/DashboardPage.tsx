import { Link } from 'react-router-dom';
import { Plus, Send, CreditCard, ScrollText, Wallet } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Stat } from '@shared/ui/Stat';
import { Card, CardContent } from '@shared/ui/Card';
import { Button } from '@shared/ui/Button';
import { Skeleton } from '@shared/ui/Skeleton';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { ROUTES } from '@app/routes';
import { useAuthStore } from '@stores/authStore';
import { useMyAccounts } from '@features/accounts/hooks';
import { AccountCard } from '@features/accounts/components/AccountCard';

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);
  const accounts = useMyAccounts();

  const list = accounts.data ?? [];
  const total = list.reduce((sum, a) => sum + Number(a.balance || 0), 0);
  const currency = list[0]?.currency || 'USD';
  const activeCount = list.filter((a) => a.status === 'ACTIVE').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title={`Welcome${user?.userName ? ', ' + user.userName : ''}`}
        description="Here's a snapshot of your finances today."
        actions={
          <>
            <Link to={ROUTES.transfer}><Button leftIcon={<Send className="h-4 w-4" />}>Transfer</Button></Link>
            <Link to={ROUTES.newAccount}><Button variant="outline" leftIcon={<Plus className="h-4 w-4" />}>Open account</Button></Link>
          </>
        }
      />

      <div className="grid gap-4 md:grid-cols-3">
        <Stat
          label="Total balance"
          value={<CurrencyDisplay amount={total} currency={currency} />}
          icon={<Wallet className="h-4 w-4" />}
          hint={`across ${list.length} account${list.length === 1 ? '' : 's'}`}
        />
        <Stat
          label="Active accounts"
          value={activeCount}
          icon={<Wallet className="h-4 w-4" />}
        />
        <Stat
          label="Primary currency"
          value={currency}
          icon={<CreditCard className="h-4 w-4" />}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardContent>
            <h3 className="mb-3 text-sm font-semibold">Your accounts</h3>
            {accounts.isLoading ? (
              <div className="grid gap-4 md:grid-cols-2">
                {Array.from({ length: 2 }).map((_, i) => <Skeleton key={i} className="h-40" />)}
              </div>
            ) : list.length === 0 ? (
              <Link to={ROUTES.newAccount}>
                <Card className="grid h-40 place-items-center border-dashed text-muted-fg hover:border-primary/50 hover:text-primary">
                  <span className="flex items-center gap-2"><Plus className="h-4 w-4" /> Open your first account</span>
                </Card>
              </Link>
            ) : (
              <div className="grid gap-4 md:grid-cols-2">
                {list.slice(0, 4).map((a) => <AccountCard key={a.id} data={a} />)}
              </div>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardContent>
            <h3 className="mb-3 text-sm font-semibold">Quick actions</h3>
            <div className="grid grid-cols-2 gap-2">
              <QuickAction to={ROUTES.transfer} icon={<Send className="h-4 w-4" />} label="Transfer" />
              <QuickAction to={ROUTES.payments} icon={<CreditCard className="h-4 w-4" />} label="Pay" />
              <QuickAction to={ROUTES.applyLoan} icon={<ScrollText className="h-4 w-4" />} label="Loan" />
              <QuickAction to={ROUTES.newAccount} icon={<Plus className="h-4 w-4" />} label="Open" />
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function QuickAction({ to, icon, label }: { to: string; icon: React.ReactNode; label: string }) {
  return (
    <Link to={to} className="flex flex-col items-center gap-2 rounded-lg border border-border p-3 hover:border-primary/50 hover:bg-primary/5">
      <span className="text-primary">{icon}</span>
      <span className="text-xs font-medium">{label}</span>
    </Link>
  );
}
