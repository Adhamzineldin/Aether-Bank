import { cn } from '@shared/utils/cn';
import { formatPanGroups, maskCard } from '@shared/utils/mask';
import type { CardSummary } from '@veld/types';

const networkGradient: Record<string, string> = {
  VISA: 'from-blue-700 via-blue-600 to-indigo-600',
  MASTERCARD: 'from-orange-600 via-rose-600 to-red-700',
  AMEX: 'from-emerald-700 via-teal-600 to-cyan-700',
};

export function CardVisual({
  card,
  /** When set, show this full number (already formatted); otherwise masked last four. */
  revealedPanDigits,
  className,
}: {
  card: CardSummary;
  revealedPanDigits?: string | null;
  className?: string;
}) {
  const gradient = networkGradient[card.cardNetwork] || 'from-slate-800 to-slate-600';
  const numberLine = revealedPanDigits
    ? formatPanGroups(revealedPanDigits)
    : maskCard(card.lastFourDigits);
  return (
    <div
      className={cn(
        'relative aspect-[1.586/1] w-full max-w-sm overflow-hidden rounded-2xl p-5 text-white shadow-soft',
        'bg-gradient-to-br',
        gradient,
        className,
      )}
    >
      <div className="absolute inset-0 opacity-30 [background:radial-gradient(circle_at_70%_30%,white_0%,transparent_55%)]" />
      <div className="relative flex h-full flex-col justify-between">
        <div className="flex items-center justify-between">
          <span className="text-xs uppercase tracking-widest opacity-80">{card.cardType}</span>
          <span className="text-sm font-bold tracking-wider">{card.cardNetwork}</span>
        </div>
        <div className="space-y-3">
          <p className="font-mono text-lg tracking-wide">{numberLine}</p>
          <div className="flex items-end justify-between text-xs">
            <div>
              <p className="opacity-70">VALID THRU</p>
              <p className="font-mono">{String(card.expiryMonth).padStart(2, '0')}/{String(card.expiryYear).slice(-2)}</p>
            </div>
            <span className="rounded bg-white/20 px-2 py-0.5 text-[10px] font-medium uppercase">{card.status}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

