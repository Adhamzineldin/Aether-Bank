import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { decodeJwt } from '@lib/jwt';

export type Role = 'CUSTOMER' | 'EMPLOYEE' | 'ADMIN' | 'SUPERADMIN' | string;

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

function normalizeToken(rawToken: string): string {
  return rawToken.replace(/^Bearer\s+/i, '').trim();
}

function userFromToken(token: string, fallback?: Partial<SessionUser>): SessionUser {
  const p = decodeJwt(token) ?? {};

  // The backend may emit `roles` either as a JSON array (e.g. ["ADMIN","CUSTOMER"])
  // or as a single comma/space separated string (e.g. "SUPERADMIN" or
  // "ROLE_ADMIN,ROLE_USER"). `authorities` follows the same pattern. Normalise
  // everything into a string[] so consumers (and `.map` below) can rely on it.
  const toArray = (v: unknown): string[] | undefined => {
    if (v == null) return undefined;
    if (Array.isArray(v)) return v.filter((x) => typeof x === 'string') as string[];
    if (typeof v === 'string') {
      const parts = v.split(/[\s,]+/).map((s) => s.trim()).filter(Boolean);
      return parts.length ? parts : undefined;
    }
    return undefined;
  };

  const rolesRaw =
    toArray(p.roles) ??
    toArray(p.authorities) ??
    toArray((p as Record<string, unknown>).role) ??
    [];
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
        set({
          accessToken: normalizeToken(token),
          user: userFromToken(normalizeToken(token), fallback),
          hydrated: true,
        }),
      clear: () => set({ accessToken: null, user: null }),
      hasRole: (...roles) => {
        const u = get().user;
        if (!u) return false;
        return roles.some((r) => u.roles.includes(r));
      },
      isAuthenticated: () => !!get().accessToken?.trim(),
    }),
    {
      name: 'aether-auth',
      onRehydrateStorage: () => (state) => {
        if (state) state.hydrated = true;
      },
    },
  ),
);
