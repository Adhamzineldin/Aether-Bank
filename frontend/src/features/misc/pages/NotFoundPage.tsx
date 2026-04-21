import { Link } from 'react-router-dom';
import { Compass } from 'lucide-react';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';

export default function NotFoundPage() {
  return (
    <div className="grid min-h-[60vh] place-items-center text-center">
      <div className="space-y-4">
        <div className="mx-auto grid h-16 w-16 place-items-center rounded-full bg-muted text-muted-fg">
          <Compass className="h-7 w-7" />
        </div>
        <h1 className="text-4xl font-bold">Page not found</h1>
        <p className="text-muted-fg">The page you're looking for doesn't exist or has moved.</p>
        <Link to={ROUTES.dashboard}><Button>Go to dashboard</Button></Link>
      </div>
    </div>
  );
}

