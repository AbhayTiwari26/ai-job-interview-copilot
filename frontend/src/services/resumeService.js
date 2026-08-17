import api from './api';

export const resumeService = {
  upload: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post('/resumes/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
  getAll: () => api.get('/resumes'),
  getById: (id) => api.get(`/resumes/${id}`),
};
