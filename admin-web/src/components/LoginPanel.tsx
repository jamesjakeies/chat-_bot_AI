'use client';

import { FormEvent, useEffect, useState } from 'react';
import { defaultApiBaseUrl, getApiBaseUrl, login } from '@/lib/api';

export function LoginPanel({ onLoggedIn }: { onLoggedIn: () => void }) {
  const [apiBaseUrl, setApiBaseUrl] = useState(defaultApiBaseUrl());
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      setApiBaseUrl(getApiBaseUrl());
    }
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login({ apiBaseUrl, email, password });
      onLoggedIn();
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="mx-auto mt-16 max-w-xl rounded-[2rem] border border-white/70 bg-white/70 p-8 shadow-soft backdrop-blur">
      <p className="text-sm font-semibold uppercase tracking-[0.28em] text-moss">Admin Access</p>
      <h1 className="mt-3 font-display text-4xl text-ink">心屿 AI 管理后台</h1>
      <p className="mt-3 text-sm leading-6 text-ink/65">
        请使用后端账号登录。本地开发时，如果后端配置了 ADMIN_DEV_ALLOW_ALL=true，任意已登录账号都可以进入后台。
      </p>
      <form className="mt-8 space-y-4" onSubmit={handleSubmit}>
        <label className="block">
          <span className="text-sm font-semibold text-ink/75">后端 API 地址</span>
          <input
            className="mt-2 w-full rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 outline-none transition focus:border-lagoon"
            value={apiBaseUrl}
            onChange={(event) => setApiBaseUrl(event.target.value)}
            placeholder="http://localhost:3000"
          />
        </label>
        <label className="block">
          <span className="text-sm font-semibold text-ink/75">邮箱</span>
          <input
            className="mt-2 w-full rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 outline-none transition focus:border-lagoon"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="admin@example.com"
            type="email"
          />
        </label>
        <label className="block">
          <span className="text-sm font-semibold text-ink/75">密码</span>
          <input
            className="mt-2 w-full rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 outline-none transition focus:border-lagoon"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            type="password"
          />
        </label>
        {error && <p className="rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>}
        <button
          className="w-full rounded-2xl bg-ink px-5 py-3 font-semibold text-oat transition hover:bg-moss disabled:cursor-not-allowed disabled:opacity-60"
          disabled={loading}
          type="submit"
        >
          {loading ? '登录中...' : '进入后台'}
        </button>
      </form>
    </section>
  );
}
