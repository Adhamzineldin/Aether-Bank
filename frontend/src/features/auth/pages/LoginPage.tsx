import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { LogIn, Lock, User } from 'lucide-react';
import { Input } from '@shared/ui/Input';
import { PasswordInput } from '@shared/ui/PasswordInput';
import { FormField } from '@shared/ui/FormField';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';
import { loginSchema, type LoginValues } from '../schemas';
import { useLogin } from '../hooks';

export default function LoginPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { userName: '', password: '' },
  });
  const login = useLogin();

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <h1 className="text-3xl font-bold">Welcome back</h1>
        <p className="text-sm text-muted-fg">Sign in to your Aether Bank account.</p>
      </header>
      <form onSubmit={handleSubmit((v) => login.mutate(v))} className="space-y-4">
        <FormField label="Username" htmlFor="userName" error={errors.userName?.message} required>
          <Input id="userName" leftIcon={<User className="h-4 w-4" />} placeholder="jane.doe" {...register('userName')} />
        </FormField>
        <FormField label="Password" htmlFor="password" error={errors.password?.message} required>
          <PasswordInput id="password" leftIcon={<Lock className="h-4 w-4" />} placeholder="••••••••" {...register('password')} />
        </FormField>
        <div className="flex justify-end">
          <Link to={ROUTES.forgot} className="text-xs text-primary hover:underline">Forgot password?</Link>
        </div>
        <Button type="submit" fullWidth loading={login.isPending} leftIcon={<LogIn className="h-4 w-4" />}>
          Sign in
        </Button>
      </form>
      <p className="text-center text-sm text-muted-fg">
        Don't have an account? <Link to={ROUTES.register} className="font-medium text-primary hover:underline">Create one</Link>
      </p>
    </div>
  );
}

