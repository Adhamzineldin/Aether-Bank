import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '@stores/authStore';
import { env } from './env';

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
    if (error.response?.status === 401) {
      const path = window.location.pathname;
      if (!path.startsWith('/auth')) {
        useAuthStore.getState().clear();
        window.location.assign(`/auth/login?next=${encodeURIComponent(path)}`);
      }
    }
    return Promise.reject(error);
  },
);

