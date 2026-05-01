using Application.DTOs;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Application.Abstractions.Interfaces
{
    public interface IConversationService
    {
        Task<IEnumerable<ConversationDto>> GetUserConversationsAsync(int userId);
        Task<IEnumerable<MessageDto>> GetMessageHistoryAsync(int conversationId, int count = 50);
        Task<ConversationDto> GetOrCreatePrivateConversationAsync(int currentUserId, int targetUserId);
        Task<bool> DeleteConversationAsync(int conversationId, int userId);
    }
}
