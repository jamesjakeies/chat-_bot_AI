'use client';

import { useMemo, useState } from 'react';
import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { EmptyBlock, ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { adminPatch } from '@/lib/api';
import { formatDate, truncate } from '@/lib/format';
import type { AdminReport } from '@/lib/types';
import { useAdminData } from '@/lib/useAdminData';

const reportStatuses = ['PENDING', 'REVIEWED', 'RESOLVED', 'REJECTED'] as const;

export default function ReportsPage() {
  return (
    <AdminGate>
      <ReportsContent />
    </AdminGate>
  );
}

function ReportsContent() {
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const [keyword, setKeyword] = useState('');
  const [notes, setNotes] = useState<Record<string, string>>({});
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const path = useMemo(() => {
    const params = new URLSearchParams();
    if (statusFilter) params.set('status', statusFilter);
    if (keyword.trim()) params.set('q', keyword.trim());
    const query = params.toString();
    return `/admin/reports${query ? `?${query}` : ''}`;
  }, [keyword, statusFilter]);
  const { data, loading, error, reload } = useAdminData<AdminReport[]>(path);

  async function updateReportStatus(reportId: string, status: string) {
    setUpdatingId(reportId);
    setActionError(null);
    try {
      await adminPatch(`/admin/reports/${reportId}/status`, {
        status,
        reviewNote: notes[reportId] || undefined,
      });
      await reload();
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Report status update failed.');
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <>
      <PageHeader
        eyebrow="Reports"
        title="User reports"
        description="Review reports submitted from chat, add review notes, and update handling status."
        action={<button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reload}>Refresh</button>}
      />
      <div className="mb-4 grid gap-3 rounded-[2rem] border border-white/70 bg-white/55 p-4 shadow-soft backdrop-blur md:grid-cols-3">
        <input
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setKeyword(event.target.value)}
          placeholder="Search report reason, note, or message"
          value={keyword}
        />
        <select
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setStatusFilter(event.target.value)}
          value={statusFilter}
        >
          <option value="">All statuses</option>
          {reportStatuses.map((status) => (
            <option key={status} value={status}>{status}</option>
          ))}
        </select>
        <button className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-3 text-sm font-semibold text-ink/70" onClick={() => {
          setKeyword('');
          setStatusFilter('');
        }}>
          Clear filters
        </button>
      </div>
      {actionError && <div className="mb-4 rounded-2xl bg-red-50 px-4 py-3 text-sm text-red-700">{actionError}</div>}
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="grid gap-4">
          {data.map((report) => (
            <article key={report.id} className="rounded-[2rem] border border-white/70 bg-white/60 p-5 shadow-soft backdrop-blur">
              <div className="flex flex-wrap items-center gap-2">
                <h3 className="font-semibold">{report.reason}</h3>
                <StatusBadge value={report.status} />
                <StatusBadge value={report.message.safetyLabel ?? 'NORMAL'} />
              </div>
              <p className="mt-2 text-sm text-ink/60">
                Reporter: {report.user.nickname} / {formatDate(report.createdAt)}
                {report.reviewedAt ? ` / Reviewed: ${formatDate(report.reviewedAt)}` : ''}
              </p>
              {report.reviewNote && (
                <p className="mt-3 rounded-2xl bg-oat/70 p-3 text-sm text-ink/70">
                  Note: {report.reviewNote}
                </p>
              )}
              <p className="mt-3 rounded-2xl bg-white/70 p-4 text-sm leading-6 text-ink/75">
                {truncate(report.message.content, 300)}
              </p>
              <div className="mt-4 grid gap-3 md:grid-cols-[1fr_auto]">
                <input
                  className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
                  onChange={(event) => setNotes((current) => ({ ...current, [report.id]: event.target.value }))}
                  placeholder="Review note (optional)"
                  value={notes[report.id] ?? ''}
                />
                <div className="flex flex-wrap gap-2">
                  {reportStatuses.map((status) => (
                    <button
                      key={status}
                      className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-2 text-sm font-semibold text-ink/70 transition hover:bg-ink hover:text-oat disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={updatingId === report.id || report.status === status}
                      onClick={() => void updateReportStatus(report.id, status)}
                    >
                      {status}
                    </button>
                  ))}
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : (
        <EmptyBlock title="No reports found" description="Try clearing filters or refreshing the data." />
      ))}
    </>
  );
}
