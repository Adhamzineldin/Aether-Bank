import { Link } from 'react-router-dom';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { FormField } from '@shared/ui/FormField';
import { Alert } from '@shared/ui/Alert';
import { ROUTES } from '@app/routes';

export default function MfaPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Two-factor verification</h1>
      <Alert tone="info">Enter the 6-digit code from your authenticator app.</Alert>
      <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
        <FormField label="Authentication code" htmlFor="code" required>
          <Input id="code" inputMode="numeric" maxLength={6} placeholder="••••••" className="text-center tracking-[0.5em] text-lg" />
        </FormField>
        <Button fullWidth type="submit">Verify</Button>
      </form>
      <p className="text-center text-sm text-muted-fg">
        <Link to={ROUTES.login} className="text-primary hover:underline">Back to sign in</Link>
      </p>
    </div>
  );
}

