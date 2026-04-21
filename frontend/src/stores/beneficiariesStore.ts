import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface Beneficiary {
  id: string;
  nickname: string;
  accountNumber: string;
  accountId?: string;
  bankName?: string;
  currency: string;
  createdAt: string;
}

interface State {
  items: Beneficiary[];
  add: (b: Omit<Beneficiary, 'id' | 'createdAt'>) => void;
  remove: (id: string) => void;
  update: (id: string, patch: Partial<Beneficiary>) => void;
}

export const useBeneficiariesStore = create<State>()(
  persist(
    (set) => ({
      items: [],
      add: (b) =>
        set((s) => ({
          items: [
            ...s.items,
            { ...b, id: crypto.randomUUID(), createdAt: new Date().toISOString() },
          ],
        })),
      remove: (id) => set((s) => ({ items: s.items.filter((x) => x.id !== id) })),
      update: (id, patch) =>
        set((s) => ({ items: s.items.map((x) => (x.id === id ? { ...x, ...patch } : x)) })),
    }),
    { name: 'aether-beneficiaries' },
  ),
);

