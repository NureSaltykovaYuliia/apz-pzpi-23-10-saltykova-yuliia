using Application.DTOs;

namespace Application.Abstractions.Interfaces
{
    public interface IEventService
    {
        Task<IEnumerable<EventDto>> GetAllEventsAsync(int? currentUserId = null);
        Task<IEnumerable<EventDto>> GetUpcomingEventsAsync(int? currentUserId = null);
        Task<IEnumerable<EventDto>> GetEventsByOrganizerIdAsync(int organizerId, int? currentUserId = null);
        Task<EventDto?> GetEventByIdAsync(int id, int? currentUserId = null);
        Task<EventDto> CreateEventAsync(CreateUpdateEventDto eventDto, int organizerId);
        Task UpdateEventAsync(int id, CreateUpdateEventDto eventDto, int organizerId);
        Task DeleteEventAsync(int id, int organizerId);
        Task<IEnumerable<EventDto>> GetMyEventsAsync(int userId);
        Task JoinEventAsync(int eventId, int userId);
        Task LeaveEventAsync(int eventId, int userId);
    }
}
