import { Link } from 'react-router-dom';
import { Alert } from '@shared/ui/Alert';
import { PasswordInput } from '@shared/ui/PasswordInput';
import { Button } from '@shared/ui/Button';
import { FormField } from '@shared/ui/FormField';
import { ROUTES } from '@app/routes';

export default function ResetPasswordPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Reset password</h1>
      <Alert tone="info">Choose a new strong password for your account.</Alert>
      <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
        <FormField label="New password" htmlFor="pw" required>
          <PasswordInput id="pw" />
        </FormField>
        <FormField label="Confirm password" htmlFor="pw2" required>
          <PasswordInput id="pw2" />
        </FormField>
        <Button fullWidth type="submit">Reset password</Button>
      </form>
      <p className="text-center text-sm text-muted-fg">
        <Link to={ROUTES.login} className="text-primary hover:underline">Back to sign in</Link>
      </p>
    </div>
  );
}

