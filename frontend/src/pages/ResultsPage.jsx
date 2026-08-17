import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { Spinner } from '../components/Spinner';
import ErrorMessage from '../components/ErrorMessage';
import { ScoreBar } from '../components/ScoreGauge';
import { analysisService } from '../services/analysisService';

export default function ResultsPage() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    analysisService.getById(id)
      .then(({ data }) => setData(data))
      .catch((err) => setError(err.response?.data?.message || 'Failed to load analysis.'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return (
    <div className="min-h-screen bg-dark-900"><Navbar />
      <div className="flex justify-center items-center py-32"><Spinner size="lg" /></div>
    </div>
  );

  if (error) return (
    <div className="min-h-screen bg-dark-900"><Navbar />
      <div className="page-container"><ErrorMessage message={error} /></div>
    </div>
  );

  const score = data.matchScore ?? 0;
  const scoreColor = score >= 80 ? 'text-emerald-400' : score >= 60 ? 'text-primary-400' : score >= 40 ? 'text-amber-400' : 'text-red-400';
  const scoreLabel = score >= 80 ? 'Excellent Match' : score >= 60 ? 'Good Match' : score >= 40 ? 'Partial Match' : 'Low Match';

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="page-container animate-fade-in">
        {/* Header */}
        <div className="flex items-start justify-between mb-6 flex-wrap gap-4">
          <div>
            <h1 className="page-title">{data.jobTitle}</h1>
            <p className="text-slate-400 text-sm mt-1">Resume: {data.resumeFilename}</p>
            <p className="text-slate-500 text-xs mt-0.5">{new Date(data.createdAt).toLocaleDateString('en-IN', { dateStyle: 'medium' })}</p>
          </div>
          <Link to={`/interview/${id}`} id="prepare-interview-btn" className="btn-primary">
            🎤 Prepare Interview
          </Link>
        </div>

        {/* Score hero */}
        <div className="card mb-6 text-center py-8">
          <p className={`text-7xl font-black ${scoreColor}`}>{score}%</p>
          <p className={`text-lg font-semibold mt-1 ${scoreColor}`}>{scoreLabel}</p>
          <p className="text-slate-400 text-sm mt-2">Overall resume-to-job match score</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          {/* Category scores */}
          {data.skillAnalysis && (
            <div className="card">
              <h2 className="section-title">Category Scores</h2>
              <div className="space-y-4">
                <ScoreBar label="Technical Skills" value={data.skillAnalysis.technical} />
                <ScoreBar label="Experience" value={data.skillAnalysis.experience} />
                <ScoreBar label="Projects" value={data.skillAnalysis.projects} />
                <ScoreBar label="Keywords / ATS" value={data.skillAnalysis.keywords} />
              </div>
            </div>
          )}

          {/* Skills */}
          <div className="space-y-4">
            {data.matchedSkills?.length > 0 && (
              <div className="card">
                <h2 className="section-title">✅ Matched Skills</h2>
                <div className="flex flex-wrap gap-2">
                  {data.matchedSkills.map((s) => (
                    <span key={s} className="badge-success">{s}</span>
                  ))}
                </div>
              </div>
            )}
            {data.missingSkills?.length > 0 && (
              <div className="card">
                <h2 className="section-title">❌ Missing Skills</h2>
                <div className="flex flex-wrap gap-2">
                  {data.missingSkills.map((s) => (
                    <span key={s} className="badge-danger">{s}</span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Strengths */}
          {data.strengths?.length > 0 && (
            <div className="card">
              <h2 className="section-title">💪 Strengths</h2>
              <ul className="space-y-2">
                {data.strengths.map((s, i) => (
                  <li key={i} className="flex items-start gap-2 text-sm text-slate-300">
                    <span className="text-emerald-400 mt-0.5">•</span>{s}
                  </li>
                ))}
              </ul>
            </div>
          )}
          {/* Recommendations */}
          {data.recommendations?.length > 0 && (
            <div className="card">
              <h2 className="section-title">🚀 Recommendations</h2>
              <ul className="space-y-2">
                {data.recommendations.map((r, i) => (
                  <li key={i} className="flex items-start gap-2 text-sm text-slate-300">
                    <span className="text-primary-400 mt-0.5">→</span>{r}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>

        <div className="mt-6 flex gap-3">
          <Link to="/analysis/new" className="btn-secondary">← New Analysis</Link>
          <Link to={`/interview/${id}`} className="btn-primary">Start Mock Interview →</Link>
        </div>
      </div>
    </div>
  );
}
