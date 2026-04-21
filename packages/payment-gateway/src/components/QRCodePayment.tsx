import React, { useEffect, useState } from 'react';

interface QRCodePaymentProps {
  amount: number;
  currency: string;
  orderId: string;
  onComplete: (data: any) => Promise<void>;
}

export const QRCodePayment: React.FC<QRCodePaymentProps> = ({
  amount,
  currency,
  orderId,
  onComplete,
}) => {
  const [qrCode, setQrCode] = useState<string>('');
  const [status, setStatus] = useState<'pending' | 'scanning' | 'completed'>('pending');

  useEffect(() => {
    // Generate QR code data
    const qrData = `PAYMENT:${orderId}:${amount}:${currency}`;
    // In production, use a QR code library like qrcode.react
    setQrCode(qrData);

    // Simulate payment status check
    const interval = setInterval(() => {
      // In production, poll backend for payment status
      const random = Math.random();
      if (random > 0.9) {
        setStatus('completed');
        clearInterval(interval);
        onComplete({ orderId, amount, currency });
      }
    }, 3000);

    return () => clearInterval(interval);
  }, [orderId, amount, currency]);

  return (
    <div className="qr-code-payment">
      <div className="qr-code-header">
        <h3>Scan to Pay</h3>
        <p>Use your mobile banking app to scan this QR code</p>
      </div>

      <div className="qr-code-container">
        {/* In production, use an actual QR code library */}
        <div className="qr-code-placeholder">
          <div className="qr-grid">
            {Array.from({ length: 25 }).map((_, i) => (
              <div
                key={i}
                className="qr-cell"
                style={{
                  background: Math.random() > 0.5 ? '#000' : '#fff',
                }}
              />
            ))}
          </div>
        </div>
      </div>

      <div className="qr-code-details">
        <div className="detail-row">
          <span className="detail-label">Amount:</span>
          <span className="detail-value">{currency} {amount.toFixed(2)}</span>
        </div>
        <div className="detail-row">
          <span className="detail-label">Order ID:</span>
          <span className="detail-value">{orderId}</span>
        </div>
      </div>

      <div className="qr-code-status">
        {status === 'pending' && (
          <div className="status-pending">
            <span className="status-icon">⏳</span>
            <span>Waiting for payment...</span>
          </div>
        )}
        {status === 'scanning' && (
          <div className="status-scanning">
            <span className="status-icon">📱</span>
            <span>Scanning detected...</span>
          </div>
        )}
        {status === 'completed' && (
          <div className="status-completed">
            <span className="status-icon">✅</span>
            <span>Payment received!</span>
          </div>
        )}
      </div>
    </div>
  );
};

