import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { Spinner } from '../components/Spinner';
import { ScoreBar } from '../components/ScoreGauge';
import { interviewService } from '../services/interviewService';

export default function InterviewResultsPage() {
  const { analysisId } = useParams();
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    interviewService.getByAnalysis(analysisId)
      .then(({ data }) => setInterviews(data.filter((i) => i.answer)))
      .finally(() => setLoading(false));
  }, [analysisId]);

  const avg = (key) => {
    const vals = interviews.map((i) => i.feedback?.[key]).filter((v) => v != null);
    return vals.length ? Math.round(vals.reduce((a, b) => a + b, 0) / vals.length * 10) / 10 : 0;
  };

  if (loading) return (
    <div className="min-h-screen bg-dark-900"><Navbar />
      <div className="flex justify-center py-32"><Spinner size="lg" /></div>
    </div>
  );

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="page-container animate-fade-in">
        <h1 className="page-title mb-2">Interview Results</h1>
        <p className="text-slate-400 text-sm mb-6">{interviews.length} questions answered</p>

        {/* Average scores */}
        <div className="card mb-6">
          <h2 className="section-title">Average Performance</h2>
          <div className="space-y-3">
            <ScoreBar label="Overall Score" value={avg('overallScore')} max={10} />
            <ScoreBar label="Technical Accuracy" value={avg('technicalAccuracy')} max={10} />
            <ScoreBar label="Completeness" value={avg('completeness')} max={10} />
            <ScoreBar label="Communication" value={avg('communication')} max={10} />
          </div>
        </div>

        {/* Per-question breakdown */}
        <div className="space-y-4">
          {interviews.map((interview, i) => {
            const f = interview.feedback;
            const color = (interview.score ?? 0) >= 8 ? 'text-emerald-400 border-emerald-500/20' :
                          (interview.score ?? 0) >= 6 ? 'text-primary-400 border-primary-500/20' :
                          (interview.score ?? 0) >= 4 ? 'text-amber-400 border-amber-500/20' : 'text-red-400 border-red-500/20';
            return (
              <div key={interview.id} className="card">
                <div className="flex items-start justify-between gap-4 mb-3">
                  <div className="flex-1">
                    <p className="text-xs text-slate-500 mb-1">Q{i + 1} · {interview.category}</p>
                    <p className="text-slate-200 font-medium text-sm">{interview.question}</p>
                  </div>
                  <span className={`text-xl font-bold ${color} border px-2 py-1 rounded-lg`}>
                    {interview.score}/10
                  </span>
                </div>

                <details className="group">
                  <summary className="text-xs text-primary-400 cursor-pointer select-none hover:text-primary-300">
                    View your answer & feedback ↓
                  </summary>
                  <div className="mt-3 space-y-3">
                    <div className="p-3 bg-dark-700 rounded-xl">
                      <p className="text-xs text-slate-500 mb-1">YOUR ANSWER</p>
                      <p className="text-sm text-slate-300">{interview.answer}</p>
                    </div>
                    {f && (
                      <>
                        {f.strengths?.length > 0 && (
                          <div><p className="text-xs text-emerald-400 font-semibold mb-1">Strengths</p>
                            {f.strengths.map((s, j) => <p key={j} className="text-sm text-slate-300">• {s}</p>)}
                          </div>
                        )}
                        {f.improvements?.length > 0 && (
                          <div><p className="text-xs text-amber-400 font-semibold mb-1">Improvements</p>
                            {f.improvements.map((s, j) => <p key={j} className="text-sm text-slate-300">• {s}</p>)}
                          </div>
                        )}
                      </>
                    )}
                  </div>
                </details>
              </div>
            );
          })}
        </div>

        <div className="mt-6 flex gap-3 flex-wrap">
          <Link to="/dashboard" className="btn-secondary">← Dashboard</Link>
          <Link to="/analysis/new" className="btn-primary">New Analysis</Link>
        </div>
      </div>
    </div>
  );
}
