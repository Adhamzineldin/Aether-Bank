import { QueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { extractErrorMessage } from './errors';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      gcTime: 5 * 60_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      onError: (err: unknown) => {
        toast.error(extractErrorMessage(err));
      },
    },
  },
});

