import React from 'react';

interface DigitalWalletButtonProps {
  type: 'apple_pay' | 'google_pay';
  amount: number;
  currency: string;
  onSubmit: (data: any) => Promise<void>;
  isProcessing: boolean;
}

export const DigitalWalletButton: React.FC<DigitalWalletButtonProps> = ({
  type,
  amount,
  currency,
  onSubmit,
  isProcessing,
}) => {
  const handleClick = async () => {
    // In production, this would integrate with Apple Pay / Google Pay SDK
    const paymentData = {
      type,
      amount,
      currency,
      timestamp: new Date(),
    };
    await onSubmit(paymentData);
  };

  return (
    <div className="digital-wallet-container">
      <p className="digital-wallet-description">
        {type === 'apple_pay' 
          ? 'Pay securely with Apple Pay' 
          : 'Pay securely with Google Pay'}
      </p>
      
      <button
        className={`digital-wallet-button digital-wallet-button--${type}`}
        onClick={handleClick}
        disabled={isProcessing}
      >
        {type === 'apple_pay' ? (
          <span className="wallet-icon">🍎 Apple Pay</span>
        ) : (
          <span className="wallet-icon">🔵 Google Pay</span>
        )}
        <span className="wallet-amount">
          {currency} {amount.toFixed(2)}
        </span>
      </button>

      <p className="digital-wallet-note">
        You'll be redirected to complete the payment
      </p>
    </div>
  );
};

