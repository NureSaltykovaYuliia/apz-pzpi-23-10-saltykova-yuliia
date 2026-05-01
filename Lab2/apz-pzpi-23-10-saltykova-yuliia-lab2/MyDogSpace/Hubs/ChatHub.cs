using Microsoft.AspNetCore.SignalR;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Application.Abstractions.Interfaces;
using Entities.Models;

namespace MyDogSpace.Hubs
{
    [Authorize] 
    public class ChatHub : Hub
    {
        private readonly IMessageRepository _messageRepo;
        private readonly IConversationRepository _conversationRepo;

        public ChatHub(IMessageRepository messageRepo, IConversationRepository conversationRepo)
        {
            _messageRepo = messageRepo;
            _conversationRepo = conversationRepo;
        }
        public async Task JoinConversation(int conversationId)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, conversationId.ToString());
        }

        public async Task SendMessage(int conversationId, string content)
        { 
            var senderId = int.Parse(Context.User.FindFirstValue(ClaimTypes.NameIdentifier));
            Console.WriteLine($"[ChatHub] SendMessage called: Conv={conversationId}, Sender={senderId}");
            
            var conversation = await _conversationRepo.GetByIdAsync(conversationId);
            if (conversation == null) {
                Console.WriteLine($"[ChatHub] Error: Conversation {conversationId} not found.");
                throw new HubException("Conversation not found.");
            }
            if (!conversation.Participants.Any(p => p.Id == senderId))
            {
                Console.WriteLine($"[ChatHub] Error: User {senderId} is not a participant in conversation {conversationId}.");
                throw new HubException("Ви не є учасником цієї розмови.");
            }

            var message = new Message
            {
                SenderId = senderId,
                ConversationId = conversationId,
                Content = content,
                Timestamp = DateTime.UtcNow
            };
            try {
                var savedMessage = await _messageRepo.CreateMessageAsync(message);
                
                var messageDto = new Application.DTOs.MessageDto
                {
                    Id = savedMessage.Id,
                    Content = savedMessage.Content,
                    Timestamp = savedMessage.Timestamp,
                    SenderId = savedMessage.SenderId,
                    SenderName = savedMessage.Sender?.Username ?? "Unknown",
                    ConversationId = savedMessage.ConversationId
                };

                await Clients.Group(conversationId.ToString()).SendAsync("ReceiveMessage", messageDto);
                Console.WriteLine($"[ChatHub] Message sent and broadcasted: {savedMessage.Id}");
            } catch (Exception ex) {
                Console.WriteLine($"[ChatHub] SendMessage error: {ex.Message}");
                Console.WriteLine(ex.StackTrace);
                throw new HubException("Помилка при збереженні повідомлення: " + ex.Message);
            }
        }
        public override async Task OnConnectedAsync()
        {
            var userId = int.Parse(Context.User.FindFirstValue(ClaimTypes.NameIdentifier));
            var conversations = await _conversationRepo.GetUserConversationsAsync(userId);
            foreach (var conv in conversations)
            {
                await Groups.AddToGroupAsync(Context.ConnectionId, conv.Id.ToString());
            }
            await base.OnConnectedAsync();
        }
        public override async Task OnDisconnectedAsync(Exception? exception)
        {
            await base.OnDisconnectedAsync(exception);
        }
    }
}