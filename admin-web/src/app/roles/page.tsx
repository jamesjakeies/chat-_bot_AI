'use client';

import { useMemo, useState } from 'react';
import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { EmptyBlock, ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { adminPatch } from '@/lib/api';
import { formatDate } from '@/lib/format';
import type { AdminRole } from '@/lib/types';
import { useAdminData } from '@/lib/useAdminData';

const statuses = ['ACTIVE', 'BLOCKED', 'ARCHIVED'] as const;

export default function RolesPage() {
  return (
    <AdminGate>
      <RolesContent />
    </AdminGate>
  );
}

function RolesContent() {
  const [statusFilter, setStatusFilter] = useState('');
  const [keyword, setKeyword] = useState('');
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const path = useMemo(() => {
    const params = new URLSearchParams();
    if (statusFilter) params.set('status', statusFilter);
    if (keyword.trim()) params.set('q', keyword.trim());
    const query = params.toString();
    return `/admin/roles${query ? `?${query}` : ''}`;
  }, [keyword, statusFilter]);
  const { data, loading, error, reload } = useAdminData<AdminRole[]>(path);

  async function updateStatus(roleId: string, status: string) {
    setUpdatingId(roleId);
    setActionError(null);
    try {
      await adminPatch(`/admin/roles/${roleId}/status`, { status });
      await reload();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : '状态更新失败');
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Roles"
        title="Role management"
        description="Review official and user-created roles, and move roles between ACTIVE, BLOCKED, and ARCHIVED."
        action={<button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reload}>Refresh</button>}
      />
      <div className="mb-4 grid gap-3 rounded-[2rem] border border-white/70 bg-white/55 p-4 shadow-soft backdrop-blur md:grid-cols-3">
        <input
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="搜索角色名、人设或说话风格"
          value={keyword}
        />
        <select
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setStatusFilter(event.target.value)}
          value={statusFilter}
        >
          <option value="">全部状态</option>
          {statuses.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </select>
        <button className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-3 text-sm font-semibold text-ink/70" onClick={() => {
          setKeyword('');
          setStatusFilter('');
        }}>
          清空筛选
        </button>
      </div>
      {actionError && <div className="mb-4 rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-700">{actionError}</div>}
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="grid gap-4">
          {data.map((role) => (
            <article key={role.id} className="rounded-[2rem] border border-white/70 bg-white/60 p-5 shadow-soft backdrop-blur">
              <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="font-display text-2xl">{role.name}</h3>
                    <StatusBadge value={role.status} />
                    <StatusBadge value={role.isOfficial ? 'OFFICIAL' : 'CUSTOM'} />
                    {role.isAdultOnly && <StatusBadge value="18+" />}
                  </div>
                  <p className="mt-2 text-sm text-ink/60">
                    {role.category} / {role.relationshipType} / {role.safetyLevel}
                  </p>
                  <p className="mt-3 text-sm leading-6 text-ink/75">{role.personality}</p>
                  <p className="mt-1 text-sm leading-6 text-ink/60">{role.speechStyle}</p>
                  <p className="mt-3 text-xs text-ink/45">
                    创建：{formatDate(role.createdAt)} / 创建者：{role.createdByUser?.nickname ?? '官方'}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2 md:justify-end">
                  {statuses.map((status) => (
                    <button
                      key={status}
                      className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-2 text-sm font-semibold text-ink/70 transition hover:bg-ink hover:text-oat disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={updatingId === role.id || role.status === status}
                      onClick={() => void updateStatus(role.id, status)}
                    >
                      {status}
                    </button>
                  ))}
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : <EmptyBlock text="暂无角色数据" />)}
    </>
  );
}
