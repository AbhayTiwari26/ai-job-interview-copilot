import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { analysisService } from '../services/analysisService';
import Navbar from '../components/Navbar';
import { Spinner } from '../components/Spinner';

export default function DashboardPage() {
  const { user } = useAuth();
  const [analyses, setAnalyses] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    analysisService.getAll()
      .then(({ data }) => setAnalyses(data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const scoreColor = (s) => s >= 80 ? 'text-emerald-400' : s >= 60 ? 'text-primary-400' : s >= 40 ? 'text-amber-400' : 'text-red-400';
  const scoreBg   = (s) => s >= 80 ? 'bg-emerald-500/10 border-emerald-500/20' : s >= 60 ? 'bg-primary-500/10 border-primary-500/20' : 'bg-amber-500/10 border-amber-500/20';

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="page-container animate-fade-in">
        {/* Hero greeting */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white">
            Good {new Date().getHours() < 12 ? 'morning' : new Date().getHours() < 17 ? 'afternoon' : 'evening'},{' '}
            <span className="text-primary-400">{user?.name?.split(' ')[0]}</span> 👋
          </h1>
          <p className="text-slate-400 mt-1">Track your job match scores and practice your interviews</p>
        </div>

        {/* Quick actions */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-8">
          <Link to="/analysis/new" id="new-analysis-btn"
            className="card hover:border-primary-500/40 hover:bg-dark-700 transition-all duration-200 group cursor-pointer">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-primary-600/20 rounded-xl flex items-center justify-center group-hover:bg-primary-600/30 transition-colors">
                <span className="text-2xl">📄</span>
              </div>
              <div>
                <p className="font-semibold text-white">Analyze Resume</p>
                <p className="text-sm text-slate-400">Upload resume & paste job description</p>
              </div>
            </div>
          </Link>

          <div className="card border-dashed border-dark-400 opacity-60 cursor-not-allowed">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 bg-dark-600 rounded-xl flex items-center justify-center">
                <span className="text-2xl">🎤</span>
              </div>
              <div>
                <p className="font-semibold text-slate-300">Mock Interview</p>
                <p className="text-sm text-slate-500">Run an analysis first to unlock</p>
              </div>
            </div>
          </div>
        </div>

        {/* Recent analyses */}
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="section-title mb-0">Recent Analyses</h2>
            {analyses.length > 0 && (
              <Link to="/analysis/new" className="text-sm text-primary-400 hover:text-primary-300">+ New</Link>
            )}
          </div>

          {loading ? (
            <div className="flex justify-center py-12"><Spinner /></div>
          ) : analyses.length === 0 ? (
            <div className="card text-center py-12">
              <p className="text-4xl mb-3">🎯</p>
              <p className="text-slate-300 font-medium">No analyses yet</p>
              <p className="text-slate-500 text-sm mt-1 mb-4">Upload your resume to get your first match score</p>
              <Link to="/analysis/new" className="btn-primary">Start your first analysis</Link>
            </div>
          ) : (
            <div className="grid gap-3">
              {analyses.map((a) => (
                <Link key={a.id} to={`/analysis/${a.id}`}
                  className="card hover:border-primary-500/30 hover:bg-dark-700 transition-all duration-200 flex items-center justify-between group">
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-white truncate group-hover:text-primary-300 transition-colors">
                      {a.jobTitle}
                    </p>
                    <p className="text-sm text-slate-400 truncate">{a.resumeFilename}</p>
                    <p className="text-xs text-slate-500 mt-1">{new Date(a.createdAt).toLocaleDateString()}</p>
                  </div>
                  <div className={`ml-4 px-3 py-1.5 rounded-lg border text-sm font-bold ${scoreBg(a.matchScore)}`}>
                    <span className={scoreColor(a.matchScore)}>{a.matchScore}%</span>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
