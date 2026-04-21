import React, { useState } from 'react';
import { BankTransferDetails } from '../types';

interface BankTransferFormProps {
  amount: number;
  currency: string;
  onSubmit: (data: BankTransferDetails) => Promise<void>;
  isProcessing: boolean;
}

export const BankTransferForm: React.FC<BankTransferFormProps> = ({
  amount,
  currency,
  onSubmit,
  isProcessing,
}) => {
  const [details, setDetails] = useState<BankTransferDetails>({
    accountNumber: '',
    routingNumber: '',
    accountHolderName: '',
    bankName: '',
  });

  const [errors, setErrors] = useState<Partial<BankTransferDetails>>({});

  const validate = (): boolean => {
    const newErrors: Partial<BankTransferDetails> = {};

    if (!details.accountNumber.trim()) {
      newErrors.accountNumber = 'Account number is required';
    }

    if (!details.routingNumber.trim()) {
      newErrors.routingNumber = 'Routing number is required';
    }

    if (!details.accountHolderName.trim()) {
      newErrors.accountHolderName = 'Account holder name is required';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (validate()) {
      await onSubmit(details);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bank-transfer-form">
      <div className="form-group">
        <label htmlFor="accountHolderName">Account Holder Name</label>
        <input
          id="accountHolderName"
          type="text"
          value={details.accountHolderName}
          onChange={(e) => setDetails({ ...details, accountHolderName: e.target.value })}
          placeholder="John Doe"
          disabled={isProcessing}
          className={errors.accountHolderName ? 'error' : ''}
        />
        {errors.accountHolderName && (
          <span className="error-message">{errors.accountHolderName}</span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="accountNumber">Account Number</label>
        <input
          id="accountNumber"
          type="text"
          value={details.accountNumber}
          onChange={(e) => setDetails({ ...details, accountNumber: e.target.value.replace(/\D/g, '') })}
          placeholder="1234567890"
          disabled={isProcessing}
          className={errors.accountNumber ? 'error' : ''}
        />
        {errors.accountNumber && (
          <span className="error-message">{errors.accountNumber}</span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="routingNumber">Routing Number</label>
        <input
          id="routingNumber"
          type="text"
          value={details.routingNumber}
          onChange={(e) => setDetails({ ...details, routingNumber: e.target.value.replace(/\D/g, '').slice(0, 9) })}
          placeholder="021000021"
          maxLength={9}
          disabled={isProcessing}
          className={errors.routingNumber ? 'error' : ''}
        />
        {errors.routingNumber && (
          <span className="error-message">{errors.routingNumber}</span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="bankName">Bank Name (Optional)</label>
        <input
          id="bankName"
          type="text"
          value={details.bankName}
          onChange={(e) => setDetails({ ...details, bankName: e.target.value })}
          placeholder="Bank of America"
          disabled={isProcessing}
        />
      </div>

      <button
        type="submit"
        className="submit-button"
        disabled={isProcessing}
      >
        {isProcessing ? 'Processing...' : `Pay ${currency} ${amount.toFixed(2)}`}
      </button>
    </form>
  );
};

