import { useEffect } from 'react';
import { useUIStore } from '@stores/uiStore';

/** Applies the current theme class to the <html> element. */
export function useTheme() {
  const theme = useUIStore((s) => s.theme);
  const setTheme = useUIStore((s) => s.setTheme);
  const toggle = useUIStore((s) => s.toggleTheme);

  useEffect(() => {
    const root = document.documentElement;
    root.classList.toggle('dark', theme === 'dark');
    root.style.colorScheme = theme;
  }, [theme]);

  return { theme, setTheme, toggle };
}

