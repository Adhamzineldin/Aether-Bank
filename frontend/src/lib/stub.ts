import { useQuery, type UseQueryResult } from '@tanstack/react-query';

/**
 * Returns a react-query result that resolves to the given value (or `undefined`)
 * without hitting the network. Used for feature hooks whose backend REST
 * controllers are not yet implemented (card / financial / notification /
 * transaction services currently have no REST layer).
 *
 * Default generic is `any` so consumer pages can still deep-destructure from
 * `data` without TypeScript complaining — the pages are expected to guard
 * against `isLoading` / `!data` before rendering, which keeps runtime safe.
 */
export function useStubQuery<T = any>(key: readonly unknown[], value?: T): UseQueryResult<T> {
  return useQuery({
    queryKey: ['stub', ...key],
    queryFn: async () => (value as T) ?? (undefined as unknown as T),
    staleTime: Infinity,
    refetchOnMount: false,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
}

/** Mutation factory for unavailable features — rejects with a clean message. */
export function unavailableMutation<I = any, O = any>() {
  return async (_input: I): Promise<O> => {
    throw new Error('This feature is not yet available on the backend.');
  };
}
