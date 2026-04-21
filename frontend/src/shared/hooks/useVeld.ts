import { veld } from '@lib/veld';

/** Hook returning the singleton Veld API client (proxies all sub-clients). */
export function useVeld() {
  return veld;
}

