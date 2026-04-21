import { useParams, Link } from 'react-router-dom';
import { Snowflake, CheckCircle2, Lock, FileText, ArrowLeftRight } from 'lucide-react';
import { Card, CardContent } from '@shared/ui/Card';
import { Button } from '@shared/ui/Button';
import { Badge } from '@shared/ui/Badge';
import { CopyButton } from '@shared/ui/CopyButton';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Skeleton } from '@shared/ui/Skeleton';
import { Stat } from '@shared/ui/Stat';
import { PageHeader } from '@shared/ui/PageHeader';
import { ROUTES } from '@app/routes';
import { formatDate } from '@shared/utils/date';
import { useAccount, useAccountBalance, useUpdateAccountStatus } from '../hooks';

export default function AccountDetailPage() {
  const { accountId = '' } = useParams();
  const { data, isLoading } = useAccount(accountId);
  const balance = useAccountBalance(accountId, data?.account.currency || '');
  const updateStatus = useUpdateAccountStatus(accountId);

  if (isLoading || !data) return <Skeleton className="h-64" />;
  const a = data.account;

  const toggleFreeze = () => {
    const next = a.status === 'FROZEN' ? 'ACTIVE' : 'FROZEN';
    updateStatus.mutate({ status: next, reason: `User toggled to ${next}` } as any);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title={a.accountType + ' Account'}
        description={<><span className="font-mono">{a.accountNumber}</span> <CopyButton value={a.accountNumber} /></>}
        back={{ to: ROUTES.accounts }}
        actions={
          <>
            <Link to={ROUTES.transfer + `?source=${a.id}`}>
              <Button leftIcon={<ArrowLeftRight className="h-4 w-4" />}>Transfer</Button>
            </Link>
            <Button
              variant="outline"
              loading={updateStatus.isPending}
              onClick={toggleFreeze}
              leftIcon={a.status === 'FROZEN' ? <CheckCircle2 className="h-4 w-4" /> : <Snowflake className="h-4 w-4" />}
            >
              {a.status === 'FROZEN' ? 'Unfreeze' : 'Freeze'}
            </Button>
            <Link to={ROUTES.accountStatement(a.id)}>
              <Button variant="outline" leftIcon={<FileText className="h-4 w-4" />}>Statement</Button>
            </Link>
          </>
        }
      />

      <div className="grid gap-4 md:grid-cols-3">
        <Stat label="Available balance" value={<CurrencyDisplay amount={data.balance} currency={a.currency} />} />
        <Stat
          label="Pending holds"
          value={<CurrencyDisplay amount={balance.data?.pendingHolds ?? 0} currency={a.currency} />}
        />
        <Stat label="Status" value={<Badge tone={a.status === 'ACTIVE' ? 'success' : 'neutral'}>{a.status}</Badge>} />
      </div>

      <Card>
        <CardContent className="grid gap-6 md:grid-cols-2">
          <Detail label="Account ID"><code className="text-xs">{a.id}</code></Detail>
          <Detail label="Currency">{a.currency}</Detail>
          <Detail label="Opened">{formatDate(a.openedDate)}</Detail>
          {a.closedDate && <Detail label="Closed">{formatDate(a.closedDate)}</Detail>}
          <Detail label="Status">
            <Badge tone={a.status === 'CLOSED' ? 'neutral' : 'success'}>
              <Lock className="h-3 w-3 mr-1" /> {a.status}
            </Badge>
          </Detail>
          <Detail label="Created">{formatDate(a.createdAt)}</Detail>
        </CardContent>
      </Card>
    </div>
  );
}

function Detail({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div>
      <p className="text-xs uppercase tracking-wider text-muted-fg">{label}</p>
      <p className="mt-1 text-sm">{children}</p>
    </div>
  );
}

