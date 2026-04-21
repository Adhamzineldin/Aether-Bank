import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Trash2, UserPlus, Send, Users } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { PageHeader } from '@shared/ui/PageHeader';
import { Card, CardContent } from '@shared/ui/Card';
import { Button } from '@shared/ui/Button';
import { Input } from '@shared/ui/Input';
import { Select } from '@shared/ui/Select';
import { FormField } from '@shared/ui/FormField';
import { EmptyState } from '@shared/ui/EmptyState';
import { Modal } from '@shared/ui/Modal';
import { useDisclosure } from '@shared/hooks/useDisclosure';
import { useBeneficiariesStore } from '@stores/beneficiariesStore';
import { CURRENCIES } from '@shared/constants/enums';
import { ROUTES } from '@app/routes';

const schema = z.object({
  nickname: z.string().min(1),
  accountNumber: z.string().min(4),
  accountId: z.string().uuid().optional().or(z.literal('')),
  bankName: z.string().optional(),
  currency: z.string().min(3),
});
type V = z.infer<typeof schema>;

export default function BeneficiariesPage() {
  const items = useBeneficiariesStore((s) => s.items);
  const add = useBeneficiariesStore((s) => s.add);
  const remove = useBeneficiariesStore((s) => s.remove);
  const dialog = useDisclosure();
  const navigate = useNavigate();
  const [editing, setEditing] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<V>({
    resolver: zodResolver(schema),
    defaultValues: { currency: 'USD' },
  });

  const submit = (v: V) => {
    add({ ...v, accountId: v.accountId || undefined });
    reset();
    dialog.close();
  };

  return (
    <div>
      <PageHeader
        title="Beneficiaries"
        description="Save people you frequently transfer money to."
        actions={<Button leftIcon={<UserPlus className="h-4 w-4" />} onClick={dialog.open}>Add beneficiary</Button>}
      />
      <Card>
        <CardContent>
          {items.length === 0 ? (
            <EmptyState
              icon={<Users className="h-5 w-5" />}
              title="No beneficiaries yet"
              description="Add the people you transfer to often for one-tap sending."
              action={<Button onClick={dialog.open}>Add beneficiary</Button>}
            />
          ) : (
            <ul className="divide-y divide-border">
              {items.map((b) => (
                <li key={b.id} className="flex items-center justify-between gap-3 py-3">
                  <div>
                    <p className="font-semibold">{b.nickname}</p>
                    <p className="text-sm text-muted-fg font-mono">{b.accountNumber} · {b.currency}{b.bankName ? ` · ${b.bankName}` : ''}</p>
                  </div>
                  <div className="flex gap-2">
                    {b.accountId && (
                      <Button size="sm" leftIcon={<Send className="h-4 w-4" />}
                        onClick={() => navigate(`${ROUTES.transfer}?dest=${b.accountId}`)}
                      >
                        Send
                      </Button>
                    )}
                    <Button size="sm" variant="outline" leftIcon={<Trash2 className="h-4 w-4" />} onClick={() => remove(b.id)}>
                      Remove
                    </Button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <Modal open={dialog.isOpen} onClose={dialog.close} title={editing ? 'Edit beneficiary' : 'Add beneficiary'}>
        <form className="space-y-3" onSubmit={handleSubmit(submit)}>
          <FormField label="Nickname" required error={errors.nickname?.message}><Input {...register('nickname')} /></FormField>
          <FormField label="Account number" required error={errors.accountNumber?.message}><Input {...register('accountNumber')} /></FormField>
          <FormField label="Account ID (UUID)" hint="Required for in-bank transfers" error={errors.accountId?.message}>
            <Input {...register('accountId')} />
          </FormField>
          <div className="grid grid-cols-2 gap-3">
            <FormField label="Bank name"><Input {...register('bankName')} /></FormField>
            <FormField label="Currency" required>
              <Select {...register('currency')}>{CURRENCIES.map((c) => <option key={c}>{c}</option>)}</Select>
            </FormField>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" onClick={dialog.close}>Cancel</Button>
            <Button type="submit">Save</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}

