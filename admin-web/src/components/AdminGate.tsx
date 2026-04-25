'use client';

import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { getAccessToken } from '@/lib/api';
import { AppShell } from './AppShell';
import { LoginPanel } from './LoginPanel';

export function AdminGate({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    setAuthenticated(Boolean(getAccessToken()));
    setReady(true);
  }, []);

  if (!ready) {
    return <div className="p-8 text-sm text-ink/60">正在初始化后台...</div>;
  }

  if (!authenticated) {
    return <LoginPanel onLoggedIn={() => setAuthenticated(true)} />;
  }

  return <AppShell>{children}</AppShell>;
}
