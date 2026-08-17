import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import { Spinner } from '../components/Spinner';
import ErrorMessage from '../components/ErrorMessage';
import { interviewService } from '../services/interviewService';

const CATEGORY_COLORS = {
  TECHNICAL:  'badge-info',
  PROJECT:    'badge-warning',
  BEHAVIORAL: 'badge-success',
  GENERAL:    'badge-info',
};

export default function InterviewPage() {
  const { analysisId } = useParams();
  const navigate = useNavigate();
  const [interviews, setInterviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [generating, setGenerating] = useState(false);
  const [currentIdx, setCurrentIdx] = useState(0);
  const [answer, setAnswer] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [phase, setPhase] = useState('idle'); // idle | ready | answering | results

  const current = interviews[currentIdx];
  const answered = interviews.filter((i) => i.answer != null);
  const allDone = interviews.length > 0 && answered.length === interviews.length;

  const handleGenerate = async () => {
    setError('');
    setGenerating(true);
    try {
      const { data } = await interviewService.generate(Number(analysisId));
      setInterviews(data);
      setPhase('ready');
      setCurrentIdx(0);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to generate questions.');
    } finally {
      setGenerating(false);
    }
  };

  const handleSubmitAnswer = async () => {
    if (!answer.trim()) { setError('Please write an answer before submitting.'); return; }
    setError('');
    setSubmitting(true);
    try {
      const { data } = await interviewService.submitAnswer(current.id, answer.trim());
      setInterviews((prev) => prev.map((i) => i.id === data.id ? data : i));
      setPhase('answering');
      setAnswer('');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit answer.');
    } finally {
      setSubmitting(false);
    }
  };

  const goNext = () => {
    if (currentIdx < interviews.length - 1) {
      setCurrentIdx((i) => i + 1);
      setAnswer('');
      setError('');
    } else {
      navigate(`/interview/${analysisId}/results`);
    }
  };

  if (phase === 'idle') {
    return (
      <div className="min-h-screen bg-dark-900">
        <Navbar />
        <div className="page-container max-w-2xl animate-fade-in">
          <div className="card text-center py-12">
            <span className="text-5xl">🎤</span>
            <h1 className="text-2xl font-bold text-white mt-4">Mock Interview</h1>
            <p className="text-slate-400 text-sm mt-2 mb-6">
              AI will generate 9 personalized questions based on your resume and job description
            </p>
            <ErrorMessage message={error} />
            <button id="generate-questions-btn" onClick={handleGenerate} className="btn-primary mt-4" disabled={generating}>
              {generating ? <Spinner size="sm" /> : '⚡'}
              {generating ? 'Generating questions...' : 'Generate Interview Questions'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  const feedback = current?.feedback;

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="page-container max-w-2xl animate-fade-in">
        {/* Progress */}
        <div className="flex items-center justify-between mb-4 text-sm text-slate-400">
          <span>Question {currentIdx + 1} of {interviews.length}</span>
          <span>{answered.length} answered</span>
        </div>
        <div className="h-1.5 bg-dark-600 rounded-full mb-6">
          <div className="h-full bg-primary-600 rounded-full transition-all duration-500"
            style={{ width: `${((currentIdx + 1) / interviews.length) * 100}%` }} />
        </div>

        {/* Question */}
        <div className="card mb-4">
          <div className="flex items-center gap-2 mb-3">
            <span className={current?.category ? CATEGORY_COLORS[current.category] : 'badge-info'}>
              {current?.category}
            </span>
          </div>
          <p className="text-white font-medium text-lg leading-relaxed">{current?.question}</p>
        </div>

        {/* If already answered — show feedback */}
        {current?.answer ? (
          <div className="space-y-4 animate-slide-up">
            <div className="card border-emerald-500/20 bg-emerald-500/5">
              <p className="text-xs text-emerald-400 font-semibold mb-2">YOUR ANSWER</p>
              <p className="text-slate-300 text-sm">{current.answer}</p>
            </div>

            {feedback && (
              <div className="card space-y-4">
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                  {[
                    { label: 'Overall', val: feedback.overallScore },
                    { label: 'Technical', val: feedback.technicalAccuracy },
                    { label: 'Completeness', val: feedback.completeness },
                    { label: 'Communication', val: feedback.communication },
                  ].map(({ label, val }) => {
                    const color = val >= 8 ? 'text-emerald-400' : val >= 6 ? 'text-primary-400' : val >= 4 ? 'text-amber-400' : 'text-red-400';
                    return (
                      <div key={label} className="text-center p-3 bg-dark-700 rounded-xl">
                        <p className={`text-2xl font-bold ${color}`}>{val}/10</p>
                        <p className="text-xs text-slate-400 mt-1">{label}</p>
                      </div>
                    );
                  })}
                </div>

                {feedback.strengths?.length > 0 && (
                  <div>
                    <p className="text-xs text-emerald-400 font-semibold mb-2">✅ STRENGTHS</p>
                    <ul className="space-y-1">
                      {feedback.strengths.map((s, i) => <li key={i} className="text-sm text-slate-300">• {s}</li>)}
                    </ul>
                  </div>
                )}
                {feedback.missingPoints?.length > 0 && (
                  <div>
                    <p className="text-xs text-red-400 font-semibold mb-2">❌ MISSING POINTS</p>
                    <ul className="space-y-1">
                      {feedback.missingPoints.map((s, i) => <li key={i} className="text-sm text-slate-300">• {s}</li>)}
                    </ul>
                  </div>
                )}
                {feedback.improvements?.length > 0 && (
                  <div>
                    <p className="text-xs text-amber-400 font-semibold mb-2">💡 IMPROVEMENTS</p>
                    <ul className="space-y-1">
                      {feedback.improvements.map((s, i) => <li key={i} className="text-sm text-slate-300">• {s}</li>)}
                    </ul>
                  </div>
                )}
                {feedback.betterAnswerStructure && (
                  <div className="p-3 bg-primary-500/10 border border-primary-500/20 rounded-xl">
                    <p className="text-xs text-primary-400 font-semibold mb-1">🎯 BETTER STRUCTURE</p>
                    <p className="text-sm text-slate-300">{feedback.betterAnswerStructure}</p>
                  </div>
                )}
              </div>
            )}

            <button id="next-question-btn" onClick={goNext} className="btn-primary w-full">
              {currentIdx < interviews.length - 1 ? 'Next Question →' : '🏁 View Final Results'}
            </button>
          </div>
        ) : (
          /* Answer input */
          <div className="animate-slide-up">
            <ErrorMessage message={error} />
            <textarea id="answer-input"
              className="input resize-none mb-4" rows={7}
              placeholder="Type your answer here... Be specific and use examples from your experience."
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
            />
            <button id="submit-answer-btn" onClick={handleSubmitAnswer} className="btn-primary w-full" disabled={submitting}>
              {submitting ? <Spinner size="sm" /> : '📤'}
              {submitting ? 'Evaluating your answer...' : 'Submit Answer'}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
