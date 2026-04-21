import { useCallback } from 'react';
import { PaymentGateway, type PaymentResult, type PaymentData } from '@aether/payment-gateway';
import { useProcessMerchantPayment } from '@features/cards/hooks';
import { useUIStore } from '@stores/uiStore';
import type { Decimal, MerchantPaymentRequest, UUID } from '@veld/types';

export interface CheckoutContainerProps {
  amount: number;
  currency: string;
  merchantId?: string;
  cardToken?: string;
  description?: string;
  onSuccess?: (result: PaymentResult) => void;
  onCancel?: () => void;
}

/**
 * Wraps the shared `<PaymentGateway/>` SDK with a real processor that calls
 * the card-service merchant payment endpoint via the Veld client.
 */
export function CheckoutContainer({
  amount, currency, merchantId, cardToken, description, onSuccess, onCancel,
}: CheckoutContainerProps) {
  const theme = useUIStore((s) => s.theme);
  const process = useProcessMerchantPayment();

  const processor = useCallback(
    async (data: PaymentData): Promise<PaymentResult> => {
      // We only handle the card method against the real backend; other rails
      // are simulated by the SDK fallback when no processor is provided.
      if (data.method !== 'card') {
        return {
          success: true,
          transactionId: `MOCK-${Date.now()}`,
          message: `${data.method} accepted (simulated)`,
          timestamp: new Date(),
        };
      }
      const payload: MerchantPaymentRequest = {
        cardToken: cardToken || (data.card?.cardNumber ?? '').replace(/\s/g, ''),
        merchantId: (merchantId || crypto.randomUUID()) as UUID,
        amount: String(amount) as Decimal,
        currency,
        idempotencyKey: crypto.randomUUID(),
        description,
      } as MerchantPaymentRequest;
      const res = await process.mutateAsync(payload);
      return {
        success: res.status === 'APPROVED',
        transactionId: res.transactionId,
        message: res.status === 'APPROVED' ? 'Payment approved' : `Payment ${res.status.toLowerCase()}`,
        timestamp: new Date(res.processedAt),
      };
    },
    [amount, currency, merchantId, cardToken, description, process],
  );

  return (
    <PaymentGateway
      amount={amount}
      currency={currency}
      methods={['card', 'bank_transfer', 'apple_pay', 'qr_code']}
      theme={theme === 'dark' ? 'dark' : 'modern'}
      sandbox
      saveCard
      processor={processor}
      onSuccess={(r: PaymentResult) => onSuccess?.(r)}
      onError={(e: Error) => console.error('Payment error', e)}
      onCancel={onCancel}
    />
  );
}




