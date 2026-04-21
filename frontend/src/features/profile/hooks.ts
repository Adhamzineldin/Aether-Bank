import { useMutation } from '@tanstack/react-query';
import { toast } from 'sonner';
import { http } from '@lib/axios';
import { useAuthStore } from '@stores/authStore';

interface UpdateUserInput { userName?: string; email?: string }
interface ChangePasswordInput { oldPassword: string; newPassword: string }

/**
 * The IAM service does not expose user CRUD via the generated SDK yet; we fall back
 * to common REST routes through the gateway. Endpoints can be adjusted once finalised.
 */
export function useUpdateProfile() {
  const id = useAuthStore((s) => s.user?.id) || '';
  return useMutation({
    mutationFn: (input: UpdateUserInput) => http.put(`/api/auth/users/${id}`, input).then((r) => r.data),
    onSuccess: () => toast.success('Profile updated'),
  });
}

export function useChangePassword() {
  const id = useAuthStore((s) => s.user?.id) || '';
  return useMutation({
    mutationFn: (input: ChangePasswordInput) =>
      http.post(`/api/auth/users/${id}/change-password`, input).then((r) => r.data),
    onSuccess: () => toast.success('Password changed'),
  });
}

export function useDeleteAccount() {
  const id = useAuthStore((s) => s.user?.id) || '';
  const clear = useAuthStore((s) => s.clear);
  return useMutation({
    mutationFn: () => http.delete(`/api/auth/users/${id}`).then((r) => r.data),
    onSuccess: () => { toast.success('Account deleted'); clear(); },
  });
}
