import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { EmptyState } from '@shared/ui/EmptyState';
import { Skeleton } from '@shared/ui/Skeleton';
import { Pagination } from '@shared/ui/Pagination';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Badge } from '@shared/ui/Badge';
import { ROUTES } from '@app/routes';
import { formatDateTime } from '@shared/utils/date';
import { useAccountTransactions } from '@features/transactions/hooks';
import type { TransactionRowDto } from '@features/transactions/api';
import { useAccount } from '../hooks';

export default function AccountStatementPage() {
  const { accountId = '' } = useParams();
  const { data: a } = useAccount(accountId);
  const [page, setPage] = useState(0);
  const currency = a?.currency || 'USD';
  const tx = useAccountTransactions(accountId, currency, page);

  return (
    <div className="space-y-4">
      <PageHeader
        title="Account statement"
        description={a ? `${a.accountType} · ${a.accountNumber}` : '...'}
        back={{ to: ROUTES.account(accountId) }}
      />
      <Card>
        <CardContent className="space-y-4">
          {tx.isLoading ? (
            <div className="space-y-2">
              {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-10" />)}
            </div>
          ) : !tx.data?.content?.length ? (
            <EmptyState
              title="No transactions yet"
              description="Transactions will appear here once funds move through this account."
            />
          ) : (
            <>
              <Table>
                <THead>
                  <tr>
                    <TH>Date</TH>
                    <TH>Reference</TH>
                    <TH>Type</TH>
                    <TH>Status</TH>
                    <TH className="text-right">Amount</TH>
                  </tr>
                </THead>
                <TBody>
                  {tx.data.content.map((t) => <StatementRow key={t.referenceNumber} row={t} />)}
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

function StatementRow({ row }: { row: TransactionRowDto }) {
  const signedAmount = row.direction === 'DEBIT' ? `-${row.amount}` : row.amount;
  const isFx = !!row.counterpartyCurrency && row.counterpartyCurrency !== row.currency;

  return (
    <TR>
      <TD>{formatDateTime(row.timestamp)}</TD>
      <TD className="font-mono text-xs">{row.referenceNumber}</TD>
      <TD>
        <div className="flex flex-col">
          <span>{row.type}</span>
          {isFx && row.counterpartyAmount && row.counterpartyCurrency && (
            <span className="text-xs text-muted-foreground">
              FX {row.direction === 'DEBIT' ? '→' : '←'}{' '}
              <CurrencyDisplay amount={row.counterpartyAmount} currency={row.counterpartyCurrency} />
            </span>
          )}
        </div>
      </TD>
      <TD>
        <Badge tone={row.status === 'SUCCESS' || row.status === 'COMPLETED' ? 'success' : 'warning'}>
          {row.status}
        </Badge>
      </TD>
      <TD className="text-right">
        <CurrencyDisplay amount={signedAmount} currency={row.currency} signed />
      </TD>
    </TR>
  );
}
