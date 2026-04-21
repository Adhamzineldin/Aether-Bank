import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { Card, CardContent } from '@shared/ui/Card';
import { Pagination } from '@shared/ui/Pagination';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Badge } from '@shared/ui/Badge';
import { EmptyState } from '@shared/ui/EmptyState';
import { ROUTES } from '@app/routes';
import { formatDateTime } from '@shared/utils/date';
import { useAccount } from '../hooks';
import { useAccountTransactions } from '@features/transactions/hooks';

export default function AccountStatementPage() {
  const { accountId = '' } = useParams();
  const { data: acc } = useAccount(accountId);
  const [page, setPage] = useState(0);
  const tx = useAccountTransactions(accountId, acc?.account.currency || 'USD', page);

  return (
    <div className="space-y-4">
      <PageHeader
        title="Account statement"
        description={acc ? `${acc.account.accountType} · ${acc.account.accountNumber}` : '...'}
        back={{ to: ROUTES.account(accountId) }}
      />
      <Card>
        <CardContent>
          {tx.isLoading ? (
            <div className="space-y-2">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
          ) : !tx.data?.content?.length ? (
            <EmptyState title="No transactions" description="Transactions will appear here once funds move through this account." />
          ) : (
            <>
              <Table>
                <THead>
                  <tr>
                    <TH>Date</TH>
                    <TH>Type</TH>
                    <TH>Status</TH>
                    <TH className="text-right">Amount</TH>
                  </tr>
                </THead>
                <TBody>
                  {tx.data.content.map((t: any, i: number) => (
                    <TR key={i}>
                      <TD>{formatDateTime(t.executedAt || t.createdAt)}</TD>
                      <TD className="font-medium">{t.transactionType || '—'}</TD>
                      <TD><Badge tone={t.status === 'COMPLETED' ? 'success' : 'warning'}>{t.status}</Badge></TD>
                      <TD className="text-right"><CurrencyDisplay amount={t.totalAmount} currency={acc?.account.currency} /></TD>
                    </TR>
                  ))}
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

