import api from './api';

export const interviewService = {
  generate: (analysisId) => api.post('/interviews/generate', { analysisId }),
  submitAnswer: (id, answer) => api.post(`/interviews/${id}/answer`, { answer }),
  getByAnalysis: (analysisId) => api.get(`/interviews/analysis/${analysisId}`),
};
