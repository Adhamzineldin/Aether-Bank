import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@stores/authStore';
import { env } from './env';
import { extractErrorMessage } from './errors';

export const http = axios.create({
  baseURL: env.API_URL || '',
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.set('Authorization', `Bearer ${token}`);
  return config;
});

http.interceptors.response.use(
  (r) => r,
  (error: AxiosError) => {
    // Handle global 401 -> bounce to login (skip if we're already in /auth/*).
    if (error.response?.status === 401) {
      const path = window.location.pathname;
      if (!path.startsWith('/auth')) {
        useAuthStore.getState().clear();
        window.location.assign(`/auth/login?next=${encodeURIComponent(path)}`);
      }
    }

    // Replace the generic "Request failed with status code 500" with the
    // backend's actual error body (e.g. "Username already exists") so any
    // downstream consumer that reads `err.message` (toast, react-query
    // mutation onError, etc.) gets a useful message for free.
    const friendly = extractErrorMessage(error);
    if (friendly && friendly !== error.message) {
      try {
        Object.defineProperty(error, 'message', { value: friendly, configurable: true });
      } catch {
        // message is non-writable in some envs — ignore.
      }
    }

    return Promise.reject(error);
  },
);

