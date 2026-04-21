import { useMutation } from '@tanstack/react-query';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';
import { http } from '@lib/axios';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';
import type { LoginValues, RegisterValues } from './schemas';

interface JwtResponse { accessToken: string; tokenType?: string; expiresIn?: number }
interface UserResponse { id: string; userName: string; email: string; role: string }

function extractAccessToken(payload: unknown): string {
  if (!payload || typeof payload !== 'object') return '';
  const data = payload as Record<string, unknown>;
  const direct =
    (typeof data.accessToken === 'string' && data.accessToken) ||
    (typeof data.token === 'string' && data.token) ||
    (typeof data.jwt === 'string' && data.jwt) ||
    '';
  if (!direct) return '';
  return direct.replace(/^Bearer\s+/i, '').trim();
}

export function useLogin() {
  const setSession = useAuthStore((s) => s.setSession);
  const navigate = useNavigate();
  const [search] = useSearchParams();
  return useMutation({
    mutationFn: async (input: LoginValues): Promise<unknown> => {
      const { data } = await http.post('/api/auth/login', input);
      return data;
    },
    onSuccess: (res, vars) => {
      const accessToken = extractAccessToken(res);
      if (!accessToken) {
        toast.error('Sign in succeeded, but no access token was returned.');
        return;
      }
      setSession(accessToken, { userName: vars.userName });
      toast.success('Welcome back!');
      const next = search.get('next') || ROUTES.dashboard;
      navigate(next, { replace: true });
    },
  });
}

export function useRegister() {
  const navigate = useNavigate();
  return useMutation({
    mutationFn: async (input: RegisterValues): Promise<UserResponse> => {
      const { data } = await http.post<UserResponse>('/api/auth/register', input);
      return data;
    },
    onSuccess: () => {
      toast.success('Account created. Please sign in.');
      navigate(ROUTES.login, { replace: true });
    },
  });
}

export function useLogout() {
  const clear = useAuthStore((s) => s.clear);
  const navigate = useNavigate();
  return useMutation({
    mutationFn: async (): Promise<void> => {
      try { await http.post('/api/auth/logout', {}); } catch { /* ignore */ }
    },
    onSettled: () => {
      clear();
      navigate(ROUTES.login, { replace: true });
    },
  });
}


