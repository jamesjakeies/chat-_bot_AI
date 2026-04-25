'use client';

import { useMemo, useState } from 'react';
import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { EmptyBlock, ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { formatDate } from '@/lib/format';
import type { AdminUser } from '@/lib/types';
import { useAdminData } from '@/lib/useAdminData';

export default function UsersPage() {
  return (
    <AdminGate>
      <UsersContent />
    </AdminGate>
  );
}

function UsersContent() {
  const [keyword, setKeyword] = useState('');
  const path = useMemo(() => {
    const params = new URLSearchParams();
    if (keyword.trim()) params.set('q', keyword.trim());
    const query = params.toString();
    return `/admin/users${query ? `?${query}` : ''}`;
  }, [keyword]);
  const { data, loading, error, reload } = useAdminData<AdminUser[]>(path);

  return (
    <>
      <PageHeader
        eyebrow="Users"
        title="Users"
        description="Review account age checks, minor status, membership tier, and soft-deletion state."
        action={<button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reload}>Refresh</button>}
      />
      <div className="mb-4 grid gap-3 rounded-[2rem] border border-white/70 bg-white/55 p-4 shadow-soft backdrop-blur md:grid-cols-2">
        <input
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="Search nickname, email, or phone"
          value={keyword}
        />
        <button className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-3 text-sm font-semibold text-ink/70" onClick={() => setKeyword('')}>
          Clear filters
        </button>
      </div>
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="overflow-hidden rounded-[2rem] border border-white/70 bg-white/60 shadow-soft backdrop-blur">
          <table className="w-full min-w-[920px] text-left text-sm">
            <thead className="bg-white/70 text-ink/60">
              <tr>
                <th className="px-5 py-4">User</th>
                <th className="px-5 py-4">Membership</th>
                <th className="px-5 py-4">Age</th>
                <th className="px-5 py-4">Guardian consent</th>
                <th className="px-5 py-4">Created</th>
                <th className="px-5 py-4">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink/5">
              {data.map((user) => (
                <tr key={user.id}>
                  <td className="px-5 py-4">
                    <p className="font-semibold">{user.nickname}</p>
                    <p className="text-xs text-ink/55">{user.email ?? user.phone ?? user.id}</p>
                  </td>
                  <td className="px-5 py-4"><StatusBadge value={user.membershipLevel} /></td>
                  <td className="px-5 py-4">
                    <div className="flex gap-2">
                      <StatusBadge value={user.isMinor ? 'MINOR' : 'ADULT'} />
                      <StatusBadge value={user.ageVerified ? 'VERIFIED' : 'UNVERIFIED'} />
                    </div>
                  </td>
                  <td className="px-5 py-4"><StatusBadge value={user.guardianConsent} /></td>
                  <td className="px-5 py-4">{formatDate(user.createdAt)}</td>
                  <td className="px-5 py-4"><StatusBadge value={user.deletedAt ? 'DELETED' : 'ACTIVE'} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyBlock title="No users found" description="Try clearing filters or refreshing the data." />
      ))}
    </>
  );
}
