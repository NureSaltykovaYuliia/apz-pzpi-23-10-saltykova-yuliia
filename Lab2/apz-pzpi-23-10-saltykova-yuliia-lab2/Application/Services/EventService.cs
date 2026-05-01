using Application.Abstractions.Interfaces;
using Application.DTOs;
using Entities.Models;

namespace Application.Services
{
    public class EventService : IEventService
    {
        private readonly IEventRepository _eventRepository;
        private readonly IUserRepository _userRepository;

        public EventService(IEventRepository eventRepository, IUserRepository userRepository)
        {
            _eventRepository = eventRepository;
            _userRepository = userRepository;
        }

        public async Task<IEnumerable<EventDto>> GetAllEventsAsync(int? currentUserId = null)
        {
            var events = await _eventRepository.GetAllAsync();
            return events.Select(e => MapToDto(e, currentUserId));
        }

        public async Task<IEnumerable<EventDto>> GetUpcomingEventsAsync(int? currentUserId = null)
        {
            var events = await _eventRepository.GetUpcomingEventsAsync();
            return events.Select(e => MapToDto(e, currentUserId));
        }

        public async Task<IEnumerable<EventDto>> GetEventsByOrganizerIdAsync(int organizerId, int? currentUserId = null)
        {
            var events = await _eventRepository.GetByOrganizerIdAsync(organizerId);
            return events.Select(e => MapToDto(e, currentUserId));
        }

        public async Task<IEnumerable<EventDto>> GetMyEventsAsync(int userId)
        {
            var events = await _eventRepository.GetByUserIdAsync(userId);
            return events.Select(e => MapToDto(e, userId));
        }

        public async Task<EventDto?> GetEventByIdAsync(int id, int? currentUserId = null)
        {
            var eventEntity = await _eventRepository.GetByIdAsync(id);
            if (eventEntity == null) return null;

            return MapToDto(eventEntity, currentUserId);
        }

        public async Task<EventDto> CreateEventAsync(CreateUpdateEventDto eventDto, int organizerId)
        {
            var eventEntity = new Event
            {
                Name = eventDto.Name,
                Description = eventDto.Description,
                StartTime = eventDto.StartTime,
                EndTime = eventDto.EndTime,
                Type = eventDto.Type,
                Latitude = eventDto.Latitude,
                Longitude = eventDto.Longitude,
                OrganizerId = organizerId
            };

            var createdEvent = await _eventRepository.AddAsync(eventEntity);
            return MapToDto(createdEvent);
        }

        public async Task UpdateEventAsync(int id, CreateUpdateEventDto eventDto, int organizerId)
        {
            var eventEntity = await _eventRepository.GetByIdAsync(id);
            if (eventEntity == null)
                throw new Exception("Подія не знайдена");

            if (eventEntity.OrganizerId != organizerId)
                throw new UnauthorizedAccessException("Ви не маєте доступу до цієї події");

            eventEntity.Name = eventDto.Name;
            eventEntity.Description = eventDto.Description;
            eventEntity.StartTime = eventDto.StartTime;
            eventEntity.EndTime = eventDto.EndTime;
            eventEntity.Type = eventDto.Type;
            eventEntity.Latitude = eventDto.Latitude;
            eventEntity.Longitude = eventDto.Longitude;

            await _eventRepository.UpdateAsync(eventEntity);
        }

        public async Task DeleteEventAsync(int id, int organizerId)
        {
            var eventEntity = await _eventRepository.GetByIdAsync(id);
            if (eventEntity == null)
                throw new Exception("Подія не знайдена");

            if (eventEntity.OrganizerId != organizerId)
                throw new UnauthorizedAccessException("Ви не маєте доступу до цієї події");

            await _eventRepository.DeleteAsync(id);
        }

        public async Task JoinEventAsync(int eventId, int userId)
        {
            await _eventRepository.JoinEventAsync(eventId, userId);
        }

        public async Task LeaveEventAsync(int eventId, int userId)
        {
            await _eventRepository.LeaveEventAsync(eventId, userId);
        }

        private static EventDto MapToDto(Event e, int? currentUserId = null)
        {
            return new EventDto
            {
                Id = e.Id,
                Name = e.Name,
                Description = e.Description,
                StartTime = e.StartTime,
                EndTime = e.EndTime,
                Type = e.Type.ToString(),
                Latitude = e.Latitude,
                Longitude = e.Longitude,
                OrganizerId = e.OrganizerId,
                OrganizerName = e.Organizer?.Username,
                ParticipantCount = e.Participants?.Count ?? 0,
                IsJoined = currentUserId.HasValue && e.Participants != null && e.Participants.Any(p => p.Id == currentUserId.Value)
            };
        }
    }
}
