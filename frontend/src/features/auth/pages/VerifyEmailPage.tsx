import { Link } from 'react-router-dom';
import { CheckCircle2 } from 'lucide-react';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';

export default function VerifyEmailPage() {
  return (
    <div className="space-y-6 text-center">
      <div className="mx-auto grid h-16 w-16 place-items-center rounded-full bg-success/10 text-success-600">
        <CheckCircle2 className="h-8 w-8" />
      </div>
      <h1 className="text-3xl font-bold">Email verified</h1>
      <p className="text-sm text-muted-fg">Your email has been confirmed. You can now sign in to your account.</p>
      <Button onClick={() => (window.location.href = ROUTES.login)}>Continue to sign in</Button>
      <p className="text-center text-sm text-muted-fg">
        <Link to="/" className="text-primary hover:underline">Back to home</Link>
      </p>
    </div>
  );
}


