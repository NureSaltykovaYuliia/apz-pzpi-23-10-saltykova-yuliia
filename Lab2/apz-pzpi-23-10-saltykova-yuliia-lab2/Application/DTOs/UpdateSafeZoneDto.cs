namespace Application.DTOs
{
    public class UpdateSafeZoneDto
    {
        public double? SafeZoneLatitude { get; set; }
        public double? SafeZoneLongitude { get; set; }
        public double? SafeRadius { get; set; }
        public bool? IsFollowingPhone { get; set; }
    }
}
