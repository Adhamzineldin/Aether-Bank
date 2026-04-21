import { Link } from 'react-router-dom';
import { Plus, Wallet } from 'lucide-react';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { Skeleton } from '@shared/ui/Skeleton';
import { PageHeader } from '@shared/ui/PageHeader';
import { ROUTES } from '@app/routes';
import { useMyAccounts } from '../hooks';
import { AccountCard } from '../components/AccountCard';

export default function AccountsListPage() {
  const { data, isLoading } = useMyAccounts();

  return (
    <div>
      <PageHeader
        title="Accounts"
        description="Manage all of your bank accounts in one place."
        actions={
          <Link to={ROUTES.newAccount}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>Open account</Button>
          </Link>
        }
      />
      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-40" />)}
        </div>
      ) : !data || data.length === 0 ? (
        <EmptyState
          icon={<Wallet className="h-5 w-5" />}
          title="No accounts yet"
          description="Open your first account to start saving and spending."
          action={<Link to={ROUTES.newAccount}><Button>Open account</Button></Link>}
        />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {data.map((a) => <AccountCard key={a.id} data={a} />)}
        </div>
      )}
    </div>
  );
}

