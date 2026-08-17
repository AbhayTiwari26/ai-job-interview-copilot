export default function ErrorMessage({ message, onRetry }) {
  if (!message) return null;
  return (
    <div className="flex items-start gap-3 p-4 bg-red-500/10 border border-red-500/20 rounded-xl animate-fade-in">
      <span className="text-red-400 text-lg mt-0.5">⚠</span>
      <div className="flex-1">
        <p className="text-red-400 text-sm">{message}</p>
        {onRetry && (
          <button onClick={onRetry} className="mt-2 text-xs text-red-300 underline hover:text-red-200">
            Try again
          </button>
        )}
      </div>
    </div>
  );
}
