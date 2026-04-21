/**
 * Deprecated. The VeldApiClient singleton pointed at mismatched backend paths
 * (e.g. `/api/accounts_service/account/`) and leaked docker hostnames
 * (`http://account-service:3003`) in browser builds. All features have been
 * migrated to per-feature axios wrappers (e.g. `@features/accounts/api`) or
 * to `@lib/stub` when the corresponding backend service is not yet deployed.
 *
 * Kept as an empty module to avoid breaking any lingering imports — but
 * importing anything from here is a bug.
 */
export const veld: never = undefined as never;
