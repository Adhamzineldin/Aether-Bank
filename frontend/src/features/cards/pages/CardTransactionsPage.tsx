import { useParams } from 'react-router-dom';
import { useState } from 'react';
import { Card, CardContent } from '@shared/ui/Card';
import { PageHeader } from '@shared/ui/PageHeader';
import { Skeleton } from '@shared/ui/Skeleton';
import { Pagination } from '@shared/ui/Pagination';
import { Table, THead, TBody, TR, TH, TD } from '@shared/ui/Table';
import { CurrencyDisplay } from '@shared/ui/CurrencyDisplay';
import { Badge } from '@shared/ui/Badge';
import { EmptyState } from '@shared/ui/EmptyState';
import { ROUTES } from '@app/routes';
import { formatDateTime } from '@shared/utils/date';
import { useCardTransactions } from '../hooks';

export default function CardTransactionsPage() {
  const { cardId = '' } = useParams();
  const [page, setPage] = useState(0);
  const { data, isLoading } = useCardTransactions(cardId, page);

  return (
    <div>
      <PageHeader title="Card transactions" back={{ to: ROUTES.card(cardId) }} />
      <Card>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">{Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-10" />)}</div>
          ) : !data?.content?.length ? (
            <EmptyState title="No transactions yet" description="Card purchases will appear here in real time." />
          ) : (
            <>
              <Table>
                <THead>
                  <tr>
                    <TH>Date</TH>
                    <TH>Type</TH>
                    <TH>Status</TH>
                    <TH>Auth</TH>
                    <TH className="text-right">Amount</TH>
                  </tr>
                </THead>
                <TBody>
                  {data.content.map((t) => (
                    <TR key={t.transactionId}>
                      <TD>{formatDateTime(t.processedAt)}</TD>
                      <TD className="font-medium">{t.type}</TD>
                      <TD><Badge tone={t.status === 'APPROVED' ? 'success' : t.status === 'DECLINED' ? 'danger' : 'warning'}>{t.status}</Badge></TD>
                      <TD className="font-mono text-xs">{t.authCode || '—'}</TD>
                      <TD className="text-right"><CurrencyDisplay amount={t.amount} currency={t.currency} /></TD>
                    </TR>
                  ))}
                </TBody>
              </Table>
              <Pagination page={data.pageNumber} totalPages={data.totalPages} onChange={setPage} />
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

