import { Link } from 'react-router-dom';
import { Wallet, Snowflake, CheckCircle2, Lock } from 'lucide-react';
import { Card } from '@shared/ui/Card';
import { Badge, type BadgeTone } from '@shared/ui/Badge';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { maskAccount } from '@shared/utils/mask';
import { ROUTES } from '@app/routes';
import type { AccountResponse, AccountStatus } from '@veld/types';

const statusTone: Record<AccountStatus, BadgeTone> = {
  ACTIVE: 'success', PENDING: 'warning', FROZEN: 'info', CLOSED: 'neutral',
};
const statusIcon: Record<AccountStatus, React.ReactNode> = {
  ACTIVE: <CheckCircle2 className="h-3 w-3" />,
  PENDING: <Wallet className="h-3 w-3" />,
  FROZEN: <Snowflake className="h-3 w-3" />,
  CLOSED: <Lock className="h-3 w-3" />,
};

export function AccountCard({ data }: { data: AccountResponse }) {
  const a = data.account;
  return (
    <Link to={ROUTES.account(a.id)} className="block group">
      <Card className="p-5 transition-all group-hover:border-primary/50 group-hover:shadow-soft">
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-xs uppercase tracking-wider text-muted-fg">{a.accountType}</p>
            <p className="mt-1 font-mono text-sm">{maskAccount(a.accountNumber)}</p>
          </div>
          <Badge tone={statusTone[a.status]} className="gap-1">
            {statusIcon[a.status]}
            {a.status}
          </Badge>
        </div>
        <div className="mt-6">
          <p className="text-xs text-muted-fg">Available balance</p>
          <p className="mt-0.5 text-2xl font-bold">
            <CurrencyDisplay amount={data.balance} currency={a.currency} />
          </p>
        </div>
      </Card>
    </Link>
  );
}

