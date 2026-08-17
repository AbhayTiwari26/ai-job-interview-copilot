/** Circular score gauge — SVG-based with color coding */
export default function ScoreGauge({ score, size = 120, label }) {
  const max = typeof score === 'number' && score <= 10 ? 10 : 100;
  const pct = Math.max(0, Math.min(score ?? 0, max)) / max;
  const radius = (size - 16) / 2;
  const circumference = 2 * Math.PI * radius;
  const strokeDash = pct * circumference;

  const color =
    pct >= 0.8 ? '#10b981' :
    pct >= 0.6 ? '#6366f1' :
    pct >= 0.4 ? '#f59e0b' : '#ef4444';

  const displayScore = max === 10 ? `${score ?? 0}/10` : `${score ?? 0}%`;

  return (
    <div className="flex flex-col items-center gap-2">
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size/2} cy={size/2} r={radius}
          fill="none" stroke="#1a1a27" strokeWidth="8" />
        <circle cx={size/2} cy={size/2} r={radius}
          fill="none" stroke={color} strokeWidth="8"
          strokeDasharray={`${strokeDash} ${circumference - strokeDash}`}
          strokeLinecap="round"
          style={{ transition: 'stroke-dasharray 1s ease-in-out' }} />
      </svg>
      <div className="absolute" style={{ marginTop: -(size/2 + 12) }}>
        <p className="text-2xl font-bold text-white text-center"
           style={{ color, lineHeight: 1 }}>
          {displayScore}
        </p>
      </div>
      {label && <p className="text-xs text-slate-400 text-center font-medium">{label}</p>}
    </div>
  );
}

/** Inline score badge for category scores */
export function ScoreBar({ label, value, max = 100 }) {
  const pct = Math.max(0, Math.min(value ?? 0, max)) / max * 100;
  const color = pct >= 80 ? 'bg-emerald-500' : pct >= 60 ? 'bg-primary-500' : pct >= 40 ? 'bg-amber-500' : 'bg-red-500';
  return (
    <div>
      <div className="flex justify-between mb-1">
        <span className="text-sm text-slate-300">{label}</span>
        <span className="text-sm font-semibold text-slate-200">{value ?? 0}{max === 10 ? '/10' : '%'}</span>
      </div>
      <div className="h-2 bg-dark-600 rounded-full overflow-hidden">
        <div className={`h-full ${color} rounded-full transition-all duration-1000`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}
