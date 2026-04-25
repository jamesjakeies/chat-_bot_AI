'use client';

import Link from 'next/link';
import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { useAdminData } from '@/lib/useAdminData';
import type { AdminReport, AdminRole, AdminSafetyEvent, AdminUser } from '@/lib/types';

export default function DashboardPage() {
  return (
    <AdminGate>
      <DashboardContent />
    </AdminGate>
  );
}

function DashboardContent() {
  const users = useAdminData<AdminUser[]>('/admin/users');
  const roles = useAdminData<AdminRole[]>('/admin/roles');
  const safetyEvents = useAdminData<AdminSafetyEvent[]>('/admin/safety-events');
  const reports = useAdminData<AdminReport[]>('/admin/reports');
  const loading = users.loading || roles.loading || safetyEvents.loading || reports.loading;
  const error = users.error ?? roles.error ?? safetyEvents.error ?? reports.error;

  function reloadAll() {
    void users.reload();
    void roles.reload();
    void safetyEvents.reload();
    void reports.reload();
  }

  return (
    <>
      <PageHeader
        eyebrow="Operations"
        title="Daily console"
        description="Review safety first, then content. The dashboard keeps safety events, reports, and role status in one operational view."
        action={
          <button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reloadAll}>
            Refresh
          </button>
        }
      />
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reloadAll} />}
      {!loading && !error && (
        <div className="grid gap-4 md:grid-cols-4">
          <MetricCard label="Users" value={users.data?.length ?? 0} href="/users" />
          <MetricCard label="Roles" value={roles.data?.length ?? 0} href="/roles" />
          <MetricCard label="Safety events" value={safetyEvents.data?.length ?? 0} href="/safety-events" />
          <MetricCard label="Reports" value={reports.data?.length ?? 0} href="/reports" />
          <section className="rounded-[2rem] border border-white/70 bg-white/55 p-6 shadow-soft backdrop-blur md:col-span-2">
            <h3 className="font-display text-2xl">Recent safety events</h3>
            <div className="mt-4 space-y-3">
              {(safetyEvents.data ?? []).slice(0, 5).map((event) => (
                <div key={event.id} className="rounded-2xl bg-white/70 p-4">
                  <div className="flex items-center justify-between gap-3">
                    <span className="font-semibold">{event.eventType}</span>
                    <StatusBadge value={event.riskLevel} />
                  </div>
                  <p className="mt-2 text-sm text-ink/65">{event.message?.content ?? 'No message content'}</p>
                </div>
              ))}
            </div>
          </section>
          <section className="rounded-[2rem] border border-white/70 bg-white/55 p-6 shadow-soft backdrop-blur md:col-span-2">
            <h3 className="font-display text-2xl">Pending reports</h3>
            <div className="mt-4 space-y-3">
              {(reports.data ?? []).slice(0, 5).map((report) => (
                <div key={report.id} className="rounded-2xl bg-white/70 p-4">
                  <div className="flex items-center justify-between gap-3">
                    <span className="font-semibold">{report.reason}</span>
                    <StatusBadge value={report.status} />
                  </div>
                  <p className="mt-2 text-sm text-ink/65">{report.message.content}</p>
                </div>
              ))}
            </div>
          </section>
        </div>
      )}
    </>
  );
}

function MetricCard({ label, value, href }: { label: string; value: number; href: string }) {
  return (
    <Link
      href={href}
      className="rounded-[2rem] border border-white/70 bg-white/60 p-6 shadow-soft backdrop-blur transition hover:-translate-y-0.5 hover:bg-white/80"
    >
      <p className="text-sm font-semibold text-ink/55">{label}</p>
      <p className="mt-3 font-display text-5xl text-ink">{value}</p>
    </Link>
  );
}
