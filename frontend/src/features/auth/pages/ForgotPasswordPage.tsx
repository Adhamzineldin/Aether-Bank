import { Link } from 'react-router-dom';
import { Alert } from '@shared/ui/Alert';
import { Input } from '@shared/ui/Input';
import { Button } from '@shared/ui/Button';
import { FormField } from '@shared/ui/FormField';
import { ROUTES } from '@app/routes';

export default function ForgotPasswordPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Forgot password</h1>
      <Alert tone="info">
        Password recovery is currently handled by your bank administrator. Submit your email and we'll forward your request.
      </Alert>
      <form className="space-y-4" onSubmit={(e) => e.preventDefault()}>
        <FormField label="Email" htmlFor="email" required>
          <Input id="email" type="email" placeholder="you@example.com" />
        </FormField>
        <Button fullWidth type="submit">Send recovery link</Button>
      </form>
      <p className="text-center text-sm text-muted-fg">
        <Link to={ROUTES.login} className="text-primary hover:underline">Back to sign in</Link>
      </p>
    </div>
  );
}

