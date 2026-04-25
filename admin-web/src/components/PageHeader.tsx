import type { ReactNode } from 'react';

export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <header className="mb-6 rounded-[2rem] border border-white/70 bg-white/55 p-6 shadow-soft backdrop-blur">
      <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-moss">{eyebrow}</p>
          <h2 className="mt-2 font-display text-4xl text-ink">{title}</h2>
          <p className="mt-2 max-w-3xl text-sm leading-6 text-ink/65">{description}</p>
        </div>
        {action}
      </div>
    </header>
  );
}
