import { type ReactNode } from 'react';
import { useTheme } from '@shared/hooks/useTheme';

/** Applies the persisted theme to <html>. */
export function ThemeProvider({ children }: { children: ReactNode }) {
  useTheme();
  return <>{children}</>;
}

