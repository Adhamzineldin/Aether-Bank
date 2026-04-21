import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { decodeJwt } from '@lib/jwt';

export type Role = 'CUSTOMER' | 'EMPLOYEE' | 'ADMIN' | string;

export interface SessionUser {
  id: string;
  userName: string;
  email?: string;
  roles: Role[];
}

interface AuthState {
  accessToken: string | null;
  user: SessionUser | null;
  hydrated: boolean;
  setSession: (token: string, fallbackUser?: Partial<SessionUser>) => void;
  clear: () => void;
  hasRole: (...roles: Role[]) => boolean;
  isAuthenticated: () => boolean;
}

function userFromToken(token: string, fallback?: Partial<SessionUser>): SessionUser {
  const p = decodeJwt(token) ?? {};
  const rolesRaw =
    (p.roles as string[] | undefined) ??
    (Array.isArray(p.authorities) ? (p.authorities as string[]) : undefined) ??
    (p.role ? [p.role as string] : []);
  const roles = rolesRaw.map((r) => r.replace(/^ROLE_/, ''));
  return {
    id: (p.userId as string) || (p.sub as string) || fallback?.id || '',
    userName: (p.userName as string) || (p.username as string) || fallback?.userName || '',
    email: (p.email as string) || fallback?.email,
    roles: roles.length ? roles : fallback?.roles ?? ['CUSTOMER'],
  };
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      user: null,
      hydrated: false,
      setSession: (token, fallback) =>
        set({ accessToken: token, user: userFromToken(token, fallback), hydrated: true }),
      clear: () => set({ accessToken: null, user: null }),
      hasRole: (...roles) => {
        const u = get().user;
        if (!u) return false;
        return roles.some((r) => u.roles.includes(r));
      },
      isAuthenticated: () => !!get().accessToken,
    }),
    {
      name: 'aether-auth',
      onRehydrateStorage: () => (state) => {
        if (state) state.hydrated = true;
      },
    },
  ),
);
