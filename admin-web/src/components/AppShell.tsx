'use client';

import type { ReactNode } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { clearAuth } from '@/lib/api';
import { classNames } from '@/lib/format';

const navItems = [
  { href: '/', label: '总览' },
  { href: '/roles', label: '角色' },
  { href: '/users', label: '用户' },
  { href: '/safety-events', label: '安全事件' },
  { href: '/chat-logs', label: '聊天日志' },
  { href: '/reports', label: '举报' },
  { href: '/audit-logs', label: '审计日志' },
];

export function AppShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();

  return (
    <div className="min-h-screen px-5 py-6 md:px-8">
      <div className="mx-auto flex max-w-7xl gap-6">
        <aside className="hidden w-64 shrink-0 rounded-[2rem] border border-white/60 bg-white/55 p-5 shadow-soft backdrop-blur md:block">
          <div className="mb-8">
            <p className="text-sm font-semibold uppercase tracking-[0.28em] text-moss">Xinyu AI</p>
            <h1 className="mt-2 font-display text-3xl text-ink">心屿后台</h1>
          </div>
          <nav className="space-y-2">
            {navItems.map((item) => {
              const active = item.href === '/' ? pathname === '/' : pathname.startsWith(item.href);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={classNames(
                    'block rounded-2xl px-4 py-3 text-sm font-semibold transition',
                    active
                      ? 'bg-ink text-oat shadow-lg shadow-ink/10'
                      : 'text-ink/70 hover:bg-white hover:text-ink',
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
          <button
            className="mt-8 w-full rounded-2xl border border-ink/10 px-4 py-3 text-sm font-semibold text-ink/70 transition hover:bg-white"
            onClick={() => {
              clearAuth();
              router.push('/');
              router.refresh();
            }}
          >
            退出登录
          </button>
        </aside>
        <main className="min-w-0 flex-1">{children}</main>
      </div>
    </div>
  );
}
