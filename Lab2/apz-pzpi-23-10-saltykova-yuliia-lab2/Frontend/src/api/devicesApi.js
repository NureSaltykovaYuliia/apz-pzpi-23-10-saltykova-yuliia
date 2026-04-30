import axiosClient from './axiosClient';

export const devicesApi = {
  getAll: () => axiosClient.get('/smartdevices'),
  getById: (id) => axiosClient.get(`/smartdevices/${id}`),
  getByDogId: (dogId) => axiosClient.get(`/smartdevices/dog/${dogId}`),
  create: (data) => axiosClient.post('/smartdevices', data),
  update: (id, data) => axiosClient.put(`/smartdevices/${id}`, data),
  delete: (id) => axiosClient.delete(`/smartdevices/${id}`),
  assignToDog: (deviceGuid, dogId) => axiosClient.post(`/smartdevices/device/${deviceGuid}/assign`, { dogId }),
};
