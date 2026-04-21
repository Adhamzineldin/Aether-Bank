import { VeldApiClient } from '@veld/client';
import { useAuthStore } from '@stores/authStore';
import { env } from './env';

/**
 * The generated SDK falls back to per-service docker URLs (e.g. http://account-service:3003)
 * when no baseUrl is supplied. In the browser we always want requests to go through the same
 * origin (handled by Vite proxy in dev / nginx in prod) so we hard-pin to window.location.origin
 * unless an explicit override is provided via VITE_API_URL.
 */
function resolvedBase(): string {
  if (env.API_URL) return env.API_URL;
  if (typeof window !== 'undefined') return window.location.origin;
  return '/';
}

function build(token: string | null): VeldApiClient {
  return new VeldApiClient({
    baseUrl: resolvedBase(),
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
}

let client: VeldApiClient = build(useAuthStore.getState().accessToken);

useAuthStore.subscribe((s) => {
  client = build(s.accessToken);
});

export const veld = new Proxy({} as VeldApiClient, {
  get(_t, prop) {
    return (client as unknown as Record<string | symbol, unknown>)[prop];
  },
});
