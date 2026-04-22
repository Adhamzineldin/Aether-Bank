import { useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeftRight, Plus } from 'lucide-react';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Select } from '@shared/ui/Select';
import { Skeleton } from '@shared/ui/Skeleton';
import { Pagination } from '@shared/ui/Pagination';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Badge } from '@shared/ui/Badge';
import { Button } from '@shared/ui/Button';
import { EmptyState } from '@shared/ui/EmptyState';
import { ROUTES } from '@app/routes';
import { formatDateTime } from '@shared/utils/date';
import { useMyAccounts } from '@features/accounts/hooks';
import { useAccountTransactions } from '../hooks';

export default function TransactionsListPage() {
  const accounts = useMyAccounts();
  const [accountId, setAccountId] = useState<string>('');
  const [page, setPage] = useState(0);
  const selected = accounts.data?.find((a) => a.id === accountId) || accounts.data?.[0];
  const id = selected?.id || '';
  const tx = useAccountTransactions(id, selected?.currency || 'USD', page);

  return (
    <div>
      <PageHeader
        title="Transactions"
        description="History of all activity across your accounts."
        actions={
          <Link to={ROUTES.transfer}>
            <Button leftIcon={<Plus className="h-4 w-4" />}>New transfer</Button>
          </Link>
        }
      />
      <Card>
        <CardContent className="space-y-4">
          <div className="flex items-center gap-3">
            <span className="text-sm font-medium">Account:</span>
            <Select
              className="max-w-xs"
              value={accountId || selected?.id}
              onChange={(e) => { setAccountId(e.target.value); setPage(0); }}
            >
              {accounts.data?.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.accountType} · {a.accountNumber}
                </option>
              ))}
            </Select>
          </div>
          {tx.isLoading ? (
            <div className="space-y-2">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
          ) : !tx.data?.content?.length ? (
            <EmptyState
              icon={<ArrowLeftRight className="h-5 w-5" />}
              title="No transactions yet"
              description="Once you send or receive money it'll show up here."
              action={<Link to={ROUTES.transfer}><Button>Send your first transfer</Button></Link>}
            />
          ) : (
            <>
              <Table>
                <THead>
                  <tr>
                    <TH>Date</TH>
                    <TH>Reference</TH>
                    <TH>Status</TH>
                    <TH className="text-right">Amount</TH>
                  </tr>
                </THead>
                <TBody>
                  {tx.data.content.map((t) => {
                    const signedAmount = t.direction === 'DEBIT' ? `-${t.amount}` : t.amount;
                    const isFx = !!t.counterpartyCurrency && t.counterpartyCurrency !== t.currency;
                    return (
                      <TR key={t.referenceNumber}>
                        <TD>{formatDateTime(t.timestamp)}</TD>
                        <TD className="font-mono text-xs">{t.referenceNumber}</TD>
                        <TD>
                          <Badge tone={t.status === 'SUCCESS' || t.status === 'COMPLETED' ? 'success' : 'warning'}>{t.status}</Badge>
                        </TD>
                        <TD className="text-right">
                          <CurrencyDisplay amount={signedAmount} currency={t.currency} signed />
                          {isFx && t.counterpartyAmount && t.counterpartyCurrency && (
                            <div className="text-xs text-muted-foreground">
                              FX · <CurrencyDisplay amount={t.counterpartyAmount} currency={t.counterpartyCurrency} />
                            </div>
                          )}
                        </TD>
                      </TR>
                    );
                  })}
                </TBody>
              </Table>
              <Pagination page={tx.data.pageNumber} totalPages={tx.data.totalPages} onChange={setPage} />
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

