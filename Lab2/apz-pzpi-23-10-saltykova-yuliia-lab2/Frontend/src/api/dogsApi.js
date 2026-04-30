import axiosClient from './axiosClient';

export const dogsApi = {
  getMyDogs: () => axiosClient.get('/dogs/my'),
  getDogById: (id) => axiosClient.get(`/dogs/${id}`),
  createDog: (data) => axiosClient.post('/dogs', data),
  updateDog: (id, data) => axiosClient.put(`/dogs/${id}`, data),
  deleteDog: (id) => axiosClient.delete(`/dogs/${id}`),
};
