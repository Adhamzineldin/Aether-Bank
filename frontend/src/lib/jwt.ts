export interface JwtPayload {
  sub?: string;
  userId?: string;
  customerId?: string;
  email?: string;
  username?: string;
  userName?: string;
  roles?: string[];
  role?: string;
  authorities?: string[];
  exp?: number;
  iat?: number;
  [k: string]: unknown;
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const part = token.split('.')[1];
    if (!part) return null;
    const padded = part.replace(/-/g, '+').replace(/_/g, '/').padEnd(part.length + ((4 - (part.length % 4)) % 4), '=');
    const json = atob(padded);
    return JSON.parse(decodeURIComponent(escape(json))) as JwtPayload;
  } catch {
    return null;
  }
}

export function isExpired(payload: JwtPayload | null): boolean {
  if (!payload?.exp) return false;
  return Date.now() >= payload.exp * 1000;
}

