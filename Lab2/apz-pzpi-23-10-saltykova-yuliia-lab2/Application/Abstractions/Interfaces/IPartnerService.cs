using Application.DTOs;

namespace Application.Abstractions.Interfaces
{
    public interface IPartnerService
    {
        Task<IEnumerable<PartnerDto>> GetAllPartnersAsync();
        Task<PartnerDto?> GetPartnerByIdAsync(int id);
        Task<PartnerDto> CreatePartnerAsync(CreateUpdatePartnerDto partnerDto);
        Task UpdatePartnerAsync(int id, CreateUpdatePartnerDto partnerDto);
        Task DeletePartnerAsync(int id);
        Task<IEnumerable<PartnerDto>> SearchPartnersAsync(string query, double? lat, double? lon, double? radius);
    }
}
