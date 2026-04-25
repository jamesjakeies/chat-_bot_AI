'use client';

import { AdminGate } from '@/components/AdminGate';
import { PageHeader } from '@/components/PageHeader';
import { EmptyBlock, ErrorBlock, LoadingBlock } from '@/components/StateBlock';
import { StatusBadge } from '@/components/StatusBadge';
import { formatDate, truncate } from '@/lib/format';
import type { AdminChatLog } from '@/lib/types';
import { useAdminData } from '@/lib/useAdminData';

export default function ChatLogsPage() {
  return (
    <AdminGate>
      <ChatLogsContent />
    </AdminGate>
  );
}

function ChatLogsContent() {
  const { data, loading, error, reload } = useAdminData<AdminChatLog[]>('/admin/chat-logs');

  return (
    <>
      <PageHeader
        eyebrow="Chat Logs"
        title="Chat logs"
        description="Use this view to investigate safety, reports, and role reply issues."
        action={<button className="rounded-2xl bg-ink px-5 py-3 text-sm font-semibold text-oat" onClick={reload}>Refresh</button>}
      />
      {loading && <LoadingBlock />}
      {error && <ErrorBlock message={error} onRetry={reload} />}
      {!loading && !error && (data?.length ? (
        <div className="grid gap-4">
          {data.map((message) => (
            <article key={message.id} className="rounded-[2rem] border border-white/70 bg-white/60 p-5 shadow-soft backdrop-blur">
              <div className="flex flex-wrap items-center gap-2">
                <StatusBadge value={message.senderType} />
                <StatusBadge value={message.safetyLabel ?? 'NORMAL'} />
                {message.reports.length > 0 && <StatusBadge value={`${message.reports.length} REPORTS`} />}
                <span className="text-sm text-ink/50">{formatDate(message.createdAt)}</span>
              </div>
              <p className="mt-3 text-sm text-ink/60">
                {message.session.user.nickname} / {message.session.role.name} / {message.session.title}
              </p>
              <p className="mt-3 rounded-2xl bg-white/70 p-4 text-sm leading-6 text-ink/75">
                {truncate(message.content, 300)}
              </p>
            </article>
          ))}
        </div>
      ) : <EmptyBlock text="暂无聊天日志" />)}
    </>
  );
}
