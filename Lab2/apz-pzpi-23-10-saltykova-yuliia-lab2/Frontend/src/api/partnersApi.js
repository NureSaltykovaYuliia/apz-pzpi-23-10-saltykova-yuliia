import axiosClient from './axiosClient';

export const partnersApi = {
  getAll: () => axiosClient.get('/partners'),
  getById: (id) => axiosClient.get(`/partners/${id}`),
  create: (data) => axiosClient.post('/partners', data),
  update: (id, data) => axiosClient.put(`/partners/${id}`, data),
  delete: (id) => axiosClient.delete(`/partners/${id}`),
  search: (params) => axiosClient.get('/partners/search', { params }),
};
