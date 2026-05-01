using Application.Abstractions.Interfaces;
using Application.DTOs;
using Entities.Models;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System;

namespace Application.Services
{
    public class ConversationService : IConversationService
    {
        private readonly IConversationRepository _conversationRepository;
        private readonly IMessageRepository _messageRepository;
        private readonly IUserRepository _userRepository;
        private static readonly SemaphoreSlim _semaphore = new SemaphoreSlim(1, 1);

        public ConversationService(IConversationRepository conversationRepository, IMessageRepository messageRepository, IUserRepository userRepository)
        {
            _conversationRepository = conversationRepository;
            _messageRepository = messageRepository;
            _userRepository = userRepository;
        }

        public async Task<IEnumerable<ConversationDto>> GetUserConversationsAsync(int userId)
        {
            var conversations = await _conversationRepository.GetUserConversationsAsync(userId);
            return conversations.Select(c => new ConversationDto
            {
                Id = c.Id,
                Name = c.Name,
                ParticipantIds = c.Participants?.Select(p => p.Id).ToList() ?? new List<int>(),
                ParticipantNames = c.Participants?.Select(p => p.Username).ToList() ?? new List<string>()
            });
        }

        public async Task<IEnumerable<MessageDto>> GetMessageHistoryAsync(int conversationId, int count = 50)
        {
            var messages = await _messageRepository.GetMessagesAsync(conversationId, count);
            return messages.Select(m => new MessageDto
            {
                Id = m.Id,
                Content = m.Content,
                Timestamp = m.Timestamp,
                SenderId = m.SenderId,
                SenderName = m.Sender?.Username ?? "Unknown",
                ConversationId = m.ConversationId
            });
        }

        public async Task<ConversationDto> GetOrCreatePrivateConversationAsync(int currentUserId, int targetUserId)
        {
            if (currentUserId == targetUserId)
            {
                throw new Exception("Ви не можете почати розмову з самим собою.");
            }

            await _semaphore.WaitAsync();
            try
            {
                var existing = await _conversationRepository.FindPrivateConversationAsync(currentUserId, targetUserId);
                if (existing != null)
                {
                    return new ConversationDto
                    {
                        Id = existing.Id,
                        Name = existing.Name,
                        ParticipantIds = existing.Participants.Select(p => p.Id).ToList(),
                        ParticipantNames = existing.Participants.Select(p => p.Username).ToList()
                    };
                }

                var currentUser = await _userRepository.GetUserByIdAsync(currentUserId);
                var targetUser = await _userRepository.GetUserByIdAsync(targetUserId);

                if (currentUser == null || targetUser == null)
                {
                    throw new Exception("Користувач не знайдений.");
                }

                var newConversation = new Conversation
                {
                    Participants = new List<User> { currentUser, targetUser },
                    Name = null
                };

                var created = await _conversationRepository.CreateConversationAsync(newConversation);

                return new ConversationDto
                {
                    Id = created.Id,
                    Name = created.Name,
                    ParticipantIds = created.Participants.Select(p => p.Id).ToList(),
                    ParticipantNames = created.Participants.Select(p => p.Username).ToList()
                };
            }
            finally
            {
                _semaphore.Release();
            }
        }

        public async Task<bool> DeleteConversationAsync(int conversationId, int userId)
        {
            var conversation = await _conversationRepository.GetByIdAsync(conversationId);
            if (conversation == null || !conversation.Participants.Any(p => p.Id == userId))
            {
                return false;
            }
            return await _conversationRepository.DeleteConversationAsync(conversationId);
        }
    }
}
