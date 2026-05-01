import axiosClient from './axiosClient';

export const conversationsApi = {
  getConversations: () => axiosClient.get('/conversations'),
  getMessages: (conversationId) => axiosClient.get(`/conversations/${conversationId}/messages`),
  getOrCreatePrivate: (targetUserId) => axiosClient.post(`/conversations/private/${targetUserId}`),
  deleteConversation: (id) => axiosClient.delete(`/conversations/${id}`),
};
