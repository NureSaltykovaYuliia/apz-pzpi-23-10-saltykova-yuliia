using Entities.Models;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Application.Abstractions.Interfaces
{
    public interface IConversationRepository
    {
        Task<IEnumerable<Conversation>> GetUserConversationsAsync(int userId);
        Task<Conversation?> FindPrivateConversationAsync(int user1Id, int user2Id);
        Task<Conversation> CreateConversationAsync(Conversation conversation);
        Task<Conversation?> GetByIdAsync(int conversationId);
        Task<bool> DeleteConversationAsync(int id);
    }
}