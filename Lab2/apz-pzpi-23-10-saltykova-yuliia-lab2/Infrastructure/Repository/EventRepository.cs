using Application.Abstractions.Interfaces;
using Entities.Models;
using Infrastructure;
using Microsoft.EntityFrameworkCore;

namespace Infrastructure.Repositories
{
    public class EventRepository : IEventRepository
    {
        private readonly MyDbContext _context;

        public EventRepository(MyDbContext context)
        {
            _context = context;
        }

        public async Task<Event?> GetByIdAsync(int eventId)
        {
            return await _context.Events
                .Include(e => e.Organizer)
                .Include(e => e.Participants)
                .FirstOrDefaultAsync(e => e.Id == eventId);
        }

        public async Task<IEnumerable<Event>> GetAllAsync()
        {
            return await _context.Events
                .Include(e => e.Organizer)
                .Include(e => e.Participants)
                .ToListAsync();
        }

        public async Task<IEnumerable<Event>> GetByOrganizerIdAsync(int organizerId)
        {
            return await _context.Events
                .Where(e => e.OrganizerId == organizerId)
                .Include(e => e.Organizer)
                .Include(e => e.Participants)
                .ToListAsync();
        }

        public async Task<IEnumerable<Event>> GetByUserIdAsync(int userId)
        {
            return await _context.Events
                .Include(e => e.Organizer)
                .Include(e => e.Participants)
                .Where(e => e.OrganizerId == userId || e.Participants.Any(p => p.Id == userId))
                .ToListAsync();
        }

        public async Task<IEnumerable<Event>> GetUpcomingEventsAsync()
        {
            return await _context.Events
                .Where(e => e.StartTime > DateTime.Now)
                .Include(e => e.Organizer)
                .Include(e => e.Participants)
                .OrderBy(e => e.StartTime)
                .ToListAsync();
        }

        public async Task<Event> AddAsync(Event eventEntity)
        {
            _context.Events.Add(eventEntity);
            await _context.SaveChangesAsync();

            // Load the Organizer navigation property
            await _context.Entry(eventEntity).Reference(e => e.Organizer).LoadAsync();

            return eventEntity;
        }

        public async Task UpdateAsync(Event eventEntity)
        {
            if (_context.Entry(eventEntity).State == EntityState.Detached)
            {
                _context.Events.Update(eventEntity);
            }
            await _context.SaveChangesAsync();
        }

        public async Task DeleteAsync(int eventId)
        {
            var eventEntity = await GetByIdAsync(eventId);
            if (eventEntity != null)
            {
                _context.Events.Remove(eventEntity);
                await _context.SaveChangesAsync();
            }
        }
        public async Task JoinEventAsync(int eventId, int userId)
        {
            var eventEntity = await _context.Events.Include(e => e.Participants).FirstOrDefaultAsync(e => e.Id == eventId);
            if (eventEntity == null) return;

            if (eventEntity.Participants.Any(p => p.Id == userId)) return;

            var user = await _context.Users.FindAsync(userId);
            if (user == null) return;

            eventEntity.Participants.Add(user);
            await _context.SaveChangesAsync();
        }

        public async Task LeaveEventAsync(int eventId, int userId)
        {
            var eventEntity = await _context.Events.Include(e => e.Participants).FirstOrDefaultAsync(e => e.Id == eventId);
            if (eventEntity == null) return;

            var participant = eventEntity.Participants.FirstOrDefault(p => p.Id == userId);
            if (participant != null)
            {
                eventEntity.Participants.Remove(participant);
                await _context.SaveChangesAsync();
            }
        }
    }
}
