import axiosClient from './axiosClient';

export const notificationsApi = {
  getMy: () => axiosClient.get('/notifications/my'),
  markAsRead: (id) => axiosClient.put(`/notifications/${id}/read`),
  delete: (id) => axiosClient.delete(`/notifications/${id}`),
};
