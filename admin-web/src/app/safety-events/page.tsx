'use client';

import { useMemo, useState } from 'react';
import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { EmptyBlock, ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { formatDate, truncate } from '@/lib/format';
import type { AdminSafetyEvent } from '@/lib/types';
import { useAdminData } from '@/lib/useAdminData';

const riskLevels = ['NORMAL', 'ATTENTION', 'CRISIS'] as const;
const eventTypes = [
  'SELF_HARM_LOW',
  'SELF_HARM_HIGH',
  'VIOLENCE',
  'ILLEGAL',
  'SEXUAL',
  'MINOR_ROMANTIC',
  'DEPENDENCY_RISK',
  'NORMAL',
] as const;

export default function SafetyEventsPage() {
  return (
    <AdminGate>
      <SafetyEventsContent />
    </AdminGate>
  );
}

function SafetyEventsContent() {
  const [riskLevel, setRiskLevel] = useState('');
  const [eventType, setEventType] = useState('');
  const path = useMemo(() => {
    const params = new URLSearchParams();
    if (riskLevel) params.set('riskLevel', riskLevel);
    if (eventType) params.set('eventType', eventType);
    const query = params.toString();
    return `/admin/safety-events${query ? `?${query}` : ''}`;
  }, [eventType, riskLevel]);
  const { data, loading, error, reload } = useAdminData<AdminSafetyEvent[]>(path);

  return (
    <>
      <PageHeader
        eyebrow="Safety"
        title="Safety events"
        description="Track high-risk content, minor restrictions, illegal requests, and dependency risks."
        action={<button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reload}>Refresh</button>}
      />
      <div className="mb-4 grid gap-3 rounded-[2rem] border border-white/70 bg-white/55 p-4 shadow-soft backdrop-blur md:grid-cols-3">
        <select
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setRiskLevel(event.target.value)}
          value={riskLevel}
        >
          <option value="">全部风险等级</option>
          {riskLevels.map((level) => (
            <option key={level} value={level}>{level}</option>
          ))}
        </select>
        <select
          className="rounded-2xl border border-ink/10 bg-white/80 px-4 py-3 text-sm outline-none focus:border-lagoon"
          onChange={(event) => setEventType(event.target.value)}
          value={eventType}
        >
          <option value="">全部事件类型</option>
          {eventTypes.map((type) => (
            <option key={type} value={type}>{type}</option>
          ))}
        </select>
        <button className="rounded-2xl border border-ink/10 bg-white/70 px-4 py-3 text-sm font-semibold text-ink/70" onClick={() => {
          setRiskLevel('');
          setEventType('');
        }}>
          清空筛选
        </button>
      </div>
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="grid gap-4">
          {data.map((event) => (
            <article key={event.id} className="rounded-[2rem] border border-white/70 bg-white/60 p-5 shadow-soft backdrop-blur">
              <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                <div>
                  <div className="flex flex-wrap items-center gap-2">
                    <h3 className="font-semibold">{event.eventType}</h3>
                    <StatusBadge value={event.riskLevel} />
                    <StatusBadge value={event.actionTaken} />
                  </div>
                  <p className="mt-2 text-sm text-ink/60">
                    用户：{event.user?.nickname ?? event.user?.id ?? '-'} / 会话：{event.session?.title ?? '-'} / {formatDate(event.createdAt)}
                  </p>
                  <p className="mt-3 rounded-2xl bg-white/70 p-4 text-sm leading-6 text-ink/75">
                    {truncate(event.message?.content ?? '无消息内容', 240)}
                  </p>
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : <EmptyBlock text="暂无安全事件" />)}
    </>
  );
}
