import { RouterProvider } from 'react-router-dom';
import { QueryProvider } from '@app/providers/QueryProvider';
import { ThemeProvider } from '@app/providers/ThemeProvider';
import { ToastProvider } from '@app/providers/ToastProvider';
import { ErrorBoundary } from '@shared/ui/ErrorBoundary';
import { router } from './router';

export function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <QueryProvider>
          <RouterProvider router={router} />
          <ToastProvider />
        </QueryProvider>
      </ThemeProvider>
    </ErrorBoundary>
  );
}

