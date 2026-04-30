import axiosClient from './axiosClient';

export const usersApi = {
  getProfile: () => axiosClient.get('/users/profile'),
  updateProfile: (data) => axiosClient.put('/users/profile', data),
  deleteProfile: () => axiosClient.delete('/users/profile'),

  // Admin endpoints
  getStatistics: () => axiosClient.get('/users/admin/statistics'),
  getAllActivity: () => axiosClient.get('/users/admin/activity'),
  getUserActivity: (userId) => axiosClient.get(`/users/admin/activity/${userId}`),
  blockUser: (userId, blockReason) => axiosClient.post(`/users/admin/block/${userId}`, { blockReason }),
  unblockUser: (userId) => axiosClient.post(`/users/admin/unblock/${userId}`),
};
