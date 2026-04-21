import React, { useState } from 'react';
import { PaymentGatewayProps, PaymentResult, CardDetails } from '../types';
import { CardPaymentForm } from './CardPaymentForm';
import { BankTransferForm } from './BankTransferForm';
import { DigitalWalletButton } from './DigitalWalletButton';
import { QRCodePayment } from './QRCodePayment';
import './PaymentGateway.css';

export const PaymentGateway: React.FC<PaymentGatewayProps> = ({
  amount,
  currency,
  onSuccess,
  onError,
  merchantId,
  orderId,
  customerEmail,
  customerName,
  methods = ['card', 'bank_transfer'],
  recurring = false,
  recurringInterval,
  theme = 'modern',
  primaryColor = '#2563eb',
  locale = 'en-US',
  saveCard = false,
  requireCVV = true,
  sandbox = false,
  onCancel,
  onMethodChange,
  processor,
}) => {
  const [selectedMethod, setSelectedMethod] = useState<string>(methods[0] || 'card');
  const [isProcessing, setIsProcessing] = useState(false);

  const handleMethodSelect = (method: string) => {
    setSelectedMethod(method);
    onMethodChange?.(method);
  };

  const processPayment = async (paymentData: any) => {
    setIsProcessing(true);
    try {
      let result: PaymentResult;
      if (processor) {
        result = await processor({
          method: selectedMethod as any,
          card: paymentData,
          bank: paymentData,
          amount,
          currency,
          orderId,
        });
      } else {
        await new Promise(resolve => setTimeout(resolve, sandbox ? 1000 : 2000));
        const isSuccess = sandbox ? Math.random() > 0.1 : true;
        if (!isSuccess) throw new Error('Payment declined');
        result = {
          success: true,
          transactionId: `TXN-${Date.now()}`,
          message: 'Payment processed successfully',
          timestamp: new Date(),
        };
      }
      if (result.success) onSuccess(result);
      else throw new Error(result.message || 'Payment failed');
    } catch (error) {
      onError(error as Error);
    } finally {
      setIsProcessing(false);
    }
  };

  const formatAmount = () => {
    return new Intl.NumberFormat(locale, {
      style: 'currency',
      currency,
    }).format(amount);
  };

  return (
    <div 
      className={`payment-gateway payment-gateway--${theme}`}
      style={{ '--primary-color': primaryColor } as React.CSSProperties}
    >
      {/* Header */}
      <div className="payment-gateway__header">
        <h2 className="payment-gateway__title">Complete Payment</h2>
        <div className="payment-gateway__amount">
          {formatAmount()}
          {recurring && (
            <span className="payment-gateway__recurring">
              /{recurringInterval}
            </span>
          )}
        </div>
        {sandbox && (
          <div className="payment-gateway__sandbox-badge">
            🧪 Test Mode
          </div>
        )}
      </div>

      {/* Payment Methods */}
      <div className="payment-gateway__methods">
        {methods.includes('card') && (
          <button
            className={`payment-method ${selectedMethod === 'card' ? 'active' : ''}`}
            onClick={() => handleMethodSelect('card')}
            disabled={isProcessing}
          >
            💳 Card
          </button>
        )}
        {methods.includes('bank_transfer') && (
          <button
            className={`payment-method ${selectedMethod === 'bank_transfer' ? 'active' : ''}`}
            onClick={() => handleMethodSelect('bank_transfer')}
            disabled={isProcessing}
          >
            🏦 Bank Transfer
          </button>
        )}
        {methods.includes('apple_pay') && (
          <button
            className={`payment-method ${selectedMethod === 'apple_pay' ? 'active' : ''}`}
            onClick={() => handleMethodSelect('apple_pay')}
            disabled={isProcessing}
          >
            🍎 Apple Pay
          </button>
        )}
        {methods.includes('google_pay') && (
          <button
            className={`payment-method ${selectedMethod === 'google_pay' ? 'active' : ''}`}
            onClick={() => handleMethodSelect('google_pay')}
            disabled={isProcessing}
          >
            🔵 Google Pay
          </button>
        )}
        {methods.includes('qr_code') && (
          <button
            className={`payment-method ${selectedMethod === 'qr_code' ? 'active' : ''}`}
            onClick={() => handleMethodSelect('qr_code')}
            disabled={isProcessing}
          >
            📱 QR Code
          </button>
        )}
      </div>

      {/* Payment Form */}
      <div className="payment-gateway__form">
        {selectedMethod === 'card' && (
          <CardPaymentForm
            amount={amount}
            currency={currency}
            requireCVV={requireCVV}
            saveCard={saveCard}
            onSubmit={processPayment}
            isProcessing={isProcessing}
          />
        )}

        {selectedMethod === 'bank_transfer' && (
          <BankTransferForm
            amount={amount}
            currency={currency}
            onSubmit={processPayment}
            isProcessing={isProcessing}
          />
        )}

        {(selectedMethod === 'apple_pay' || selectedMethod === 'google_pay') && (
          <DigitalWalletButton
            type={selectedMethod as 'apple_pay' | 'google_pay'}
            amount={amount}
            currency={currency}
            onSubmit={processPayment}
            isProcessing={isProcessing}
          />
        )}

        {selectedMethod === 'qr_code' && (
          <QRCodePayment
            amount={amount}
            currency={currency}
            orderId={orderId || `ORDER-${Date.now()}`}
            onComplete={processPayment}
          />
        )}
      </div>

      {/* Footer */}
      <div className="payment-gateway__footer">
        <div className="payment-gateway__security">
          🔒 Secured by SSL encryption
        </div>
        {onCancel && (
          <button
            className="payment-gateway__cancel"
            onClick={onCancel}
            disabled={isProcessing}
          >
            Cancel
          </button>
        )}
      </div>
    </div>
  );
};

export default PaymentGateway;

