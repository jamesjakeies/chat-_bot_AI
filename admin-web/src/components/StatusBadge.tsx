import { classNames } from '@/lib/format';

const toneByValue: Record<string, string> = {
  ACTIVE: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  FREE: 'bg-stone-100 text-stone-700 ring-stone-200',
  MONTHLY: 'bg-lagoon/15 text-lagoon ring-lagoon/20',
  PREMIUM: 'bg-clay/15 text-clay ring-clay/20',
  CRISIS: 'bg-red-100 text-red-800 ring-red-200',
  ATTENTION: 'bg-amber-100 text-amber-800 ring-amber-200',
  BLOCKED: 'bg-red-100 text-red-800 ring-red-200',
  ARCHIVED: 'bg-stone-200 text-stone-700 ring-stone-300',
  PENDING: 'bg-amber-100 text-amber-800 ring-amber-200',
  REVIEWED: 'bg-lagoon/15 text-lagoon ring-lagoon/20',
  RESOLVED: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  REJECTED: 'bg-stone-200 text-stone-700 ring-stone-300',
  ADULT: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  MINOR: 'bg-amber-100 text-amber-800 ring-amber-200',
  VERIFIED: 'bg-emerald-100 text-emerald-800 ring-emerald-200',
  UNVERIFIED: 'bg-stone-100 text-stone-700 ring-stone-200',
  DELETED: 'bg-stone-200 text-stone-700 ring-stone-300',
};

export function StatusBadge({ value }: { value: string | boolean | null | undefined }) {
  const label = value === true ? 'YES' : value === false ? 'NO' : value ?? '-';
  const tone = toneByValue[String(label)] ?? 'bg-white/70 text-ink ring-black/10';

  return (
    <span
      className={classNames(
        'inline-flex items-center rounded-full px-2.5 py-1 text-xs font-semibold ring-1',
        tone,
      )}
    >
      {String(label)}
    </span>
  );
}
