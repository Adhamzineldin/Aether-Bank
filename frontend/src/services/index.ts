import apiClient from './api';

export const authService = {
  login: async (email: string, password: string) => {
    const response = await apiClient.post('/api/iam/login', { email, password });
    return response.data;
  },

  register: async (data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
  }) => {
    const response = await apiClient.post('/api/iam/register', data);
    return response.data;
  },

  logout: async () => {
    const response = await apiClient.post('/api/iam/logout');
    return response.data;
  },
};

export const accountService = {
  getAccounts: async (customerId: string) => {
    const response = await apiClient.get(`/api/accounts/customer/${customerId}`);
    return response.data;
  },

  getAccount: async (accountId: string) => {
    const response = await apiClient.get(`/api/accounts/${accountId}`);
    return response.data;
  },

  openAccount: async (data: {
    customerId: string;
    accountType: string;
    currency: string;
    initialDeposit?: number;
  }) => {
    const response = await apiClient.post('/api/accounts', data);
    return response.data;
  },
};

export const transactionService = {
  transfer: async (data: {
    sourceAccountId: string;
    destinationAccountId: string;
    amount: number;
    currency: string;
    description?: string;
  }) => {
    const response = await apiClient.post('/api/transaction_service/transactions/transfer', data);
    return response.data;
  },

  getTransactions: async (accountId: string) => {
    const response = await apiClient.get(`/api/transaction_service/transactions/account/${accountId}`);
    return response.data;
  },

  deposit: async (data: {
    accountId: string;
    amount: number;
    currency: string;
  }) => {
    const response = await apiClient.post('/api/transaction_service/transactions/deposit', data);
    return response.data;
  },
};

