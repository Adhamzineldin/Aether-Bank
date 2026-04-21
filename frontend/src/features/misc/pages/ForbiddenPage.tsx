import { Link } from 'react-router-dom';
import { ShieldX } from 'lucide-react';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';

export default function ForbiddenPage() {
  return (
    <div className="grid min-h-[60vh] place-items-center text-center">
      <div className="space-y-4">
        <div className="mx-auto grid h-16 w-16 place-items-center rounded-full bg-danger/10 text-danger">
          <ShieldX className="h-7 w-7" />
        </div>
        <h1 className="text-4xl font-bold">Access denied</h1>
        <p className="text-muted-fg">You don't have permission to view this resource.</p>
        <Link to={ROUTES.dashboard}><Button>Back to dashboard</Button></Link>
      </div>
    </div>
  );
}

