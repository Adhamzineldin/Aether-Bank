import { Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Mail, Lock, User } from 'lucide-react';
import { Input } from '@shared/ui/Input';
import { PasswordInput } from '@shared/ui/PasswordInput';
import { FormField } from '@shared/ui/FormField';
import { Button } from '@shared/ui/Button';
import { ROUTES } from '@app/routes';
import { registerSchema, type RegisterValues } from '../schemas';
import { useRegister } from '../hooks';

export default function RegisterPage() {
  const { register, handleSubmit, formState: { errors } } = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
  });
  const reg = useRegister();

  return (
    <div className="space-y-6">
      <header className="space-y-2">
        <h1 className="text-3xl font-bold">Create your account</h1>
        <p className="text-sm text-muted-fg">Join Aether Bank in less than a minute.</p>
      </header>
      <form onSubmit={handleSubmit((v) => reg.mutate(v))} className="space-y-4">
        <FormField label="Username" htmlFor="userName" error={errors.userName?.message} required>
          <Input id="userName" leftIcon={<User className="h-4 w-4" />} {...register('userName')} />
        </FormField>
        <FormField label="Email" htmlFor="email" error={errors.email?.message} required>
          <Input id="email" type="email" leftIcon={<Mail className="h-4 w-4" />} {...register('email')} />
        </FormField>
        <FormField label="Password" htmlFor="password" error={errors.password?.message} required>
          <PasswordInput id="password" leftIcon={<Lock className="h-4 w-4" />} {...register('password')} />
        </FormField>
        <Button type="submit" fullWidth loading={reg.isPending}>Create account</Button>
      </form>
      <p className="text-center text-sm text-muted-fg">
        Already have an account? <Link to={ROUTES.login} className="font-medium text-primary hover:underline">Sign in</Link>
      </p>
    </div>
  );
}

