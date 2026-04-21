import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { http } from '@lib/axios';

export interface AdminUser {
  id: string;
  username: string;
  email: string;
  fullName?: string | null;
  roles: string[];
  isActive: boolean;
  isEmailVerified: boolean;
  isLocked: boolean;
  mfaEnabled: boolean;
  createdAt?: string | null;
  lastLogin?: string | null;
}

export const adminKeys = {
  users: ['admin', 'users'] as const,
  user: (id: string) => ['admin', 'user', id] as const,
  roles: ['admin', 'roles'] as const,
};

const BASE = '/api/auth';

export const adminApi = {
  listUsers: () => http.get<AdminUser[]>(`${BASE}/users`).then((r) => r.data),
  getUser: (id: string) => http.get<AdminUser>(`${BASE}/users/${id}`).then((r) => r.data),
  listRoles: () => http.get<string[]>(`${BASE}/roles`).then((r) => r.data),
  assignRole: (id: string, role: string) =>
    http.post<AdminUser>(`${BASE}/users/${id}/roles`, { role }).then((r) => r.data),
  removeRole: (id: string, role: string) =>
    http.delete<AdminUser>(`${BASE}/users/${id}/roles/${role}`).then((r) => r.data),
  lock: (id: string) => http.post<AdminUser>(`${BASE}/users/${id}/lock`).then((r) => r.data),
  unlock: (id: string) => http.post<AdminUser>(`${BASE}/users/${id}/unlock`).then((r) => r.data),
  activate: (id: string) => http.post<AdminUser>(`${BASE}/users/${id}/activate`).then((r) => r.data),
  deactivate: (id: string) => http.post<AdminUser>(`${BASE}/users/${id}/deactivate`).then((r) => r.data),
};

export function useAdminUsers() {
  return useQuery({ queryKey: adminKeys.users, queryFn: adminApi.listUsers });
}

export function useAdminUser(id: string | undefined) {
  return useQuery({
    queryKey: adminKeys.user(id || ''),
    enabled: !!id,
    queryFn: () => adminApi.getUser(id as string),
  });
}

export function useRoles() {
  return useQuery({ queryKey: adminKeys.roles, queryFn: adminApi.listRoles });
}

function useUserMutation(fn: (id: string, arg?: string) => Promise<AdminUser>, successMsg: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, arg }: { id: string; arg?: string }) => fn(id, arg),
    onSuccess: (_data, { id }) => {
      toast.success(successMsg);
      qc.invalidateQueries({ queryKey: adminKeys.users });
      qc.invalidateQueries({ queryKey: adminKeys.user(id) });
    },
  });
}

export const useAssignRole = () =>
  useUserMutation((id, role) => adminApi.assignRole(id, role as string), 'Role assigned');
export const useRemoveRole = () =>
  useUserMutation((id, role) => adminApi.removeRole(id, role as string), 'Role removed');
export const useLockUser = () => useUserMutation((id) => adminApi.lock(id), 'User locked');
export const useUnlockUser = () => useUserMutation((id) => adminApi.unlock(id), 'User unlocked');
export const useActivateUser = () => useUserMutation((id) => adminApi.activate(id), 'User activated');
export const useDeactivateUser = () => useUserMutation((id) => adminApi.deactivate(id), 'User deactivated');

