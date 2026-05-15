using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Application.DTOs
{
    public class DogDto
    {
        public int Id { get; set; }
        public string Name { get; set; }
        public string Breed { get; set; }
        public DateTime DateOfBirth { get; set; }
        public string Description { get; set; }
        public string? PhotoUrl { get; set; }
        public double? Latitude { get; set; }
        public double? Longitude { get; set; }
        
        public double? SafeZoneLatitude { get; set; }
        public double? SafeZoneLongitude { get; set; }
        public double? SafeRadius { get; set; }
        public bool IsFollowingPhone { get; set; }
    }
}
