import axiosClient from './axiosClient';

export const eventsApi = {
  getAll: () => axiosClient.get('/events'),
  getUpcoming: () => axiosClient.get('/events/upcoming'),
  getMy: () => axiosClient.get('/events/my'),
  getById: (id) => axiosClient.get(`/events/${id}`),
  create: (data) => axiosClient.post('/events', data),
  update: (id, data) => axiosClient.put(`/events/${id}`, data),
  delete: (id) => axiosClient.delete(`/events/${id}`),
};
