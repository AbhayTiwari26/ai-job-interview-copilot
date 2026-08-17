import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/Navbar';
import ErrorMessage from '../components/ErrorMessage';
import { Spinner } from '../components/Spinner';
import { resumeService } from '../services/resumeService';
import { jobService } from '../services/jobService';
import { analysisService } from '../services/analysisService';

export default function AnalysisPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1=upload, 2=job, 3=analyzing
  const [resumes, setResumes] = useState([]);
  const [selectedResume, setSelectedResume] = useState(null);
  const [uploadingResume, setUploadingResume] = useState(false);
  const [jobForm, setJobForm] = useState({ title: '', description: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    resumeService.getAll().then(({ data }) => setResumes(data)).catch(() => {});
  }, []);

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (file.type !== 'application/pdf') { setError('Only PDF files are accepted.'); return; }
    if (file.size > 10 * 1024 * 1024) { setError('File too large. Max 10MB.'); return; }
    setError('');
    setUploadingResume(true);
    try {
      const { data } = await resumeService.upload(file);
      if (!data.hasText) {
        setError('⚠️ This PDF appears to be scanned (no extractable text). Please upload a text-based PDF.');
      }
      setResumes((prev) => [data, ...prev]);
      setSelectedResume(data.id);
      setStep(2);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to upload resume.');
    } finally {
      setUploadingResume(false);
    }
  };

  const handleAnalyze = async (e) => {
    e.preventDefault();
    if (!selectedResume) { setError('Please select or upload a resume.'); return; }
    if (!jobForm.title.trim() || !jobForm.description.trim()) { setError('Please fill in job title and description.'); return; }
    setError('');
    setLoading(true);
    setStep(3);
    try {
      const { data: job } = await jobService.create(jobForm);
      const { data: analysis } = await analysisService.create({ resumeId: selectedResume, jobDescriptionId: job.id });
      navigate(`/analysis/${analysis.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Analysis failed. Please try again.');
      setStep(2);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-dark-900">
      <Navbar />
      <div className="page-container animate-fade-in max-w-2xl">
        <div className="mb-6">
          <h1 className="page-title">New Job Analysis</h1>
          <p className="text-slate-400 text-sm mt-1">Match your resume against a job description using AI</p>
        </div>

        {/* Progress Steps */}
        <div className="flex items-center gap-2 mb-8">
          {['Resume', 'Job Description', 'Analyzing'].map((s, i) => (
            <div key={s} className="flex items-center gap-2 flex-1">
              <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold shrink-0 transition-colors
                ${step > i + 1 ? 'bg-emerald-500 text-white' : step === i + 1 ? 'bg-primary-600 text-white' : 'bg-dark-600 text-slate-500'}`}>
                {step > i + 1 ? '✓' : i + 1}
              </div>
              <span className={`text-xs font-medium hidden sm:block ${step === i + 1 ? 'text-primary-400' : 'text-slate-500'}`}>{s}</span>
              {i < 2 && <div className={`h-px flex-1 mx-1 ${step > i + 1 ? 'bg-emerald-500' : 'bg-dark-500'}`} />}
            </div>
          ))}
        </div>

        <ErrorMessage message={error} />

        {step === 3 ? (
          <div className="card text-center py-16 animate-fade-in">
            <Spinner size="lg" className="mx-auto mb-4" />
            <p className="text-white font-semibold text-lg">Analyzing with Gemini AI...</p>
            <p className="text-slate-400 text-sm mt-2">This may take 10–30 seconds</p>
          </div>
        ) : (
          <form onSubmit={handleAnalyze} className="space-y-6">
            {/* Step 1 — Resume */}
            <div className="card">
              <h2 className="section-title">1. Resume</h2>
              {resumes.length > 0 && (
                <div className="space-y-2 mb-4">
                  {resumes.map((r) => (
                    <label key={r.id} className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-all
                      ${selectedResume === r.id ? 'border-primary-500 bg-primary-500/10' : 'border-dark-500 hover:border-dark-400'}`}>
                      <input type="radio" name="resume" value={r.id} checked={selectedResume === r.id}
                        onChange={() => { setSelectedResume(r.id); setStep(2); }} className="accent-primary-500" />
                      <div>
                        <p className="text-sm font-medium text-slate-200">{r.filename}</p>
                        <p className="text-xs text-slate-500">{r.hasText ? '✓ Text extracted' : '⚠ No text'}</p>
                      </div>
                    </label>
                  ))}
                </div>
              )}
              <label id="upload-resume-btn"
                className={`flex flex-col items-center justify-center gap-2 p-6 border-2 border-dashed rounded-xl cursor-pointer transition-all
                  ${uploadingResume ? 'opacity-50 cursor-wait' : 'border-dark-400 hover:border-primary-500 hover:bg-primary-500/5'}`}>
                {uploadingResume ? <Spinner /> : <span className="text-3xl">📄</span>}
                <span className="text-sm text-slate-300 font-medium">{uploadingResume ? 'Uploading...' : 'Upload new PDF resume'}</span>
                <span className="text-xs text-slate-500">PDF only, max 10MB</span>
                <input type="file" accept=".pdf" className="hidden" onChange={handleFileUpload} disabled={uploadingResume} />
              </label>
            </div>

            {/* Step 2 — Job Description */}
            {step >= 2 && (
              <div className="card animate-slide-up">
                <h2 className="section-title">2. Job Description</h2>
                <div className="space-y-4">
                  <div>
                    <label className="label">Job Title</label>
                    <input className="input" placeholder="e.g. Backend Engineer" value={jobForm.title}
                      onChange={(e) => setJobForm({ ...jobForm, title: e.target.value })} required />
                  </div>
                  <div>
                    <label className="label">Job Description</label>
                    <textarea id="job-description-input" className="input resize-none" rows={8}
                      placeholder="Paste the full job description here..."
                      value={jobForm.description}
                      onChange={(e) => setJobForm({ ...jobForm, description: e.target.value })} required />
                  </div>
                </div>
                <button id="analyze-btn" type="submit" className="btn-primary w-full mt-4" disabled={loading || uploadingResume}>
                  {loading ? <Spinner size="sm" /> : '🔍'}
                  {loading ? 'Analyzing...' : 'Analyze Match'}
                </button>
              </div>
            )}
          </form>
        )}
      </div>
    </div>
  );
}
