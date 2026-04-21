import { useQuery, type UseQueryResult } from '@tanstack/react-query';

/**
 * Returns a react-query result that resolves to `undefined` after a microtask
 * without hitting the network. Use this for feature hooks whose backend REST
 * controllers do not exist yet (card/financial/notification/transaction
 * services currently have no REST layer — they are RabbitMQ-driven).
 *
 * The surrounding UI typically treats `undefined` / empty arrays as "no data"
 * and shows an EmptyState, so the user experience stays clean.
 */
export function useStubQuery<T = undefined>(key: readonly unknown[], value?: T): UseQueryResult<T> {
  return useQuery({
    queryKey: ['stub', ...key],
    queryFn: async () => (value as T) ?? (undefined as unknown as T),
    staleTime: Infinity,
    refetchOnMount: false,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
}

/** Mutation factory for unavailable features — returns a no-op mutation shape. */
export function unavailableMutation<I = unknown, O = void>() {
  return async (_input: I): Promise<O> => {
    throw new Error('This feature is not yet available on the backend.');
  };
}

