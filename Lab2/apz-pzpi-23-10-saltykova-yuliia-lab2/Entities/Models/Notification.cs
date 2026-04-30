using System;

namespace Entities.Models
{
    public class Notification
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string Message { get; set; }
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
        public bool IsRead { get; set; } = false;

        public int UserId { get; set; }
        public User User { get; set; }

        
        public string NotificationType { get; set; }

    
        public int? RelatedEntityId { get; set; }
    }
}
