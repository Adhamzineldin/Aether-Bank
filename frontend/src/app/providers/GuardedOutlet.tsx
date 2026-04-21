import { Outlet } from 'react-router-dom';
import { AuthGuard } from './AuthGuard';

interface Props { roles?: string[] }

/** Layout-level auth guard that renders nested routes via <Outlet/>. */
export function GuardedOutlet({ roles }: Props) {
  return (
    <AuthGuard roles={roles}>
      <Outlet />
    </AuthGuard>
  );
}

