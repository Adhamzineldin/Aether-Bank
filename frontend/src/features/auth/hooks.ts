import { useMutation } from '@tanstack/react-query';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { toast } from 'sonner';
import { http } from '@lib/axios';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';
import type { LoginValues, RegisterValues } from './schemas';

interface JwtResponse { accessToken: string; tokenType?: string; expiresIn?: number }
interface UserResponse { id: string; userName: string; email: string; role: string }

export function useLogin() {
  const setSession = useAuthStore((s) => s.setSession);
  const navigate = useNavigate();
  const [search] = useSearchParams();
  return useMutation({
    mutationFn: async (input: LoginValues): Promise<JwtResponse> => {
      const { data } = await http.post<JwtResponse>('/api/auth/login', input);
      return data;
    },
    onSuccess: (res, vars) => {
      setSession(res.accessToken, { userName: vars.userName });
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


