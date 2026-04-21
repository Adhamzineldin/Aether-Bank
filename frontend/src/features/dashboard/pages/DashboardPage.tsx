import { Link } from 'react-router-dom';
import { ArrowUpRight, ArrowDownRight, Plus, Send, CreditCard, ScrollText, Wallet } from 'lucide-react';
import { ResponsiveContainer, AreaChart, Area, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
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

const chartData = Array.from({ length: 14 }).map((_, i) => ({
  day: `D${i + 1}`,
  inflow: 1500 + Math.round(Math.random() * 800),
  outflow: 800 + Math.round(Math.random() * 700),
}));

export default function DashboardPage() {
  const user = useAuthStore((s) => s.user);
  const accounts = useMyAccounts();

  const total = accounts.data?.reduce((sum, a) => sum + Number(a.balance || 0), 0) ?? 0;
  const currency = accounts.data?.[0]?.account.currency || 'USD';

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

      <div className="grid gap-4 md:grid-cols-4">
        <Stat
          label="Total balance"
          value={<CurrencyDisplay amount={total} currency={currency} />}
          icon={<Wallet className="h-4 w-4" />}
          hint="across all accounts"
        />
        <Stat label="Inflow (14d)" value={<CurrencyDisplay amount={chartData.reduce((s, d) => s + d.inflow, 0)} />} icon={<ArrowDownRight className="h-4 w-4 text-success-600" />} trend="up" hint="+8.2% vs prev" />
        <Stat label="Outflow (14d)" value={<CurrencyDisplay amount={chartData.reduce((s, d) => s + d.outflow, 0)} />} icon={<ArrowUpRight className="h-4 w-4 text-danger-600" />} trend="down" hint="−2.1% vs prev" />
        <Stat label="Active cards" value={1} icon={<CreditCard className="h-4 w-4" />} />
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-2">
          <CardContent>
            <h3 className="mb-3 text-sm font-semibold">Cash flow (last 14 days)</h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData}>
                  <defs>
                    <linearGradient id="cIn" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="rgb(16,185,129)" stopOpacity={0.4} />
                      <stop offset="100%" stopColor="rgb(16,185,129)" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="cOut" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="rgb(239,68,68)" stopOpacity={0.4} />
                      <stop offset="100%" stopColor="rgb(239,68,68)" stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeOpacity={0.2} vertical={false} />
                  <XAxis dataKey="day" tickLine={false} axisLine={false} fontSize={11} />
                  <YAxis tickLine={false} axisLine={false} fontSize={11} />
                  <Tooltip />
                  <Area type="monotone" dataKey="inflow" stroke="rgb(16,185,129)" fill="url(#cIn)" strokeWidth={2} />
                  <Area type="monotone" dataKey="outflow" stroke="rgb(239,68,68)" fill="url(#cOut)" strokeWidth={2} />
                </AreaChart>
              </ResponsiveContainer>
            </div>
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

      <div>
        <h3 className="mb-3 text-sm font-semibold">Your accounts</h3>
        {accounts.isLoading ? (
          <div className="grid gap-4 md:grid-cols-3">{Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-40" />)}</div>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            {accounts.data?.slice(0, 3).map((a) => <AccountCard key={a.account.id} data={a} />)}
            {(!accounts.data || accounts.data.length === 0) && (
              <Link to={ROUTES.newAccount}>
                <Card className="grid h-40 place-items-center border-dashed text-muted-fg hover:border-primary/50 hover:text-primary">
                  <span className="flex items-center gap-2"><Plus className="h-4 w-4" /> Open your first account</span>
                </Card>
              </Link>
            )}
          </div>
        )}
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

