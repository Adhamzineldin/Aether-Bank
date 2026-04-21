export interface PaymentMethod {
  id: string;
  type: 'card' | 'bank_transfer' | 'wallet' | 'qr_code';
  name: string;
  icon?: React.ReactNode;
  enabled: boolean;
}

export interface CardDetails {
  cardNumber: string;
  cardholderName: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
}

export interface BankTransferDetails {
  accountNumber: string;
  routingNumber: string;
  accountHolderName: string;
  bankName?: string;
}

export interface PaymentResult {
  success: boolean;
  transactionId?: string;
  message?: string;
  timestamp: Date;
}

export interface PaymentGatewayProps {
  // Required props
  amount: number;
  currency: string;
  onSuccess: (result: PaymentResult) => void;
  onError: (error: Error) => void;

  // Optional props
  merchantId?: string;
  orderId?: string;
  customerEmail?: string;
  customerName?: string;
  
  // Payment methods
  methods?: Array<'card' | 'bank_transfer' | 'apple_pay' | 'google_pay' | 'qr_code'>;
  
  // Recurring payments
  recurring?: boolean;
  recurringInterval?: 'daily' | 'weekly' | 'monthly' | 'yearly';
  
  // UI customization
  theme?: 'light' | 'dark' | 'modern' | 'minimal';
  primaryColor?: string;
  locale?: string;
  
  // Features
  saveCard?: boolean;
  requireCVV?: boolean;
  sandbox?: boolean; // Test mode
  
  // Callbacks
  onCancel?: () => void;
  onMethodChange?: (method: string) => void;
}

export interface PaymentGatewayConfig {
  apiEndpoint: string;
  publicKey: string;
  sandbox: boolean;
}

export interface PaymentToken {
  token: string;
  type: string;
  expiresAt: Date;
}

