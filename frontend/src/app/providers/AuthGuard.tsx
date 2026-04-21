import { Navigate, useLocation } from 'react-router-dom';
import { type ReactNode } from 'react';
import { useAuthStore } from '@stores/authStore';
import { ROUTES } from '@app/routes';

interface Props {
  children: ReactNode;
  roles?: string[];
}

export function AuthGuard({ children, roles }: Props) {
  const { accessToken, hasRole } = useAuthStore();
  const location = useLocation();

  if (!accessToken) {
    const next = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`${ROUTES.login}?next=${next}`} replace />;
  }
  if (roles && roles.length && !hasRole(...roles)) {
    return <Navigate to={ROUTES.forbidden} replace />;
  }
  return <>{children}</>;
}

