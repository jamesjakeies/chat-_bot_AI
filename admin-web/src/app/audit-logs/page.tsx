'use client';

import { useMemo, useState } from 'react';
import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { EmptyBlock, ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { compactJson, formatDate, truncate } from '@/lib/format';
import type { AdminAuditLog } from '@/lib/types';
import { useAdminData } from '@/lib/useAdminData';

export default function AuditLogsPage() {
  return (
    <AdminGate>
      <AuditLogsContent />
    </AdminGate>
  );
}

function AuditLogsContent() {
  const [action, setAction] = useState('');
  const [targetType, setTargetType] = useState('');
  const path = useMemo(() => {
    const params = new URLSearchParams();
    if (action) params.set('action', action);
    if (targetType) params.set('targetType', targetType);
    const query = params.toString();
    return `/admin/audit-logs${query ? `?${query}` : ''}`;
  }, [action, targetType]);
  const { data, loading, error, reload } = useAdminData<AdminAuditLog[]>(path);

  return (
    <>
      <PageHeader
        eyebrow="Audit"
        title="Audit logs"
        description="Track key admin actions such as role status changes and report handling."
        action={<button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reload}>Refresh</button>}
      />
      <div className="mb-4 grid gap-3 rounded-[2rem] border border-white/70 bg-white/55 p-4 shadow-soft backdrop-blur md:grid-cols-3">
        <input
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setAction(event.target.value.trim())}
          placeholder="按 action 筛选，如 REPORT_STATUS_UPDATE"
          value={action}
        />
        <input
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setTargetType(event.target.value.trim())}
          placeholder="按 targetType 筛选，如 REPORT"
          value={targetType}
        />
        <button className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-3 text-sm font-semibold text-ink/70" onClick={() => {
          setAction('');
          setTargetType('');
        }}>
          清空筛选
        </button>
      </div>
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="grid gap-4">
          {data.map((log) => (
            <article key={log.id} className="rounded-[2rem] border border-white/70 bg-white/60 p-5 shadow-soft backdrop-blur">
              <div className="flex flex-wrap items-center gap-2">
                <StatusBadge value={log.action} />
                <StatusBadge value={log.targetType} />
                {log.targetId && <span className="text-xs text-ink/45">{log.targetId}</span>}
                <span className="text-sm text-ink/50">{formatDate(log.createdAt)}</span>
              </div>
              <p className="mt-2 text-sm text-ink/60">
                操作人：{log.adminUser.nickname} / {log.adminUser.email ?? log.adminUser.id}
              </p>
              <p className="mt-3 rounded-2xl bg-white/70 p-4 font-mono text-xs leading-6 text-ink/70">
                {truncate(compactJson(log.metadata), 420)}
              </p>
            </article>
          ))}
        </div>
      ) : <EmptyBlock text="暂无审计日志" />)}
    </>
  );
}
