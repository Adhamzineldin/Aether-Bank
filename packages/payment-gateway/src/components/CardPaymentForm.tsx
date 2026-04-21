import React, { useState } from 'react';
import { CardDetails } from '../types';
import { validateCardNumber, validateCVV, validateExpiry } from '../utils/validation';
import { formatCardNumber, formatExpiry } from '../utils/formatting';

interface CardPaymentFormProps {
  amount: number;
  currency: string;
  requireCVV: boolean;
  saveCard: boolean;
  onSubmit: (data: CardDetails) => Promise<void>;
  isProcessing: boolean;
}

export const CardPaymentForm: React.FC<CardPaymentFormProps> = ({
  amount,
  currency,
  requireCVV,
  saveCard,
  onSubmit,
  isProcessing,
}) => {
  const [cardDetails, setCardDetails] = useState<CardDetails>({
    cardNumber: '',
    cardholderName: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
  });

  const [errors, setErrors] = useState<Partial<CardDetails>>({});
  const [shouldSaveCard, setShouldSaveCard] = useState(false);

  const handleCardNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatCardNumber(e.target.value);
    setCardDetails({ ...cardDetails, cardNumber: formatted });
    if (errors.cardNumber) {
      setErrors({ ...errors, cardNumber: undefined });
    }
  };

  const handleExpiryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatExpiry(e.target.value);
    const [month, year] = formatted.split('/');
    setCardDetails({ ...cardDetails, expiryMonth: month || '', expiryYear: year || '' });
    if (errors.expiryMonth) {
      setErrors({ ...errors, expiryMonth: undefined, expiryYear: undefined });
    }
  };

  const validate = (): boolean => {
    const newErrors: Partial<CardDetails> = {};

    if (!validateCardNumber(cardDetails.cardNumber)) {
      newErrors.cardNumber = 'Invalid card number';
    }

    if (!cardDetails.cardholderName.trim()) {
      newErrors.cardholderName = 'Name is required';
    }

    if (!validateExpiry(cardDetails.expiryMonth, cardDetails.expiryYear)) {
      newErrors.expiryMonth = 'Invalid expiry date';
    }

    if (requireCVV && !validateCVV(cardDetails.cvv)) {
      newErrors.cvv = 'Invalid CVV';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (validate()) {
      await onSubmit(cardDetails);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="card-payment-form">
      <div className="form-group">
        <label htmlFor="cardNumber">Card Number</label>
        <input
          id="cardNumber"
          type="text"
          value={cardDetails.cardNumber}
          onChange={handleCardNumberChange}
          placeholder="1234 5678 9012 3456"
          maxLength={19}
          disabled={isProcessing}
          className={errors.cardNumber ? 'error' : ''}
        />
        {errors.cardNumber && (
          <span className="error-message">{errors.cardNumber}</span>
        )}
      </div>

      <div className="form-group">
        <label htmlFor="cardholderName">Cardholder Name</label>
        <input
          id="cardholderName"
          type="text"
          value={cardDetails.cardholderName}
          onChange={(e) => setCardDetails({ ...cardDetails, cardholderName: e.target.value })}
          placeholder="JOHN DOE"
          disabled={isProcessing}
          className={errors.cardholderName ? 'error' : ''}
        />
        {errors.cardholderName && (
          <span className="error-message">{errors.cardholderName}</span>
        )}
      </div>

      <div className="form-row">
        <div className="form-group">
          <label htmlFor="expiry">Expiry Date</label>
          <input
            id="expiry"
            type="text"
            value={`${cardDetails.expiryMonth}${cardDetails.expiryYear ? '/' + cardDetails.expiryYear : ''}`}
            onChange={handleExpiryChange}
            placeholder="MM/YY"
            maxLength={5}
            disabled={isProcessing}
            className={errors.expiryMonth ? 'error' : ''}
          />
          {errors.expiryMonth && (
            <span className="error-message">{errors.expiryMonth}</span>
          )}
        </div>

        {requireCVV && (
          <div className="form-group">
            <label htmlFor="cvv">CVV</label>
            <input
              id="cvv"
              type="text"
              value={cardDetails.cvv}
              onChange={(e) => setCardDetails({ ...cardDetails, cvv: e.target.value.replace(/\D/g, '').slice(0, 4) })}
              placeholder="123"
              maxLength={4}
              disabled={isProcessing}
              className={errors.cvv ? 'error' : ''}
            />
            {errors.cvv && (
              <span className="error-message">{errors.cvv}</span>
            )}
          </div>
        )}
      </div>

      {saveCard && (
        <div className="form-group form-checkbox">
          <input
            type="checkbox"
            id="saveCard"
            checked={shouldSaveCard}
            onChange={(e) => setShouldSaveCard(e.target.checked)}
            disabled={isProcessing}
          />
          <label htmlFor="saveCard">Save card for future payments</label>
        </div>
      )}

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

