import { AxiosError } from 'axios';

/**
 * Pulls the most user-friendly error message out of an unknown thrown value.
 *
 * Order of preference:
 *   1. Backend JSON body fields: `error` / `message` / `detail` / `title`
 *      (covers our `{error, code, status}` shape and Spring's ProblemDetail)
 *   2. Plain string body
 *   3. Axios HTTP "Status N" fallback
 *   4. `Error.message`
 *   5. Generic fallback
 */
export function extractErrorMessage(
  err: unknown,
  fallback = 'Something went wrong',
): string {
  if (err == null) return fallback;

  if (err instanceof AxiosError) {
    const data = err.response?.data as unknown;

    if (typeof data === 'string' && data.trim()) return data;

    if (data && typeof data === 'object') {
      const d = data as Record<string, unknown>;
      const candidates = [d.error, d.message, d.detail, d.title, d.errorMessage];
      for (const c of candidates) {
        if (typeof c === 'string' && c.trim()) return c;
      }
      // Spring validation: { errors: [{ defaultMessage }] } or { fieldErrors }
      const errors = d.errors;
      if (Array.isArray(errors) && errors.length) {
        const first = errors[0] as Record<string, unknown> | string;
        if (typeof first === 'string') return first;
        const m = (first?.defaultMessage ?? first?.message) as string | undefined;
        if (m) return m;
      }
    }

    if (err.response?.status) {
      return `Request failed (HTTP ${err.response.status})`;
    }
    if (err.code === 'ERR_NETWORK') return 'Network error — is the server reachable?';
    if (err.code === 'ECONNABORTED') return 'Request timed out';
    return err.message || fallback;
  }

  if (err instanceof Error) return err.message || fallback;
  if (typeof err === 'string') return err;

  return fallback;
}

