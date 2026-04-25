export function LoadingBlock() {
  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/55 p-8 text-sm text-ink/60 shadow-soft">
      正在加载数据...
    </div>
  );
}

export function ErrorBlock({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="rounded-[2rem] border border-red-100 bg-red-50 p-8 text-sm text-red-800 shadow-soft">
      <p>{message}</p>
      <button
        className="mt-4 rounded-2xl bg-red-700 px-4 py-2 font-semibold text-white"
        onClick={onRetry}
      >
        重新加载
      </button>
    </div>
  );
}

export function EmptyBlock({ text }: { text: string }) {
  return (
    <div className="rounded-[2rem] border border-white/70 bg-white/55 p-8 text-sm text-ink/60 shadow-soft">
      {text}
    </div>
  );
}
