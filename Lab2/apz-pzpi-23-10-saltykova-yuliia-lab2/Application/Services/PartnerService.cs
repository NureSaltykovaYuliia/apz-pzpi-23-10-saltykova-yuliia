using Application.Abstractions.Interfaces;
using Application.DTOs;
using Entities.Models;

namespace Application.Services
{
    public class PartnerService : IPartnerService
    {
        private readonly IPartnerRepository _partnerRepository;

        public PartnerService(IPartnerRepository partnerRepository)
        {
            _partnerRepository = partnerRepository;
        }

        public async Task<IEnumerable<PartnerDto>> GetAllPartnersAsync()
        {
            var partners = await _partnerRepository.GetAllAsync();
            return partners.Select(MapToDto);
        }

        public async Task<PartnerDto?> GetPartnerByIdAsync(int id)
        {
            var partner = await _partnerRepository.GetByIdAsync(id);
            if (partner == null) return null;

            return MapToDto(partner);
        }

        public async Task<PartnerDto> CreatePartnerAsync(CreateUpdatePartnerDto partnerDto)
        {
            var partner = new Partner
            {
                Name = partnerDto.Name,
                Description = partnerDto.Description,
                Address = partnerDto.Address,
                PhoneNumber = partnerDto.PhoneNumber,
                Website = partnerDto.Website,
                Latitude = partnerDto.Latitude,
                Longitude = partnerDto.Longitude,
                PhotoUrl = partnerDto.PhotoUrl
            };

            var createdPartner = await _partnerRepository.AddAsync(partner);
            return MapToDto(createdPartner);
        }

        public async Task UpdatePartnerAsync(int id, CreateUpdatePartnerDto partnerDto)
        {
            var partner = await _partnerRepository.GetByIdAsync(id);
            if (partner == null)
                throw new Exception("Партнер не знайдений");

            partner.Name = partnerDto.Name;
            partner.Description = partnerDto.Description;
            partner.Address = partnerDto.Address;
            partner.PhoneNumber = partnerDto.PhoneNumber;
            partner.Website = partnerDto.Website;
            partner.Latitude = partnerDto.Latitude;
            partner.Longitude = partnerDto.Longitude;
            partner.PhotoUrl = partnerDto.PhotoUrl;

            await _partnerRepository.UpdateAsync(partner);
        }

        public async Task DeletePartnerAsync(int id)
        {
            var partner = await _partnerRepository.GetByIdAsync(id);
            if (partner == null)
                throw new Exception("Партнер не знайдений");

            await _partnerRepository.DeleteAsync(id);
        }

        public async Task<IEnumerable<PartnerDto>> SearchPartnersAsync(string query, double? lat, double? lon, double? radius)
        {
            var partners = await _partnerRepository.GetAllAsync();
            var filtered = partners.AsEnumerable();

            if (!string.IsNullOrWhiteSpace(query))
            {
                var lowerQuery = query.ToLower();
                filtered = filtered.Where(p => p.Name.ToLower().Contains(lowerQuery) || 
                                              p.Description.ToLower().Contains(lowerQuery) || 
                                              p.Address.ToLower().Contains(lowerQuery));
            }

            return filtered.Select(MapToDto);
        }

        private double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            var R = 6371; // Radius of the earth in km
            var dLat = ToRadians(lat2 - lat1);
            var dLon = ToRadians(lon1 - lon2);
            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(ToRadians(lat1)) * Math.Cos(ToRadians(lat2)) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return R * c;
        }

        private double ToRadians(double deg) => deg * (Math.PI / 180);

        private static PartnerDto MapToDto(Partner p)
        {
            return new PartnerDto
            {
                Id = p.Id,
                Name = p.Name,
                Description = p.Description,
                Address = p.Address,
                PhoneNumber = p.PhoneNumber,
                Website = p.Website,
                Latitude = p.Latitude,
                Longitude = p.Longitude,
                PhotoUrl = p.PhotoUrl
            };
        }
    }
}
