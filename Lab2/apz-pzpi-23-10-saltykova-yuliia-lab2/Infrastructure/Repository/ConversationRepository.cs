using Application.Abstractions.Interfaces;
using Entities.Models;
using Infrastructure; 
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Infrastructure.Repositories
{
    public class ConversationRepository : IConversationRepository
    {
        private readonly MyDbContext _context;

        public ConversationRepository(MyDbContext context)
        {
            _context = context;
        }

        public async Task<Conversation> CreateConversationAsync(Conversation conversation)
        {
            await _context.Conversations.AddAsync(conversation);
            await _context.SaveChangesAsync();
            return conversation;
        }

        public async Task<Conversation?> FindPrivateConversationAsync(int user1Id, int user2Id)
        {
            var conversations = await _context.Conversations
                .Include(c => c.Participants)
                .Where(c => c.Participants.Any(p => p.Id == user1Id) && 
                            c.Participants.Any(p => p.Id == user2Id))
                .ToListAsync();

            return conversations.FirstOrDefault(c => c.Participants.Count == 2);
        }

        public async Task<Conversation?> GetByIdAsync(int conversationId)
        {
            return await _context.Conversations
                .Include(c => c.Participants)
                .FirstOrDefaultAsync(c => c.Id == conversationId);
        }

        public async Task<IEnumerable<Conversation>> GetUserConversationsAsync(int userId)
        {
            return await _context.Conversations
                .Include(c => c.Participants)
                .Where(c => c.Participants.Any(p => p.Id == userId))
                .ToListAsync();
        }

        public async Task<bool> DeleteConversationAsync(int id)
        {
            var conversation = await _context.Conversations.FindAsync(id);
            if (conversation == null) return false;
            _context.Conversations.Remove(conversation);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}