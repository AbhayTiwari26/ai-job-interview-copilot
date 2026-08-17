import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import AnalysisPage from './pages/AnalysisPage';
import ResultsPage from './pages/ResultsPage';
import InterviewPage from './pages/InterviewPage';
import InterviewResultsPage from './pages/InterviewResultsPage';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected */}
          <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
          <Route path="/analysis/new" element={<ProtectedRoute><AnalysisPage /></ProtectedRoute>} />
          <Route path="/analysis/:id" element={<ProtectedRoute><ResultsPage /></ProtectedRoute>} />
          <Route path="/interview/:analysisId" element={<ProtectedRoute><InterviewPage /></ProtectedRoute>} />
          <Route path="/interview/:analysisId/results" element={<ProtectedRoute><InterviewResultsPage /></ProtectedRoute>} />

          {/* Default */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
