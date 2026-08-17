import api from './api';

export const analysisService = {
  create: (data) => api.post('/analysis', data),
  getAll: () => api.get('/analysis'),
  getById: (id) => api.get(`/analysis/${id}`),
};
